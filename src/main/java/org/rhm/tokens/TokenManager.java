package org.rhm.tokens;

import org.rhm.Interpreter;
import org.rhm.Lexer;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class TokenManager {
    public final EnumMap<Lexer.TokenType, TokenHandler> tokenHandlers = new EnumMap<>(Lexer.TokenType.class);


    public TokenManager() {
        tokenHandlers.put(Lexer.TokenType.FLOAT, new FloatTokenHandler());
        tokenHandlers.put(Lexer.TokenType.INTEGER, new IntegerTokenHandler());
        tokenHandlers.put(Lexer.TokenType.OPERATOR, new OperatorTokenHandler());
        tokenHandlers.put(Lexer.TokenType.PARENTHESIS, new ParenthesisTokenHandler());
        tokenHandlers.put(Lexer.TokenType.STRING, new StringTokenHandler());
        tokenHandlers.put(Lexer.TokenType.BOOLEAN, new BooleanTokenHandler());
        tokenHandlers.put(Lexer.TokenType.IDENTIFIER, new IdentifierTokenHandler());
        tokenHandlers.put(Lexer.TokenType.COMMA, new SingleTokenHandler(',', Lexer.TokenType.COMMA));
        tokenHandlers.put(Lexer.TokenType.SEMICOLON, new SingleTokenHandler(';', Lexer.TokenType.SEMICOLON));
        tokenHandlers.put(Lexer.TokenType.COMMENT, new CommentTokenHandler());
        tokenHandlers.put(Lexer.TokenType.KEYWORD, new KeywordTokenHandler());
    }

    public interface TokenHandler {
        Lexer.Token<?> parse(String source, AtomicInteger index);
        boolean canHandle(String source, AtomicInteger index);

        default String getString(String source, AtomicInteger index, BiFunction<String, AtomicInteger, Boolean> condition) {
            StringBuilder string = new StringBuilder();
            while (index.get() < source.length() && condition.apply(source, index)) {
                string.append(source.charAt(index.get()));
                index.getAndIncrement();
            }
            return string.toString();
        }
    }
    public static class SingleTokenHandler implements TokenHandler {
        final char token;
        final Lexer.TokenType type;

        public SingleTokenHandler(char token, Lexer.TokenType type) {
            this.token = token;
            this.type = type;
        }

        @Override
        public Lexer.Token<Character> parse(String source, AtomicInteger index) {
            Character srcToken = source.charAt(index.get());
            index.getAndIncrement();
            return new Lexer.Token<>(type, srcToken);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return source.charAt(index.get()) == token;
        }
    }

    public static class KeywordTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            String keyword = getString(source, index, this::canHandle);
            return new Lexer.Token<>(Lexer.TokenType.KEYWORD, keyword);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            for (String keyword : Interpreter.keywordManager.keywordHandlers.keySet()) {
                if (index.get() + keyword.length() <= source.length()) {
                    String subSequence = source.substring(index.get(), index.get() + keyword.length());
                    if (subSequence.equals(keyword)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    public static class BooleanTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<Boolean> parse(String source, AtomicInteger index) {
            boolean bool = false;
            if (source.startsWith("true", index.get())) {
                bool = true;
                index.addAndGet(4);
            } else if (source.startsWith("false", index.get())) {
                index.addAndGet(5);
            }

            return new Lexer.Token<>(Lexer.TokenType.BOOLEAN, bool);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return source.startsWith("true", index.get()) || source.startsWith("false", index.get());
        }
    }

    public static class CommentTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            StringBuilder comment = new StringBuilder();
            char currentChar = source.charAt(index.get());

            if (currentChar == '~') {
                index.getAndIncrement();

                if (index.get() < source.length() && source.charAt(index.get()) == '*') {
                    index.getAndIncrement();
                    while (index.get() < source.length() && !(source.charAt(index.get()) == '*' && index.get() + 1 < source.length() && source.charAt(index.get() + 1) == '~')) {
                        comment.append(source.charAt(index.get()));
                        index.getAndIncrement();
                    }

                    if (index.get() + 1 < source.length() && source.charAt(index.get()) == '*' && source.charAt(index.get() + 1) == '~') {
                        index.getAndIncrement();
                        index.getAndIncrement();
                    }
                } else {
                    while (index.get() < source.length() && source.charAt(index.get()) != '\n') {
                        comment.append(source.charAt(index.get()));
                        index.getAndIncrement();
                    }
                }
            }

            return new Lexer.Token<>(Lexer.TokenType.COMMENT, comment.toString().trim());
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            char currentChar = source.charAt(index.get());
            return currentChar == '~';
        }
    }

    public static class OperatorTokenHandler implements TokenHandler {
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            return new Lexer.Token<>(Lexer.TokenType.OPERATOR, getString(source, index, this::canHandle));
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            char currentChar = source.charAt(index.get());

            for (String operator : Interpreter.operationManager.operatorHandlers.keySet()) {
                if (operator.charAt(0) == currentChar) {
                    if (index.get() + operator.length() <= source.length()) {
                        String subSequence = source.substring(index.get(), index.get() + operator.length());
                        if (subSequence.equals(operator)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public static class StringTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            index.getAndIncrement();
            String stringValue = getString(source, index, (s, idx) -> s.charAt(idx.get()) != '"');
            index.getAndIncrement();
            return new Lexer.Token<>(Lexer.TokenType.STRING, stringValue);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return source.charAt(index.get()) == '"';
        }
    }

    public static class IdentifierTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            String identifier = getString(source, index, (s, idx) ->
                    (Character.isLetterOrDigit(source.charAt(index.get())) || source.charAt(index.get()) == '_'));
            return new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, identifier);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return Character.isLetter(source.charAt(index.get())) || source.charAt(index.get()) == '_';
        }
    }
}
