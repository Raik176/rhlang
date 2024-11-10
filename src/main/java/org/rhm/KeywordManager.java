package org.rhm;

import java.util.HashMap;
import java.util.Map;

public class KeywordManager {
    public final Map<String, KeywordHandler> keywordHandlers = new HashMap<>();

    public interface KeywordHandler {
        Object execute(SafeObject[] args);
    }

    public KeywordManager() {
        keywordHandlers.put("if", args -> {
            return null;
        });
    }
}
