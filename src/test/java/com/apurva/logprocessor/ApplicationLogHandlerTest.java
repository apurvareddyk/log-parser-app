package com.apurva.logprocessor;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class ApplicationLogHandlerTest {

    @Test
    public void testCanHandle_ValidApplicationLogLine() {
        ApplicationLogHandler handler = new ApplicationLogHandler();
        String logLine = "timestamp=2024-02-24T16:22:20Z level=INFO message=\"Scheduled maintenance starting\" host=webserver1";
        assertTrue("Should correctly identify an application log line", handler.canHandle(logLine));
    }

    @Test
    public void testCanHandle_ApmLogLine_ShouldReturnFalse() {
        ApplicationLogHandler handler = new ApplicationLogHandler();
        String logLine = "timestamp=2024-02-24T16:22:15Z metric=cpu_usage_percent host=webserver1 value=72";
        assertFalse("Should not handle an APM log line", handler.canHandle(logLine));
    }

    @Test
    public void testCanHandle_RequestLogLine_ShouldReturnFalse() {
        ApplicationLogHandler handler = new ApplicationLogHandler();
        String logLine = "timestamp=2024-02-24T16:22:25Z request_method=POST request_url=\"/api/update\" response_status=202 response_time_ms=200 host=webserver1";
        assertFalse("Should not handle a request log line", handler.canHandle(logLine));
    }

    @Test
    public void testCanHandle_MalformedLine_ShouldReturnFalse() {
        ApplicationLogHandler handler = new ApplicationLogHandler();
        String logLine = "this is not a valid log line";
        assertFalse("Should not handle a malformed log line", handler.canHandle(logLine));
    }

    @Test
    public void testProcessAndGetAggregatedResults_CountsSeverityCorrectly() {
        ApplicationLogHandler handler = new ApplicationLogHandler();

        // Process a mix of log lines
        handler.process("timestamp=... level=INFO message=First info");
        handler.process("timestamp=... level=ERROR message=First error");
        handler.process("timestamp=... level=INFO message=Second info");
        handler.process("timestamp=... level=DEBUG message=First debug");
        handler.process("timestamp=... level=WARNING message=First warning");
        handler.process("timestamp=... level=ERROR message=Second error");
        handler.process("timestamp=... level=INFO message=Third info");
        // Add a log line that this handler should NOT process to ensure it's ignored
        handler.process("timestamp=... metric=cpu value=10"); // APM log

        @SuppressWarnings("unchecked") // We expect a Map<String, Integer>
        Map<String, Integer> counts = (Map<String, Integer>) handler.getAggregatedResults();

        // Check the counts. Remember ApplicationLogStats initializes all to 0.
        assertEquals("INFO count should be 3", Integer.valueOf(3), counts.get("INFO"));
        assertEquals("ERROR count should be 2", Integer.valueOf(2), counts.get("ERROR"));
        assertEquals("DEBUG count should be 1", Integer.valueOf(1), counts.get("DEBUG"));
        assertEquals("WARNING count should be 1", Integer.valueOf(1), counts.get("WARNING"));
        // Ensure other levels (if any were unexpectedly added or if default is not 0
        // for others) are handled
        // For example, if your ApplicationLogStats doesn't initialize other keys, they
        // might be null.
        // If they are initialized to 0, then this test is fine.
    }

    @Test
    public void testProcessAndGetAggregatedResults_NoMatchingLogs() {
        ApplicationLogHandler handler = new ApplicationLogHandler();
        handler.process("timestamp=... metric=cpu value=10");
        handler.process("timestamp=... request_method=GET request_url=/api value=100");

        @SuppressWarnings("unchecked")
        Map<String, Integer> counts = (Map<String, Integer>) handler.getAggregatedResults();

        // All initialized counts should remain 0
        assertEquals("INFO count should be 0 for no matching logs", Integer.valueOf(0), counts.get("INFO"));
        assertEquals("ERROR count should be 0 for no matching logs", Integer.valueOf(0), counts.get("ERROR"));
        assertEquals("DEBUG count should be 0 for no matching logs", Integer.valueOf(0), counts.get("DEBUG"));
        assertEquals("WARNING count should be 0 for no matching logs", Integer.valueOf(0), counts.get("WARNING"));
    }
}