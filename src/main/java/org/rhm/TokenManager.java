package org.rhm;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenManager {
    public final LinkedHashMap<Lexer.TokenType, TokenHandler> tokenHandlers = new LinkedHashMap<>();

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
        tokenHandlers.put(Lexer.TokenType.KEYWORD, new TokenHandler() {
            private static final Set<String> KEYWORDS = Set.of("if", "else", "for", "while");

            @Override
            public Lexer.Token<String> parse(String source, AtomicInteger index) {
                StringBuilder keyword = new StringBuilder();

                while (index.get() < source.length() &&
                        (Character.isLetterOrDigit(source.charAt(index.get())) || source.charAt(index.get()) == '_')) {
                    keyword.append(source.charAt(index.get()));
                    index.getAndIncrement();
                }

                String keywordString = keyword.toString();

                if (KEYWORDS.contains(keywordString)) {
                    return new Lexer.Token<>(Lexer.TokenType.KEYWORD, keywordString);
                } else {
                    return null;
                }
            }

            @Override
            public boolean canHandle(String source, AtomicInteger index) {
                if (Character.isLetter(source.charAt(index.get())) || source.charAt(index.get()) == '_') {
                    StringBuilder keyword = new StringBuilder();
                    int startIndex = index.get();

                    while (index.get() < source.length() &&
                            (Character.isLetterOrDigit(source.charAt(index.get())) || source.charAt(index.get()) == '_')) {
                        keyword.append(source.charAt(index.get()));
                        index.getAndIncrement();
                    }

                    return KEYWORDS.contains(keyword.toString());
                }

                return false;
            }
        });
    }

    public interface TokenHandler {
        Lexer.Token<?> parse(String source, AtomicInteger index);
        boolean canHandle(String source, AtomicInteger index);
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

    public static class IntegerTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<Integer> parse(String source, AtomicInteger index) {
            StringBuilder number = new StringBuilder();

            if (index.get() < source.length() && source.charAt(index.get()) == '-') {
                number.append('-');
                index.getAndIncrement();
            }

            while (index.get() < source.length() && Character.isDigit(source.charAt(index.get()))) {
                number.append(source.charAt(index.get()));
                index.getAndIncrement();
            }

            if (number.isEmpty() || (number.length() == 1 && number.charAt(0) == '-')) {
                throw new NumberFormatException("Invalid integer format: '" + number + "'");
            }

            return new Lexer.Token<>(Lexer.TokenType.INTEGER, Integer.parseInt(number.toString()));
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            int startIndex = index.get();

            if (source.charAt(startIndex) == '-') {
                startIndex++;
            }

            return startIndex < source.length() && Character.isDigit(source.charAt(startIndex));
        }
    }


    public static class FloatTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<Float> parse(String source, AtomicInteger index) {
            StringBuilder number = new StringBuilder();

            if (index.get() < source.length() && source.charAt(index.get()) == '-') {
                number.append('-');
                index.getAndIncrement();
            }

            while (index.get() < source.length() && Character.isDigit(source.charAt(index.get()))) {
                number.append(source.charAt(index.get()));
                index.getAndIncrement();
            }

            if (index.get() < source.length() && source.charAt(index.get()) == '.') {
                number.append('.');
                index.getAndIncrement();

                while (index.get() < source.length() && Character.isDigit(source.charAt(index.get()))) {
                    number.append(source.charAt(index.get()));
                    index.getAndIncrement();
                }
            }

            if (index.get() < source.length() && source.charAt(index.get()) == 'f') {
                index.getAndIncrement();
            }

            try {
                return new Lexer.Token<>(Lexer.TokenType.FLOAT, Float.parseFloat(number.toString()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid float format: '" + number + "'");
            }
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            int startIndex = index.get();
            if (source.charAt(startIndex) == '-') {
                startIndex++;
            }

            return startIndex < source.length() && Character.isDigit(source.charAt(startIndex));
        }
    }

    public static class OperatorTokenHandler implements TokenHandler {
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            StringBuilder operator = new StringBuilder();
            char currentChar = source.charAt(index.get());

            while (index.get() < source.length() && canHandle(source, index)) {
                operator.append(currentChar);
                index.getAndIncrement();
                if (index.get() < source.length()) {
                    currentChar = source.charAt(index.get());
                }
            }

            return new Lexer.Token<>(Lexer.TokenType.OPERATOR, operator.toString());
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

    public static class ParenthesisTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<Character> parse(String source, AtomicInteger index) {
            char currentChar = source.charAt(index.get());
            index.getAndIncrement();
            return new Lexer.Token<>(Lexer.TokenType.PARENTHESIS, currentChar);
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            char currentChar = source.charAt(index.get());
            return currentChar == '(' || currentChar == ')';
        }
    }

    public static class StringTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            index.getAndIncrement();
            StringBuilder stringValue = new StringBuilder();
            while (index.get() < source.length() && source.charAt(index.get()) != '"') {
                stringValue.append(source.charAt(index.get()));
                index.getAndIncrement();
            }
            index.getAndIncrement();
            return new Lexer.Token<>(Lexer.TokenType.STRING, stringValue.toString());
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return source.charAt(index.get()) == '"';
        }
    }

    public static class IdentifierTokenHandler implements TokenHandler {
        @Override
        public Lexer.Token<String> parse(String source, AtomicInteger index) {
            StringBuilder identifier = new StringBuilder();
            while (index.get() < source.length() && (Character.isLetterOrDigit(source.charAt(index.get())) || source.charAt(index.get()) == '_')) {
                identifier.append(source.charAt(index.get()));
                index.getAndIncrement();
            }
            return new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, identifier.toString());
        }

        @Override
        public boolean canHandle(String source, AtomicInteger index) {
            return Character.isLetter(source.charAt(index.get())) || source.charAt(index.get()) == '_';
        }
    }
}
