package org.rhm.tokens;

import org.rhm.Lexer;

import java.util.concurrent.atomic.AtomicInteger;

public final class IntegerTokenHandler implements TokenManager.TokenHandler {
    @Override
    public Lexer.Token<Integer> parse(String source, AtomicInteger index) {
        StringBuilder number = new StringBuilder();

        if (index.get() < source.length() && source.charAt(index.get()) == '-') {
            number.append('-');
            index.getAndIncrement();
        }

        number.append(getString(source, index, (s, idx) -> Character.isDigit(s.charAt(index.get()))));

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