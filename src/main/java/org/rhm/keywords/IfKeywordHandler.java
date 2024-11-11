package org.rhm.keywords;

import org.rhm.Parser;
import org.rhm.tokens.ParenthesisTokenHandler;

import java.util.function.Consumer;

public class IfKeywordHandler extends KeywordManager.KeywordHandler {
    @Override
    public Object execute(Parser parser) {
        parser.advance();
        if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_LEFT)) {
            logger.error("Expected opening round parenthesis for condition, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected opening round parenthesis for condition.");
        }

        parser.advance();
        Object condition = parser.parseExpression();

        if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.ROUND_RIGHT)) {
            logger.error("Expected closing round parenthesis for condition, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected closing round parenthesis for condition.");
        }

        parser.advance();

        if (condition instanceof Boolean b) {
            if (b) {
                parseIfBlock(parser);
            } else {
                skipIfBlock(parser);
            }
        } else {
            logger.error("Condition must evaluate to a boolean value, but got {}", condition.getClass().getSimpleName());
            throw new IllegalArgumentException("Condition must evaluate to a boolean value.");
        }

        return null;
    }

    private void parseIfBlock(Parser parser) {
        validKeyword(parser, (parenCount) -> {
            while (parser.hasMoreTokens() && parenCount > 0) {
                if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                    parenCount--;
                }
                parser.parseExpression();
            }
        });
    }

    private void skipIfBlock(Parser parser) {
        validKeyword(parser, (parenCount) -> {
            while (parser.hasMoreTokens() && parenCount > 0) {
                if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                    parenCount--;
                }
                parser.advance();
            }
        });
    }

    private void validIf(Parser parser, Consumer<Integer> callback) {
        if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_LEFT)) {
            logger.error("Expected opening curly brace for while block, but found {}", parser.getCurrentToken());
            throw new IllegalArgumentException("Expected opening curly brace for while block.");
        }

        parser.advance();
        int openingBraces = 0;
        int openBraces = 1;
        int position = parser.getIndex();

        while (parser.hasMoreTokens()) {
            if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_LEFT)) {
                openBraces++;
                openingBraces++;
            } else if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                openBraces--;
            }

            if (openBraces <= 0) {
                break;
            }

            parser.advance();
        }

        if (openBraces != 0) {
            logger.error("Unbalanced curly braces in while block.");
            throw new IllegalArgumentException("Unbalanced curly braces in while block.");
        } else {
            parser.setIndex(position);
            callback.accept(openingBraces);
        }
    }
}
