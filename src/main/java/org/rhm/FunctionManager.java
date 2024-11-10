package org.rhm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FunctionManager {
    private static final Logger logger = LoggerFactory.getLogger(FunctionManager.class);

    public final Map<String, FunctionHandler> functionHandlers = new HashMap<>();

    public FunctionManager() {
        functionHandlers.put("println", args -> {
            if (args.length < 1) {
                logger.error("Invalid number of arguments for 'println'. Expected at least 1 but got {}", args.length);
                throw new IllegalArgumentException("println requires at least one argument.");
            }

            String formatString = args[0].getAs(String.class);

            Object[] formatArgs = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                formatArgs[i - 1] = args[i].getAs(Object.class);
            }

            logger.debug("Executing 'println' with format string: {} and arguments: {}", formatString, formatArgs);

            System.out.println(String.format(formatString.replaceAll("\\{}","%s"), formatArgs));

            return null;
        });

        functionHandlers.put("sqrt", args -> {
            if (args.length != 1) {
                logger.error("Invalid number of arguments for 'sqrt'. Expected 1 but got {}", args.length);
                throw new IllegalArgumentException("sqrt requires exactly one argument.");
            }
            double result = Math.sqrt(args[0].getAs(Number.class).doubleValue());
            logger.debug("Executing 'sqrt' with argument: {}. Result: {}", args[0], result); // Changed to debug
            return result;
        });

        functionHandlers.put("abs", args -> {
            if (args.length != 1) {
                logger.error("Invalid number of arguments for 'abs'. Expected 1 but got {}", args.length);
                throw new IllegalArgumentException("abs requires exactly one argument.");
            }
            Number value = args[0].getAs(Number.class);
            double result;
            if (value instanceof Integer) {
                result = Math.abs((Integer) value);
            } else if (value instanceof Float) {
                result = Math.abs((Float) value);
            } else {
                logger.error("Unsupported type for 'abs': {}", value.getClass().getSimpleName());
                throw new IllegalArgumentException("Unsupported type for abs: " + value.getClass().getSimpleName());
            }
            logger.debug("Executing 'abs' with argument: {}. Result: {}", value, result); // Changed to debug
            return result;
        });

        functionHandlers.put("max", args -> {
            if (args.length != 2) {
                logger.error("Invalid number of arguments for 'max'. Expected 2 but got {}", args.length);
                throw new IllegalArgumentException("max requires exactly two arguments.");
            }
            float result = Math.max(args[0].getAs(Number.class).floatValue(), args[1].getAs(Number.class).floatValue());
            logger.debug("Executing 'max' with arguments: {} and {}. Result: {}", args[0], args[1], result); // Changed to debug
            return result;
        });

        functionHandlers.put("min", args -> {
            if (args.length != 2) {
                logger.error("Invalid number of arguments for 'min'. Expected 2 but got {}", args.length);
                throw new IllegalArgumentException("min requires exactly two arguments.");
            }
            float result = Math.min(args[0].getAs(Number.class).floatValue(), args[1].getAs(Number.class).floatValue());
            logger.debug("Executing 'min' with arguments: {} and {}. Result: {}", args[0], args[1], result); // Changed to debug
            return result;
        });
    }

    public interface FunctionHandler {
        Object execute(SafeObject[] args);
    }
}