package com.apurva.logprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApmLogHandler implements LogHandler {
    // Regex to find "metric=METRIC_NAME" and "value=NUMBER"
    // Example: timestamp=2024-02-24T16:22:15Z metric=cpu_usage_percent
    // host=webserver1 value=72
    // Group 1: metric name (e.g., cpu_usage_percent)
    // Group 2: value (e.g., 72)
    private static final Pattern APM_LOG_PATTERN = Pattern.compile("metric=([\\w_.-]+).*value=(\\d+(\\.\\d+)?)");

    // Stores ApmMetricDetail objects, keyed by the metric name (String)
    private Map<String, ApmMetricDetail> metricsData;

    public ApmLogHandler() {
        this.metricsData = new HashMap<>();
    }

    @Override
    public boolean canHandle(String logLine) {
        // Check if the log line matches the APM log pattern
        return APM_LOG_PATTERN.matcher(logLine).find();
    }

    @Override
    public void process(String logLine) {
        Matcher matcher = APM_LOG_PATTERN.matcher(logLine);
        if (matcher.find()) {
            String metricName = matcher.group(1);
            double value = Double.parseDouble(matcher.group(2)); // group(2) captures the full number

            // Get the ApmMetricDetail for this metricName.
            // If it doesn't exist yet, computeIfAbsent will create a new one and put it in
            // the map.
            ApmMetricDetail detail = this.metricsData.computeIfAbsent(metricName, k -> new ApmMetricDetail());
            detail.addValue(value);
        }
    }

    @Override
    public Object getAggregatedResults() {
        // Before returning the results, ensure all calculations are done.
        for (ApmMetricDetail detail : this.metricsData.values()) {
            detail.calculateAggregates();
        }
        return this.metricsData; // Jackson will serialize this Map<String, ApmMetricDetail>
    }

    @Override
    public String getOutputFileName() {
        return "apm.json";
    }

    @Override
    public void writeResultToFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Makes JSON pretty
        objectMapper.writeValue(new File(getOutputFileName()), getAggregatedResults());
        System.out.println("Successfully wrote " + getOutputFileName());
    }
}