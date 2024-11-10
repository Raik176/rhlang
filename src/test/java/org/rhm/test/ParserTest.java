package org.rhm.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.rhm.Lexer;
import org.rhm.Parser;
import org.rhm.Interpreter;

import java.util.Arrays;
import java.util.List;

class ParserTest {

    private Parser parser;
    private Interpreter interpreter;

    @BeforeEach
    void setUp() {
        parser = new Parser(List.of());
        this.interpreter = new Interpreter(parser);
        parser.interpreter = interpreter;
    }

    @Test
    void testAssignment() {
        parser.setTokens(Arrays.asList(
                new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, "myVar"),
                new Lexer.Token<>(Lexer.TokenType.OPERATOR, "="),
                new Lexer.Token<>(Lexer.TokenType.INTEGER, 42)
        ));

        Object result = parser.parseExpression();

        assertInstanceOf(Parser.Assignment.class, result);
        Parser.Assignment assignment = (Parser.Assignment) result;

        assertEquals("myVar", assignment.variableName());
        assertEquals(42, assignment.value());
    }

    @Test
    void testParseVariableReference() {
        interpreter.setVariableValue("myVar", 42);

        parser.setTokens(List.of(new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, "myVar")));

        assertEquals(42, parser.parsePrimary());
    }

    @Test
    void testParseExpressionWithOperators() {
        interpreter.setVariableValue("a", 2);
        interpreter.setVariableValue("b", 4);

        Lexer.Token<?> identifierTokenA = new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, "a");
        Lexer.Token<?> plusToken = new Lexer.Token<>(Lexer.TokenType.OPERATOR, "+");
        Lexer.Token<?> identifierTokenB = new Lexer.Token<>(Lexer.TokenType.IDENTIFIER, "b");
        Lexer.Token<?> multiplyToken = new Lexer.Token<>(Lexer.TokenType.OPERATOR, "*");
        Lexer.Token<?> numberToken = new Lexer.Token<>(Lexer.TokenType.INTEGER, 2);

        interpreter.getVariableValue("a");
        interpreter.getVariableValue("b");

        parser.setTokens(Arrays.asList(identifierTokenA, plusToken, identifierTokenB, multiplyToken, numberToken));

        Object result = parser.parseExpression();

        assertEquals(10, result);
    }

    private void parseAll(Parser parser) {
        while (parser.hasMoreTokens()) {
            parser.parseExpression();
        }
    }
}
