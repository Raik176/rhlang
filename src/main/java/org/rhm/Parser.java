package org.rhm;

import org.rhm.Interpreter;
import org.rhm.Lexer;

import java.util.List;

public class Parser {
    public Interpreter interpreter;
    private List<Lexer.Token> tokens;
    private int index = 0;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
    }

    public boolean hasMoreTokens() {
        return index < tokens.size();
    }

    public Lexer.Token getCurrentToken() {
        if (hasMoreTokens()) {
            return tokens.get(index);
        }
        return null;
    }

    public void advance() {
        if (hasMoreTokens()) {
            index++;
        }
    }

    public Object parseExpression() {
        return parseAddSubtract(); // Start parsing with the lowest precedence (addition and subtraction)
    }

    private Object parseAddSubtract() {
        Object result = parseMultiplyDivide(); // Handle multiplication and division first

        while (hasMoreTokens() && (getCurrentToken().value.equals("+") || getCurrentToken().value.equals("-"))) {
            String operator = getCurrentToken().value;
            advance();
            Object rightOperand = parseMultiplyDivide(); // Evaluate the next term

            if (operator.equals("+")) {
                result = add(result, rightOperand);
            } else if (operator.equals("-")) {
                result = subtract(result, rightOperand);
            }
        }

        return result;
    }

    public Object parseMultiplyDivide() {
        Object result = parsePower(); // Handle exponentiation first

        while (hasMoreTokens() && (getCurrentToken().value.equals("*") || getCurrentToken().value.equals("/") || getCurrentToken().value.equals("%"))) {
            String operator = getCurrentToken().value;
            advance();
            Object rightOperand = parsePower(); // Evaluate the next term

            if (operator.equals("*")) {
                result = multiply(result, rightOperand);
            } else if (operator.equals("/")) {
                result = divide(result, rightOperand);
            } else if (operator.equals("%")) {
                result = modulus(result, rightOperand);
            }
        }

        return result;
    }

    private Object parsePower() {
        Object result = parsePrimary(); // Handle primary expressions (numbers, variables, parentheses)

        while (hasMoreTokens() && getCurrentToken().value.equals("^")) {
            advance();
            Object rightOperand = parsePrimary(); // Evaluate the exponentiation part
            result = Math.pow(toFloat(result), toFloat(rightOperand)); // Use Math.pow for exponentiation
        }

        return result;
    }

    private Object parsePrimary() {
        if (getCurrentToken().type == Lexer.TokenType.STRING) {
            String value = getCurrentToken().value;
            advance();
            return value;
        } else if (getCurrentToken().type == Lexer.TokenType.INTEGER) {
            int value = Integer.parseInt(getCurrentToken().value);
            advance();
            return value;
        } else if (getCurrentToken().type == Lexer.TokenType.FLOAT) {
            float value = Float.parseFloat(getCurrentToken().value);
            advance();
            return value;
        } else if (getCurrentToken().type == Lexer.TokenType.IDENTIFIER) {
            String identifier = getCurrentToken().value;
            advance();

            // Check for function calls
            if (getCurrentToken().type == Lexer.TokenType.PARENTHESIS && getCurrentToken().value.equals("(")) {
                advance();
                if (identifier.equals("println")) {
                    Object value = parseExpression(); // Evaluate the expression inside the parentheses
                    if (getCurrentToken().type == Lexer.TokenType.PARENTHESIS && getCurrentToken().value.equals(")")) {
                        advance();
                        System.out.println(value);
                        return null;
                    } else {
                        throw new IllegalArgumentException("Expected closing parenthesis for function call.");
                    }
                } else {
                    throw new IllegalArgumentException("Unknown function: " + identifier);
                }
            }

            // If not a function call, treat it as a variable
            return interpreter.getVariableValue(identifier);
        } else if (getCurrentToken().type == Lexer.TokenType.PARENTHESIS && getCurrentToken().value.equals("(")) {
            advance(); // Skip '('
            Object result = parseExpression(); // Recursively parse the inner expression
            if (getCurrentToken().type == Lexer.TokenType.PARENTHESIS && getCurrentToken().value.equals(")")) {
                advance(); // Skip ')'
            } else {
                throw new IllegalArgumentException("Expected closing parenthesis.");
            }
            return result;
        } else if (getCurrentToken().type == Lexer.TokenType.EOF) {
            System.exit(0);
            return null;
        } else {
            throw new IllegalArgumentException("Unexpected token: " + getCurrentToken().value);
        }
    }

    // Helper methods to handle float and int operations together
    private Object add(Object left, Object right) {
        float leftVal = toFloat(left);
        float rightVal = toFloat(right);
        return leftVal + rightVal;
    }

    private Object subtract(Object left, Object right) {
        float leftVal = toFloat(left);
        float rightVal = toFloat(right);
        return leftVal - rightVal;
    }

    private Object multiply(Object left, Object right) {
        float leftVal = toFloat(left);
        float rightVal = toFloat(right);
        return leftVal * rightVal;
    }

    private Object divide(Object left, Object right) {
        float leftVal = toFloat(left);
        float rightVal = toFloat(right);
        if (rightVal == 0) {
            throw new ArithmeticException("Division by zero.");
        }
        return leftVal / rightVal;
    }

    private Object modulus(Object left, Object right) {
        float leftVal = toFloat(left);
        float rightVal = toFloat(right);
        return leftVal % rightVal;
    }

    // Convert Object to float (handles both int and float)
    private float toFloat(Object value) {
        if (value instanceof Integer) {
            return (float) (int) value;
        } else if (value instanceof Float) {
            return (float) value;
        } else {
            throw new IllegalArgumentException("Invalid type for arithmetic operation: " + value);
        }
    }

    public record Assignment(String variableName, Object value) { }
}
