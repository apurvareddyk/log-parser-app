package com.apurva.logprocessor;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class ApmLogHandlerTest {

    @Test
    public void testCanHandle_ValidApmLogLine() {
        ApmLogHandler handler = new ApmLogHandler();
        String logLine = "timestamp=2024-02-24T16:22:15Z metric=cpu_usage_percent host=webserver1 value=72";
        assertTrue("Should correctly identify an APM log line", handler.canHandle(logLine));
    }

    @Test
    public void testCanHandle_ApplicationLogLine_ShouldReturnFalse() {
        ApmLogHandler handler = new ApmLogHandler();
        String logLine = "timestamp=2024-02-24T16:22:20Z level=INFO message=\"Test\"";
        assertFalse("Should not handle an application log line", handler.canHandle(logLine));
    }

    @Test
    public void testProcessAndGetAggregatedResults_SingleMetric() {
        ApmLogHandler handler = new ApmLogHandler();
        String metricName = "cpu_usage_percent";

        handler.process("timestamp=... metric=" + metricName + " host=... value=60");
        handler.process("timestamp=... metric=" + metricName + " host=... value=70");
        handler.process("timestamp=... metric=" + metricName + " host=... value=80");

        // Add a non-matching log to ensure it's ignored
        handler.process("timestamp=... level=INFO message=...");

        @SuppressWarnings("unchecked")
        Map<String, ApmMetricDetail> results = (Map<String, ApmMetricDetail>) handler.getAggregatedResults();

        assertNotNull("Results map should not be null", results);
        assertTrue("Results map should contain the metric: " + metricName, results.containsKey(metricName));
        assertEquals("Results map should contain 1 metric entry", 1, results.size());

        ApmMetricDetail detail = results.get(metricName);
        assertNotNull("ApmMetricDetail object should not be null", detail);

        // Values: 60, 70, 80
        assertEquals("Minimum should be 60.0", 60.0, detail.getMinimum(), 0.001);
        assertEquals("Maximum should be 80.0", 80.0, detail.getMaximum(), 0.001);
        assertEquals("Average should be 70.0", 70.0, detail.getAverage(), 0.001); // (60+70+80)/3
        assertEquals("Median should be 70.0", 70.0, detail.getMedian(), 0.001); // Sorted: 60, 70, 80
    }

    @Test
    public void testProcessAndGetAggregatedResults_MultipleMetrics() {
        ApmLogHandler handler = new ApmLogHandler();
        String cpuMetric = "cpu_usage";
        String memMetric = "memory_usage";

        handler.process("timestamp=... metric=" + cpuMetric + " host=... value=50");
        handler.process("timestamp=... metric=" + memMetric + " host=... value=200");
        handler.process("timestamp=... metric=" + cpuMetric + " host=... value=70");
        handler.process("timestamp=... metric=" + memMetric + " host=... value=100");

        @SuppressWarnings("unchecked")
        Map<String, ApmMetricDetail> results = (Map<String, ApmMetricDetail>) handler.getAggregatedResults();

        assertEquals("Results map should contain 2 metric entries", 2, results.size());
        assertTrue("Results map should contain CPU metric", results.containsKey(cpuMetric));
        assertTrue("Results map should contain Memory metric", results.containsKey(memMetric));

        // CPU: 50, 70
        ApmMetricDetail cpuDetail = results.get(cpuMetric);
        assertEquals("CPU Min", 50.0, cpuDetail.getMinimum(), 0.001);
        assertEquals("CPU Max", 70.0, cpuDetail.getMaximum(), 0.001);
        assertEquals("CPU Avg", 60.0, cpuDetail.getAverage(), 0.001); // (50+70)/2
        assertEquals("CPU Median", 60.0, cpuDetail.getMedian(), 0.001); // (50+70)/2

        // Memory: 100, 200
        ApmMetricDetail memDetail = results.get(memMetric);
        assertEquals("Mem Min", 100.0, memDetail.getMinimum(), 0.001);
        assertEquals("Mem Max", 200.0, memDetail.getMaximum(), 0.001);
        assertEquals("Mem Avg", 150.0, memDetail.getAverage(), 0.001); // (100+200)/2
        assertEquals("Mem Median", 150.0, memDetail.getMedian(), 0.001); // (100+200)/2
    }

    @Test
    public void testProcessAndGetAggregatedResults_NoMatchingLogs() {
        ApmLogHandler handler = new ApmLogHandler();
        handler.process("timestamp=... level=INFO message=...");
        handler.process("timestamp=... request_method=GET request_url=/api ...");

        @SuppressWarnings("unchecked")
        Map<String, ApmMetricDetail> results = (Map<String, ApmMetricDetail>) handler.getAggregatedResults();
        assertTrue("Results map should be empty if no APM logs processed", results.isEmpty());
    }
}