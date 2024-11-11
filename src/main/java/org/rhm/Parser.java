package org.rhm;

import org.rhm.tokens.ParenthesisTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private List<Lexer.Token<?>> tokens;
    public Interpreter interpreter;
    private int index = 0;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Parser(List<Lexer.Token<?>> tokens) {
        this.tokens = tokens;

        logger.info("Parser initialized with {} tokens.", tokens.size());
    }

    public void setTokens(List<Lexer.Token<?>> newTokens) {
        this.index = 0;
        this.tokens = newTokens;
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

    public void retreat() {
        if (index > 0) {
            index--;
            logger.debug("retreat: New index is {}", index);
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
                    leftOperand = entry.getValue().parse(leftOperand, parseExpression());
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
        if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.SQUARE_LEFT)) {
            List<SafeObject> elements = new ArrayList<>();
            Class<?> elementType = null;
            advance();

            while (hasMoreTokens() && !ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.SQUARE_RIGHT)) {
                Object element = parseExpression();

                if (elementType == null) {
                    elementType = element.getClass();
                } else if (!elementType.equals(element.getClass())) {
                    throw new IllegalArgumentException("List elements must all be of the same type.");
                }

                elements.add(new SafeObject(element));

                if (getCurrentToken().type == Lexer.TokenType.COMMA) {
                    advance();
                } else if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.SQUARE_RIGHT)) {
                    throw new IllegalArgumentException("Expected comma or closing bracket in list.");
                }
            }

            if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.SQUARE_RIGHT)) {
                throw new IllegalArgumentException("Expected closing bracket for list.");
            }
            advance();

            logger.debug("Parsed list with elements: {}", elements);
            return elements;
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
         } else if (getCurrentToken().type == Lexer.TokenType.KEYWORD) {
             Object value = Interpreter.keywordManager.keywordHandlers.get(getCurrentToken().getAs(String.class)).execute(this);
             advance();
             return value;
         } else if (getCurrentToken().type == Lexer.TokenType.IDENTIFIER) {
            String identifier = getCurrentToken().getAs(String.class);
            advance();

            if (getCurrentToken().type == Lexer.TokenType.OPERATOR && getCurrentToken().getAs(String.class).equals("=")) {
                logger.debug("Assignment found for identifier: {}", identifier);
                advance();
                Object value = parseExpression();
                interpreter.setVariableValue(identifier, value);
                return new Assignment(identifier, value);
            }
            if (Interpreter.functionManager.functionHandlers.containsKey(identifier) && ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_LEFT)) {
                logger.debug("Function call found for identifier: {}", identifier);
                return Interpreter.functionManager.functionHandlers.get(identifier).execute(parseFunctionArguments());
            }

            return interpreter.getVariableValue(identifier);
        }
        Object value = getCurrentToken().getAs(Object.class);
        advance();
        return value;
    }


    private SafeObject[] parseFunctionArguments() {
        logger.debug("Parsing function arguments.");
        advance();
        List<SafeObject> args = new ArrayList<>();
        int parenCount = 0;
        boolean initial = true;

        while (initial || (parenCount > 0 && hasMoreTokens())) {
            initial = false;
            if (getCurrentToken().type == Lexer.TokenType.PARENTHESIS) {
                if (getCurrentToken().getAs(ParenthesisTokenHandler.ParenthesisType.class) == ParenthesisTokenHandler.ParenthesisType.ROUND_LEFT) {
                    parenCount++;
                } else if (getCurrentToken().getAs(ParenthesisTokenHandler.ParenthesisType.class) == ParenthesisTokenHandler.ParenthesisType.ROUND_RIGHT) {
                    parenCount--;
                } else {
                    logger.error("Unexpected parenthesis type at index {}: {}", index, getCurrentToken().getAs(ParenthesisTokenHandler.ParenthesisType.class));
                    throw new IllegalArgumentException("Unexpected parenthesis at index " + index);
                }
            } else if (getCurrentToken().type == Lexer.TokenType.COMMA && parenCount == 1) {
                advance();
                continue;
            }

            args.add(new SafeObject(parseExpression()));
        }

        if (parenCount == 0) {
            advance();
        } else {
            logger.error("Expected closing parenthesis for function arguments.");
            throw new IllegalArgumentException("Expected closing parenthesis.");
        }

        logger.debug("Finished parsing function arguments: {}.", args);
        return args.toArray(new SafeObject[0]);
    }


    public record Assignment(String variableName, Object value) { }
}
