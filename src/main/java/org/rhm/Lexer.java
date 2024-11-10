package org.rhm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Lexer {
    private static final Logger logger = LoggerFactory.getLogger(Lexer.class);
    public static final Token<?> EOF_TOKEN = new Token<>(TokenType.EOF, null);

    public enum TokenType {
        KEYWORD,
        COMMENT,
        COMMA,
        IDENTIFIER,
        INTEGER,
        BOOLEAN,
        FLOAT,
        OPERATOR,
        STRING,
        PARENTHESIS,
        SEMICOLON,
        EOF
    }

    public static class Token<T> {
        public TokenType type;
        private final SafeObject value;

        public Token(TokenType type, T value) {
            this.type = type;
            this.value = new SafeObject(value);
        }

        @Override
        public String toString() {
            return "Token{" +
                    "type=" + type +
                    ", value=" + value + (value.isNull() ? "" : (" [" + value.getValueClass().getSimpleName() + "]")) +
                    '}';
        }

        public <S> S getAs(Class<S> clazz) {
            return value.isNull() ? null : value.getAs(clazz);
        }
    }

    public List<Token<?>> tokenize(String source) {
        List<Token<?>> tokens = new ArrayList<>();
        int length = source.length();
        AtomicInteger i = new AtomicInteger(0);

        while (i.get() < length) {
            char currentChar = source.charAt(i.get());

            if (Character.isWhitespace(currentChar)) {
                i.set(i.get() + 1);
                continue;
            }

            boolean tokenHandled = false;

            for (TokenManager.TokenHandler handler : Interpreter.tokenManager.tokenHandlers.values()) {
                if (handler.canHandle(source, i)) {
                    Token<?> token = handler.parse(source, i);
                    tokens.add(token);
                    tokenHandled = true;
                    logger.debug("Parsed token: {}", token);
                    break;
                }
            }

            if (!tokenHandled) {
                logger.error("Invalid character at index {}: {}", i.get(), currentChar);
                throw new IllegalArgumentException("Invalid character at index " + i.get() + ": " + currentChar);
            }
        }

        tokens.add(EOF_TOKEN);
        return tokens;
    }
}
