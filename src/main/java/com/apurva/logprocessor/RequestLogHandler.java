// File: src/main/java/com/apurva/logprocessor/RequestLogHandler.java
package com.apurva.logprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLogHandler implements LogHandler {

    // Example: timestamp=... request_method=POST request_url="/api/update"
    // response_status=202 response_time_ms=200 ...
    // Groups: 1=method, 2=url, 3=status, 4=time
    private static final Pattern REQUEST_LOG_PATTERN = Pattern.compile(
            "request_method=([A-Z]+)\\s+" +
                    "request_url=\"([^\"]+)\"\\s+" +
                    "response_status=(\\d{3})\\s+" +
                    "response_time_ms=(\\d+)");

    // Key: API Route (String), Value: Raw data collected for that route
    private Map<String, RequestRouteData> rawDataByRoute;

    public RequestLogHandler() {
        this.rawDataByRoute = new HashMap<>();
    }

    @Override
    public boolean canHandle(String logLine) {
        return REQUEST_LOG_PATTERN.matcher(logLine).find();
    }

    @Override
    public void process(String logLine) {
        Matcher matcher = REQUEST_LOG_PATTERN.matcher(logLine);
        if (matcher.find()) {
            // String method = matcher.group(1); // We don't need the method for current
            // aggregations
            String requestUrl = matcher.group(2);
            int statusCode = Integer.parseInt(matcher.group(3));
            long responseTimeMs = Long.parseLong(matcher.group(4));

            RequestRouteData routeData = this.rawDataByRoute.computeIfAbsent(requestUrl, k -> new RequestRouteData());
            routeData.addResponseTime(responseTimeMs);
            routeData.addStatusCode(statusCode);
        }
    }

    @Override
    public Object getAggregatedResults() {
        Map<String, RequestRouteStats> aggregatedStatsByRoute = new HashMap<>();

        for (Map.Entry<String, RequestRouteData> entry : this.rawDataByRoute.entrySet()) {
            String route = entry.getKey();
            RequestRouteData rawData = entry.getValue();
            List<Long> times = new ArrayList<>(rawData.getResponseTimesMs()); // Make a copy for sorting
            List<Integer> codes = rawData.getStatusCodes();

            ResponseTimeStats timeStats;
            if (times.isEmpty()) {
                timeStats = new ResponseTimeStats(0, 0, 0, 0, 0, 0); // Default for no times
            } else {
                Collections.sort(times);
                long minTime = times.get(0);
                long maxTime = times.get(times.size() - 1);
                long p50 = calculatePercentile(times, 50);
                long p90 = calculatePercentile(times, 90);
                long p95 = calculatePercentile(times, 95);
                long p99 = calculatePercentile(times, 99);

                timeStats = new ResponseTimeStats(minTime, p50, p90, p95, p99, maxTime);
            }

            StatusCodeCounts codeCounts = new StatusCodeCounts();
            for (int code : codes) {
                if (code >= 200 && code < 300) {
                    codeCounts.increment2XX();
                } else if (code >= 400 && code < 500) {
                    codeCounts.increment4XX();
                } else if (code >= 500 && code < 600) {
                    codeCounts.increment5XX();
                }
            }
            aggregatedStatsByRoute.put(route, new RequestRouteStats(timeStats, codeCounts));
        }
        return aggregatedStatsByRoute;
    }

    // Helper method for percentile calculation
    private long calculatePercentile(List<Long> sortedValues, double percentile) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return 0;
        }
        // N * P / 100
        // For 0-based index: ceil(percentile / 100.0 * N) - 1
        int N = sortedValues.size();
        int index = (int) Math.ceil((percentile / 100.0) * N) - 1;

        // Ensure index is within bounds
        if (index < 0)
            index = 0;
        if (index >= N)
            index = N - 1;

        return sortedValues.get(index);
    }

    @Override
    public String getOutputFileName() {
        return "request.json";
    }

    @Override
    public void writeResultToFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new File(getOutputFileName()), getAggregatedResults());
        System.out.println("Successfully wrote " + getOutputFileName());
    }
}