package org.rhm;

import org.rhm.tokens.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    public static final OperationManager operationManager = new OperationManager();
    public static final FunctionManager functionManager = new FunctionManager();
    public static final KeywordManager keywordManager = new KeywordManager();
    public static final TokenManager tokenManager = new TokenManager();
    public static Interpreter instance;
    private final Parser parser;
    private final HashMap<String, Object> variables;

    public Interpreter(Parser parser) {
        Interpreter.instance = this;
        this.parser = parser;
        this.parser.interpreter = this;
        this.variables = new HashMap<>();
    }

    public void interpret() {
        while (parser.hasMoreTokens()) {
            Object result = parser.parseExpression();
            if (result instanceof Parser.Assignment assignment) {
                String variableName = assignment.variableName();
                Object value = assignment.value();
                variables.put(variableName, value);
                logger.debug("Assigned value {} to variable {}", value, variableName);
            }
        }
    }

    public Object getVariableValue(String name) {
        Object value = variables.get(name);
        if (value == null) {
            logger.error("Attempted to access an undefined variable: {}", name);
            throw new IllegalArgumentException("Variable not defined: " + name);
        }
        logger.debug("Retrieved value {} for variable {}", value, name);
        return value;
    }
}