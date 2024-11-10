package org.rhm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeObject {
    private static final Logger logger = LoggerFactory.getLogger(SafeObject.class); // SLF4J Logger
    private final Object value;

    public SafeObject(Object value) {
        this.value = value;
    }

    public <S> S getAs(Class<S> clazz) {
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        } else {
            logger.error("Failed to cast token value: Expected {}, but found {}",
                    clazz.getSimpleName(), (value == null ? "null" : value.getClass().getSimpleName())); // Use SLF4J logging
            return null;
        }
    }

    public Class<?> getValueClass() {
        return value.getClass();
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
}