package org.rhm;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public enum TokenType { KEYWORD, IDENTIFIER, INTEGER, FLOAT, OPERATOR, STRING, PARENTHESIS, EOF }

    public static class Token {
        public TokenType type;
        public String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        int length = source.length();
        int i = 0;

        while (i < length) {
            char currentChar = source.charAt(i);

            if (Character.isWhitespace(currentChar)) {
                i++;
            } else if (Character.isDigit(currentChar)) {
                StringBuilder number = new StringBuilder();
                while (i < length && Character.isDigit(source.charAt(i))) {
                    number.append(source.charAt(i));
                    i++;
                }

                if (i < length && source.charAt(i) == '.') {
                    number.append('.');
                    i++;

                    while (i < length && Character.isDigit(source.charAt(i))) {
                        number.append(source.charAt(i));
                        i++;
                    }

                    if (i < length && source.charAt(i) == 'f') {
                        i++;
                        tokens.add(new Token(TokenType.FLOAT, number.toString()));
                    } else {
                        tokens.add(new Token(TokenType.FLOAT, number.toString()));
                    }
                } else if (i < length && source.charAt(i) == 'f') {
                    i++;
                    tokens.add(new Token(TokenType.FLOAT, number.toString()));
                } else {
                    tokens.add(new Token(TokenType.INTEGER, number.toString()));
                }
            } else if (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/' || currentChar == '^' || currentChar == '=') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
                i++;
            } else if (currentChar == '(' || currentChar == ')') {
                tokens.add(new Token(TokenType.PARENTHESIS, String.valueOf(currentChar)));
                i++;
            } else if (currentChar == '"') {
                i++;
                StringBuilder stringValue = new StringBuilder();
                while (i < length && source.charAt(i) != '"') {
                    stringValue.append(source.charAt(i));
                    i++;
                }
                if (i < length && source.charAt(i) == '"') {
                    i++;
                }
                tokens.add(new Token(TokenType.STRING, stringValue.toString()));
            } else if (Character.isLetter(currentChar) || currentChar == '_') {
                StringBuilder identifier = new StringBuilder();
                while (i < length && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_')) {
                    identifier.append(source.charAt(i));
                    i++;
                }
                String identifierStr = identifier.toString();
                tokens.add(new Token(TokenType.IDENTIFIER, identifierStr));
            } else {
                throw new IllegalArgumentException("Invalid character: " + currentChar);
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
