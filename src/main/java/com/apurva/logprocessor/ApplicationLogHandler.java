// File: src/main/java/com/apurva/logprocessor/ApplicationLogHandler.java
package com.apurva.logprocessor; // Make sure this matches your package

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationLogHandler implements LogHandler {

    // Regex to find "level=LEVEL_NAME"
    // Example: timestamp=2024-02-24T16:22:20Z level=INFO message="Scheduled
    // maintenance starting" host=webserver1
    // We want to capture "INFO"
    private static final Pattern APP_LOG_PATTERN = Pattern.compile("level=([A-Z]+)");
    private ApplicationLogStats stats;

    public ApplicationLogHandler() {
        this.stats = new ApplicationLogStats();
    }

    @Override
    public boolean canHandle(String logLine) {
        return APP_LOG_PATTERN.matcher(logLine).find();
    }

    @Override
    public void process(String logLine) {
        Matcher matcher = APP_LOG_PATTERN.matcher(logLine);
        if (matcher.find()) {
            String level = matcher.group(1); // group(1) is the captured level name
            stats.incrementCount(level);

        }
    }

    @Override
    public Object getAggregatedResults() {
        return stats.getSeverityCounts();
    }

    @Override
    public String getOutputFileName() {
        return "application.json";
    }

    @Override
    public void writeResultToFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // This makes the JSON output nicely formatted (indented)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Write the map directly as per the specified output format
        objectMapper.writeValue(new File(getOutputFileName()), getAggregatedResults());
        System.out.println("Successfully wrote " + getOutputFileName());
    }
}