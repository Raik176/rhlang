package org.rhm.tokens;

import org.rhm.Lexer;

import java.util.concurrent.atomic.AtomicInteger;

public final class ParenthesisTokenHandler implements TokenManager.TokenHandler {

    @Override
    public Lexer.Token<ParenthesisType> parse(String source, AtomicInteger index) {
        char currentChar = source.charAt(index.get());
        index.getAndIncrement();
        return new Lexer.Token<>(Lexer.TokenType.PARENTHESIS, ParenthesisType.fromChar(currentChar));
    }

    @Override
    public boolean canHandle(String source, AtomicInteger index) {
        char currentChar = source.charAt(index.get());
        try {
            ParenthesisType.fromChar(currentChar);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public enum ParenthesisType {
        ROUND_LEFT('('),
        CURLY_LEFT('{'),
        SQUARE_LEFT('['),
        ROUND_RIGHT(')'),
        CURLY_RIGHT('}'),
        SQUARE_RIGHT(']');

        private final char symbol;

        ParenthesisType(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        public static ParenthesisType fromChar(char c) {
            for (ParenthesisType type : values()) {
                if (type.getSymbol() == c) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid parenthesis character: " + c);
        }

        public static boolean isParenthesisOfType(Lexer.Token<?> token, ParenthesisType type) {
            return token.type == Lexer.TokenType.PARENTHESIS &&
                    token.getAs(ParenthesisType.class) == type;
        }
    }
}