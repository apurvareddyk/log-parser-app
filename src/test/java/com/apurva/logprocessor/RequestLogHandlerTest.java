// File: src/test/java/com/apurva/logprocessor/RequestLogHandlerTest.java
package com.apurva.logprocessor;

import org.junit.Before; // Import Before if needed for setup
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class RequestLogHandlerTest {

    private RequestLogHandler handler; // Reuse handler instance

    // Use @Before to set up a fresh handler before each test method
    @Before
    public void setUp() {
        handler = new RequestLogHandler();
    }

    @Test
    public void testCanHandle_ValidRequestLogLine() {
        String logLine = "timestamp=... request_method=GET request_url=\"/api/status\" response_status=200 response_time_ms=100";
        assertTrue("Should identify valid request log line", handler.canHandle(logLine));
    }

    @Test
    public void testCanHandle_InvalidLogLines() {
        assertFalse("Should not handle APM log", handler.canHandle("timestamp=... metric=cpu value=50"));
        assertFalse("Should not handle App log", handler.canHandle("timestamp=... level=INFO msg=..."));
        assertFalse("Should not handle garbage", handler.canHandle("this is not a log"));
    }

    // Test the percentile calculation helper method directly
    // Note: You might need to make `calculatePercentile` public or package-private
    // OR test it implicitly through getAggregatedResults which is often sufficient.
    // If testing directly:
    /*
     * @Test
     * public void testCalculatePercentile() {
     * // If calculatePercentile were accessible (e.g., static or on the instance)
     * // RequestLogHandler handler = new RequestLogHandler(); // Or use static call
     * if possible
     * List<Long> sortedTestData = Arrays.asList(10L, 20L, 30L, 40L, 50L, 60L, 70L,
     * 80L, 90L, 100L); // N=10
     * // P50: ceil(0.50*10)-1 = 5-1 = 4. Value: 50L
     * assertEquals(50L, handler.calculatePercentile(sortedTestData, 50.0));
     * // P90: ceil(0.90*10)-1 = 9-1 = 8. Value: 90L
     * assertEquals(90L, handler.calculatePercentile(sortedTestData, 90.0));
     * // P95: ceil(0.95*10)-1 = ceil(9.5)-1 = 10-1 = 9. Value: 100L
     * assertEquals(100L, handler.calculatePercentile(sortedTestData, 95.0));
     * // P99: ceil(0.99*10)-1 = ceil(9.9)-1 = 10-1 = 9. Value: 100L
     * assertEquals(100L, handler.calculatePercentile(sortedTestData, 99.0));
     * 
     * List<Long> singleValue = Arrays.asList(55L);
     * assertEquals(55L, handler.calculatePercentile(singleValue, 50.0));
     * assertEquals(55L, handler.calculatePercentile(singleValue, 99.0));
     * }
     */

    @Test
    public void testProcessAndGetAggregatedResults_SingleRoute_MultipleRequests() {
        String route = "/api/users";
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=200 response_time_ms=100", route));
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=201 response_time_ms=200", route));
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=404 response_time_ms=50", route)); // min
                                                                                                                        // time,
                                                                                                                        // 4xx
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=500 response_time_ms=300", route)); // max
                                                                                                                         // time,
                                                                                                                         // 5xx
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=200 response_time_ms=150", route));

        @SuppressWarnings("unchecked")
        Map<String, RequestRouteStats> results = (Map<String, RequestRouteStats>) handler.getAggregatedResults();

        assertEquals("Should contain 1 route", 1, results.size());
        assertTrue("Should contain route: " + route, results.containsKey(route));

        RequestRouteStats stats = results.get(route);
        assertNotNull("Route stats should not be null", stats);

        // Verify Status Codes
        StatusCodeCounts codes = stats.getStatusCodes();
        assertNotNull("Status code counts should not be null", codes);
        assertEquals("2XX count", 3, codes.getCount2XX()); // 200, 201, 200
        assertEquals("4XX count", 1, codes.getCount4XX()); // 404
        assertEquals("5XX count", 1, codes.getCount5XX()); // 500

        // Verify Response Times: 100, 200, 50, 300, 150 (N=5)
        // Sorted: 50, 100, 150, 200, 300
        ResponseTimeStats times = stats.getResponseTimes();
        assertNotNull("Response time stats should not be null", times);
        assertEquals("Min time", 50, times.getMin());
        assertEquals("Max time", 300, times.getMax());

        // Calculate expected percentiles for N=5
        // P50: ceil(0.50*5)-1 = ceil(2.5)-1 = 3-1=2. Value at index 2: 150
        assertEquals("P50", 150, times.getP50Percentile());
        // P90: ceil(0.90*5)-1 = ceil(4.5)-1 = 5-1=4. Value at index 4: 300
        assertEquals("P90", 300, times.getP90Percentile());
        // P95: ceil(0.95*5)-1 = ceil(4.75)-1 = 5-1=4. Value at index 4: 300
        assertEquals("P95", 300, times.getP95Percentile());
        // P99: ceil(0.99*5)-1 = ceil(4.95)-1 = 5-1=4. Value at index 4: 300
        assertEquals("P99", 300, times.getP99Percentile());
    }

    @Test
    public void testProcessAndGetAggregatedResults_MultipleRoutes() {
        String route1 = "/api/a";
        String route2 = "/api/b";

        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=200 response_time_ms=10", route1));
        handler.process(String.format(
                "timestamp=... request_method=POST request_url=\"%s\" response_status=400 response_time_ms=20",
                route2));
        handler.process(String.format(
                "timestamp=... request_method=GET request_url=\"%s\" response_status=200 response_time_ms=30", route1));

        @SuppressWarnings("unchecked")
        Map<String, RequestRouteStats> results = (Map<String, RequestRouteStats>) handler.getAggregatedResults();

        assertEquals("Should contain 2 routes", 2, results.size());
        assertTrue("Should contain route1", results.containsKey(route1));
        assertTrue("Should contain route2", results.containsKey(route2));

        // Basic checks for route1 (Times: 10, 30; Status: 200, 200)
        RequestRouteStats stats1 = results.get(route1);
        assertEquals("Route 1 Min Time", 10, stats1.getResponseTimes().getMin());
        assertEquals("Route 1 Max Time", 30, stats1.getResponseTimes().getMax());
        assertEquals("Route 1 2XX Count", 2, stats1.getStatusCodes().getCount2XX());
        assertEquals("Route 1 4XX Count", 0, stats1.getStatusCodes().getCount4XX());

        // Basic checks for route2 (Times: 20; Status: 400)
        RequestRouteStats stats2 = results.get(route2);
        assertEquals("Route 2 Min Time", 20, stats2.getResponseTimes().getMin());
        assertEquals("Route 2 Max Time", 20, stats2.getResponseTimes().getMax());
        assertEquals("Route 2 P99", 20, stats2.getResponseTimes().getP99Percentile()); // Single value -> all
                                                                                       // percentiles are that value
        assertEquals("Route 2 2XX Count", 0, stats2.getStatusCodes().getCount2XX());
        assertEquals("Route 2 4XX Count", 1, stats2.getStatusCodes().getCount4XX());
        assertEquals("Route 2 5XX Count", 0, stats2.getStatusCodes().getCount5XX());
    }

    @Test
    public void testProcessAndGetAggregatedResults_NoMatchingLogs() {
        handler.process("timestamp=... level=INFO message=...");
        handler.process("timestamp=... metric=cpu value=10");

        @SuppressWarnings("unchecked")
        Map<String, RequestRouteStats> results = (Map<String, RequestRouteStats>) handler.getAggregatedResults();
        assertTrue("Results map should be empty if no request logs processed", results.isEmpty());
    }
}