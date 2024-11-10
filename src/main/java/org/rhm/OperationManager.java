package org.rhm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class OperationManager {
    private static final Logger logger = LoggerFactory.getLogger(OperationManager.class);
    public final LinkedHashMap<String, OperatorHandler> operatorHandlers = new LinkedHashMap<>();

    public OperationManager() {
        operatorHandlers.put("+", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '+' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand + (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return left + right;
            }
        });

        operatorHandlers.put("-", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '-' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand - (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return left - right;
            }
        });

        operatorHandlers.put("*", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '*' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand * (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return left * right;
            }
        });

        operatorHandlers.put("//", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '//' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand / (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return Math.round(left) / Math.round(right);
            }
        });
        operatorHandlers.put("/", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '/' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand / (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return left / right;
            }
        });

        operatorHandlers.put("%", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '%' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer && rightOperand instanceof Integer) {
                return (Integer) leftOperand % (Integer) rightOperand;
            } else {
                float left = toFloat(leftOperand);
                float right = toFloat(rightOperand);
                return left % right;
            }
        });

        operatorHandlers.put("^", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '^' with operands: {} and {}", leftOperand, rightOperand);
            float left = toFloat(leftOperand);
            float right = toFloat(rightOperand);
            return (float) Math.pow(left, right);
        });

        operatorHandlers.put("=", (leftOperand, rightOperand) -> {
            logger.debug("Handling assignment operator '=' with operands: {} and {}", leftOperand, rightOperand);
            return new Parser.Assignment(leftOperand.toString(), rightOperand);
        });

        operatorHandlers.put("==", (leftOperand, rightOperand) -> {
            logger.debug("Handling equality operator '==' with operands: {} and {}", leftOperand, rightOperand);
            return leftOperand == rightOperand;
        });

        operatorHandlers.put(">=", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '>=' with operands: {} and {}", leftOperand, rightOperand);
            return toFloat(leftOperand) >= toFloat(rightOperand);
        });

        operatorHandlers.put("<=", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '<=' with operands: {} and {}", leftOperand, rightOperand);
            return toFloat(leftOperand) <= toFloat(rightOperand);
        });

        operatorHandlers.put(">", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '>' with operands: {} and {}", leftOperand, rightOperand);
            return toFloat(leftOperand) > toFloat(rightOperand);
        });

        operatorHandlers.put("<", (leftOperand, rightOperand) -> {
            logger.debug("Handling operator '<' with operands: {} and {}", leftOperand, rightOperand);
            return toFloat(leftOperand) < toFloat(rightOperand);
        });

        operatorHandlers.put("&&", (leftOperand, rightOperand) -> {
            logger.debug("Handling logical operator '&&' with operands: {} and {}", leftOperand, rightOperand);
            return (boolean) leftOperand && (boolean) rightOperand;
        });

        operatorHandlers.put("||", (leftOperand, rightOperand) -> {
            logger.debug("Handling logical operator '||' with operands: {} and {}", leftOperand, rightOperand);
            return (boolean) leftOperand || (boolean) rightOperand;
        });

        operatorHandlers.put("&", (leftOperand, rightOperand) -> {
            logger.debug("Handling bitwise operator '&' with operands: {} and {}", leftOperand, rightOperand);
            return (Integer) leftOperand & (Integer) rightOperand;
        });

        operatorHandlers.put("|", (leftOperand, rightOperand) -> {
            logger.debug("Handling bitwise operator '|' with operands: {} and {}", leftOperand, rightOperand);
            return (Integer) leftOperand | (Integer) rightOperand;
        });

        operatorHandlers.put("~", (leftOperand, rightOperand) -> {
            logger.debug("Handling bitwise operator '~' with operand: {}", leftOperand);
            return ~(Integer) leftOperand;
        });

        operatorHandlers.put("<<", (leftOperand, rightOperand) -> {
            logger.debug("Handling bitwise operator '<<' with operands: {} and {}", leftOperand, rightOperand);
            return (Integer) leftOperand << (Integer) rightOperand;
        });

        operatorHandlers.put(">>", (leftOperand, rightOperand) -> {
            logger.debug("Handling bitwise operator '>>' with operands: {} and {}", leftOperand, rightOperand);
            return (Integer) leftOperand >> (Integer) rightOperand;
        });

        operatorHandlers.put("+=", (leftOperand, rightOperand) -> {
            logger.debug("Handling compound operator '+=' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer) {
                return (Integer) leftOperand + (Integer) rightOperand;
            } else {
                return toFloat(leftOperand) + toFloat(rightOperand);
            }
        });

        operatorHandlers.put("-=", (leftOperand, rightOperand) -> {
            logger.debug("Handling compound operator '-=' with operands: {} and {}", leftOperand, rightOperand);
            if (leftOperand instanceof Integer) {
                return (Integer) leftOperand - (Integer) rightOperand;
            } else {
                return toFloat(leftOperand) - toFloat(rightOperand);
            }
        });

        operatorHandlers.put("^=", (leftOperand, rightOperand) -> {
            logger.debug("Handling compound operator '^=' with operands: {} and {}", leftOperand, rightOperand);
            return Math.pow(toFloat(leftOperand), toFloat(rightOperand));
        });

        operatorHandlers.put("%=", (leftOperand, rightOperand) -> {
            logger.debug("Handling compound operator '%=' with operands: {} and {}", leftOperand, rightOperand);
            return (Integer) leftOperand % (Integer) rightOperand;
        });
    }

    private float toFloat(Object operand) {
        logger.debug("Converting operand {} to float", operand);
        if (operand instanceof Integer) {
            return (float) (Integer) operand; // Convert Integer to Float
        } else if (operand instanceof Float) {
            return (Float) operand; // No conversion needed if it's already a Float
        } else {
            logger.error("Unsupported operand type: {}", operand.getClass());
            throw new IllegalArgumentException("Unsupported operand type: " + operand.getClass());
        }
    }

    public interface OperatorHandler {
        Object parse(Object leftOperand, Object rightOperand);
    }
}
