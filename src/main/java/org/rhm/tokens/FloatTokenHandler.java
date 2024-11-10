package org.rhm.tokens;

import org.rhm.Lexer;

import java.util.concurrent.atomic.AtomicInteger;

public final class FloatTokenHandler implements TokenManager.TokenHandler {
    @Override
    public Lexer.Token<Float> parse(String source, AtomicInteger index) {
        StringBuilder number = new StringBuilder();

        if (index.get() < source.length() && source.charAt(index.get()) == '-') {
            number.append('-');
            index.getAndIncrement();
        }

        number.append(getString(source, index, (s, idx) -> Character.isDigit(s.charAt(index.get()))));

        if (index.get() < source.length() && source.charAt(index.get()) == '.') {
            number.append('.');
            index.getAndIncrement();

            number.append(getString(source, index, (s, idx) -> Character.isDigit(s.charAt(index.get()))));
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