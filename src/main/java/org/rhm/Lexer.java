package org.rhm;

import org.rhm.tokens.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Lexer {
    private static final Logger logger = LoggerFactory.getLogger(Lexer.class);
    public static final Token<?> EOF_TOKEN = new Token<>(TokenType.EOF, null);

    public enum TokenType {
        INTEGER,
        FLOAT,
        OPERATOR,
        PARENTHESIS,
        STRING,
        BOOLEAN,
        KEYWORD,
        IDENTIFIER,
        COMMA,
        SEMICOLON,
        COMMENT,
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
            return "Token{" + "type=" + type + ", value=" + value + (value.isNull() ? "" : " [" + value.getValueClass().getSimpleName() + "]") + '}';
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
                i.incrementAndGet();
                continue;
            }

            Token<?> token = handleToken(source, i);
            if (token != null) {
                tokens.add(token);
                logger.debug("Parsed token: {}", token);
            } else {
                logger.error("Invalid character at index {}: {}", i.get(), currentChar);
                throw new IllegalArgumentException("Invalid character at index " + i.get() + ": " + currentChar);
            }
        }

        tokens.add(EOF_TOKEN);
        return tokens;
    }

    private Token<?> handleToken(String source, AtomicInteger index) {
        for (TokenManager.TokenHandler handler : Interpreter.tokenManager.tokenHandlers.values()) {
            if (handler.canHandle(source, index)) {
                return handler.parse(source, index);
            }
        }
        return null;
    }
}