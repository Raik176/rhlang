package org.rhm.keywords;

import org.rhm.Parser;
import org.rhm.tokens.ParenthesisTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class KeywordManager {
    public final Map<String, KeywordHandler> keywordHandlers = new HashMap<>();

    public KeywordManager() {
        keywordHandlers.put("if", new IfKeywordHandler());
        keywordHandlers.put("while", new WhileKeywordHandler());
        keywordHandlers.put("for", new ForKeywordHandler());
    }

    public abstract static class KeywordHandler {
        protected Logger logger = LoggerFactory.getLogger(getClass());

        public abstract Object execute(Parser parser);
        protected void validKeyword(Parser parser, Consumer<Integer> callback) {
            if (!ParenthesisTokenHandler.ParenthesisType.isParenthesisOfType(parser.getCurrentToken(), ParenthesisTokenHandler.ParenthesisType.CURLY_LEFT)) {
                logger.error("Expected opening curly brace for while block, but found {}", parser.getCurrentToken());
                throw new IllegalArgumentException("Expected opening curly brace for block.");
            }

            parser.advance();
            int openingBraces = 1;
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
}
