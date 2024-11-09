package org.rhm.operations;

import org.rhm.Parser;

public interface OperatorHandler {
    Object parse(Object leftOperand, Parser parser);

    default float toNumber(Object value) {
        if (value instanceof Integer) {
            return (float) (int) value;  // Convert int to float
        } else if (value instanceof Float) {
            return (float) value;
        } else {
            throw new IllegalArgumentException("Invalid type for arithmetic operation: " + value);
        }
    }
}