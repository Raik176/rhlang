package org.rhm.operations;

import org.rhm.Parser;

import java.util.HashMap;
import java.util.Map;

public class OperationManager {
    private final Map<String, OperatorHandler> operatorHandlers = new HashMap<>();

    public OperationManager() {
        operatorHandlers.put("+", new OperatorHandler() {
            @Override
            public Object parse(Object leftOperand, Parser parser) {
                return toNumber(leftOperand) + toNumber(parser.parseMultiplyDivide());
            }
        });
        operatorHandlers.put("-", new OperatorHandler() {
            @Override
            public Object parse(Object leftOperand, Parser parser) {
                return toNumber(leftOperand) - toNumber(parser.parseMultiplyDivide());
            }
        });
        operatorHandlers.put("*", new OperatorHandler() {
            @Override
            public Object parse(Object leftOperand, Parser parser) {
                return toNumber(leftOperand) + toNumber(parser.parseMultiplyDivide());
            }
        });
        operatorHandlers.put("/", new DivideOperatorHandler());
        operatorHandlers.put("%", new ModulusOperatorHandler());
        operatorHandlers.put("^", new PowerOperatorHandler());
    }

    public Object applyOperator(String operator, Object leftOperand, Parser parser) {
        OperatorHandler handler = operatorHandlers.get(operator);
        if (handler != null) {
            return handler.parse(leftOperand, parser);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
}
