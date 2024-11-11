package org.rhm;

import org.rhm.keywords.KeywordManager;
import org.rhm.tokens.TokenManager;
import org.rhm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

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
            Utils.profile(parser::parseExpression, "parseExpression");
        }
    }

    public Object getVariableValue(String name) {
        AtomicReference<Object> value = new AtomicReference<>();
        Utils.profile(() -> {
            value.set(variables.get(name));
            if (value.get() == null) {
                logger.error("Attempted to access an undefined variable: {}", name);
                throw new IllegalArgumentException("Variable not defined: " + name);
            }
            logger.debug("Retrieved value {} for variable {}", value, name);
        }, "getVariableValue");
        return value.get();
    }

    public void setVariableValue(String name, Object value) {
        Utils.profile(() -> {
            variables.put(name, value);
            logger.debug("Assigned value {} to variable {}", value, name);
        }, "setVariableValue");
    }
}
