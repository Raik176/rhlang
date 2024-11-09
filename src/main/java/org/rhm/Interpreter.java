package org.rhm;

import java.util.HashMap;

public class Interpreter {
    private final Parser parser;
    private final HashMap<String,Object> variables;

    public Interpreter(Parser parser) {
        this.parser = parser;
        this.parser.interpreter = this;
        this.variables = new HashMap<>();
    }

    public void interpret() {
        while (parser.hasMoreTokens()) {
            Object result = parser.parseExpression();
            if (result instanceof Parser.Assignment assignment) {
                System.out.println("Assigned variable (" + assignment.variableName() + ") to [" + assignment.value() + "]");
                variables.put(assignment.variableName(), assignment.value());
            }
        }
    }

    public Object getVariableValue(String name) {
        Object value = variables.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Variable not defined: " + name);
        }
        return value;
    }
}