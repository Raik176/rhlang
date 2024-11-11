package org.rhm.keywords;

import org.rhm.Interpreter;
import org.rhm.Lexer;
import org.rhm.Parser;
import org.rhm.SafeObject;
import org.rhm.tokens.ParenthesisTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ForKeywordHandler extends KeywordManager.KeywordHandler {
    private final Logger logger = LoggerFactory.getLogger(ForKeywordHandler.class);

    @Override
    public Object execute(Parser parser) {
        parser.advance();
        if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_LEFT)) {
            logger.error("Expected opening round parenthesis for 'for' loop, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected opening round parenthesis for 'for' loop.");
        }
        parser.advance();

        if (parser.getCurrentToken().type != Lexer.TokenType.IDENTIFIER) {
            logger.error("Expected identifier for loop variable, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected identifier for loop variable.");
        }

        String varName = parser.getCurrentToken().getAs(String.class);
        parser.advance();

        if (parser.getCurrentToken().type != Lexer.TokenType.IDENTIFIER || !Objects.equals(parser.getCurrentToken().getAs(String.class), "in")) {
            logger.error("Expected 'in' keyword, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected 'in' keyword in 'for' loop.");
        }
        parser.advance();

        Object object = parser.parseExpression();
        if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_RIGHT)) {
            logger.error("Expected closing round parenthesis for 'for' loop, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected closing round parenthesis for 'for' loop.");
        }
        parser.advance();

        int index = parser.getIndex();

        if (object instanceof List<?> elements) {
            for (Object element : elements) {
                SafeObject safe = (SafeObject) element;
                Interpreter.instance.setVariableValue(varName, safe.getAs(Object.class));
                parseForBlock(parser);
                parser.setIndex(index);
            }
        } else {
            logger.error("Expected a list for 'for' loop, but found {}", object.getClass().getName());
            throw new IllegalArgumentException("Expected a list for 'for' loop.");
        }

        skipForBlock(parser);
        return null;
    }

    private void parseForBlock(Parser parser) {
        validKeyword(parser, (parenCount) -> {
            while (parser.hasMoreTokens() && parenCount > 0) {
                if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                    parenCount--;
                }
                parser.parseExpression();
            }
        });
    }

    private void skipForBlock(Parser parser) {
        validKeyword(parser, (parenCount) -> {
            while (parser.hasMoreTokens() && parenCount > 0) {
                if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                    parenCount--;
                }
                parser.advance();
            }
        });
    }
}