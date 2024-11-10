package org.rhm;

import org.rhm.tokens.ParenthesisTokenHandler;
import org.rhm.tokens.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final List<Lexer.Token<?>> tokens;
    public Interpreter interpreter;
    private int index = 0;

    public Parser(List<Lexer.Token<?>> tokens) {
        this.tokens = tokens;

        logger.info("Parser initialized with {} tokens.", tokens.size());
    }

    public boolean hasMoreTokens() {
        return index < tokens.size();
    }

    public Lexer.Token<?> getCurrentToken() {
        if (hasMoreTokens()) {
            return tokens.get(index);
        }
        return Lexer.EOF_TOKEN;
    }
    public Lexer.Token<?> getNextToken() {
        if (index + 1 < tokens.size()) {
            return tokens.get(index + 1);
        }
        return Lexer.EOF_TOKEN;
    }

    public void advance() {
        if (hasMoreTokens()) {
            index++;
            logger.debug("advance: New index is {}", index);
        }
    }

    public Object parseExpression() {
        logger.debug("Parsing expression starting at token index {}", index);
        Object leftOperand = parsePrimary();

        for (Map.Entry<String, OperationManager.OperatorHandler> entry : Interpreter.operationManager.operatorHandlers.entrySet()) {
            while (hasMoreTokens() && getCurrentToken().type != Lexer.TokenType.EOF) {
                Object operator = getCurrentToken().getAs(Object.class);
                if (operator.equals(entry.getKey())) {
                    logger.debug("Found operator: {}. Parsing with handler.", operator);
                    advance();
                    leftOperand = entry.getValue().parse(leftOperand, parsePrimary());
                } else {
                    break;
                }
            }
        }

        logger.debug("Finished parsing expression, result: {}", leftOperand);
        return leftOperand;
    }


    public Object parsePrimary() {
        logger.debug("Parsing primary expression at index {}", index);
        if (getCurrentToken().type == Lexer.TokenType.IDENTIFIER) {
            String identifier = getCurrentToken().getAs(String.class);
            advance();

            if (getCurrentToken().type == Lexer.TokenType.OPERATOR && getCurrentToken().getAs(String.class).equals("=")) {
                logger.debug("Assignment found for identifier: {}", identifier);
                advance();
                Object value = parseExpression();
                return new Assignment(identifier, value);
            }
            if (Interpreter.functionManager.functionHandlers.containsKey(identifier) && getCurrentToken().type == Lexer.TokenType.PARENTHESIS) {
                logger.debug("Function call found for identifier: {}", identifier);
                return Interpreter.functionManager.functionHandlers.get(identifier).execute(parseFunctionArguments());
            }

            return interpreter.getVariableValue(identifier);
        } else if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_LEFT)) {
            logger.debug("Parsing grouped expression.");
            advance();
            Object result = parseExpression();
            if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_RIGHT)) {
                advance();
            } else {
                logger.error("Expected closing parenthesis at index {}", index);
                throw new IllegalArgumentException("Expected closing parenthesis on index " + index + ".");
            }
            return result;
        } else if (getCurrentToken().type == Lexer.TokenType.EOF) {
            System.exit(0);
            return null;
        }
        Object value = getCurrentToken().getAs(Object.class);
        advance();
        return value;
    }


    private SafeObject[] parseFunctionArguments() {
        logger.debug("Parsing function arguments.");
        advance();  // Advance to the first token after '('
        List<SafeObject> args = new ArrayList<>();

        while (getCurrentToken().type != Lexer.TokenType.PARENTHESIS) {
            if (getCurrentToken().type == Lexer.TokenType.COMMA) {
                advance();
                continue;
            }

            args.add(new SafeObject(parseExpression()));
        }

        if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_RIGHT)) {
            advance();
        } else {
            logger.error("Expected closing parenthesis for function arguments.");
            throw new IllegalArgumentException("Expected closing parenthesis.");
        }

        return args.toArray(new SafeObject[0]);
    }

    public record Assignment(String variableName, Object value) { }
}
