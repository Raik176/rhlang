package org.rhm.keywords;

import org.rhm.Parser;
import org.rhm.tokens.ParenthesisTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhileKeywordHandler extends KeywordManager.KeywordHandler {
    private static final Logger logger = LoggerFactory.getLogger(WhileKeywordHandler.class);

    @Override
    public Object execute(Parser parser) {
        parser.advance();
        int index = parser.getIndex();

        while (getCondition(parser) instanceof Boolean b && b) {
            parseWhileBlock(parser);
            parser.setIndex(index);
        }
        skipWhileBlock(parser);

        return null;
    }

    private Object getCondition(Parser parser) {
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
        return condition;
    }

    private void parseWhileBlock(Parser parser) {
        validKeyword(parser, (parenCount) -> {
            while (parser.hasMoreTokens() && parenCount > 0) {
                if (ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_RIGHT)) {
                    parenCount--;
                }
                parser.parseExpression();
            }
        });
    }

    private void skipWhileBlock(Parser parser) {
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
