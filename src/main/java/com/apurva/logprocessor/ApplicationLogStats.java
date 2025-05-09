// File: src/main/java/com/apurva/logprocessor/ApplicationLogStats.java
package com.apurva.logprocessor; // Make sure this matches your package

import java.util.HashMap;
import java.util.Map;

public class ApplicationLogStats {
    // Using a Map to store counts like {"ERROR": 2, "INFO": 3}
    // The problem description shows the direct map as the JSON output for
    // application logs.
    private Map<String, Integer> severityCounts;

    public ApplicationLogStats() {
        this.severityCounts = new HashMap<>();
        // Initialize with known levels to ensure they appear in JSON even if count is
        // 0,
        // as per the example output format.
        this.severityCounts.put("ERROR", 0);
        this.severityCounts.put("INFO", 0);
        this.severityCounts.put("DEBUG", 0);
        this.severityCounts.put("WARNING", 0);
    }

    public void incrementCount(String level) {
        // Ensure level is one of the predefined keys if necessary, or handle new
        // levels.
        // For simplicity, we'll assume 'level' will be one of the keys we initialized.
        // If a new level comes that wasn't pre-initialized, getOrDefault will add it.
        this.severityCounts.put(level.toUpperCase(), this.severityCounts.getOrDefault(level.toUpperCase(), 0) + 1);
    }

    // This getter is important for Jackson (the JSON library) to serialize this
    // object.
    public Map<String, Integer> getSeverityCounts() {
        return severityCounts;
    }

    // Optional: A setter might be useful for testing or other scenarios.
    public void setSeverityCounts(Map<String, Integer> severityCounts) {
        this.severityCounts = severityCounts;
    }
}