// File: src/main/java/com/apurva/logprocessor/LogParser.java
package com.apurva.logprocessor; // Make sure this matches your package

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogParser {
    private List<LogHandler> handlers;

    public LogParser() {
        handlers = new ArrayList<>();
        handlers.add(new ApplicationLogHandler());
        handlers.add(new ApmLogHandler());
        handlers.add(new RequestLogHandler());
    }

    public void processLogFile(String filePath) {
        System.out.println("LogParser: Starting to process file - " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // System.out.println("DEBUG: Reading line " + lineNumber + ": " + line); // For
                // debugging
                boolean handled = false;
                for (LogHandler handler : handlers) {
                    if (handler.canHandle(line)) {
                        try {
                            handler.process(line);
                            handled = true;
                            break; // Important: Assume only one handler processes a line
                        } catch (Exception e) {
                            System.err.println("Error processing line " + lineNumber + " with "
                                    + handler.getClass().getSimpleName() + ": [" + line + "]. Error: "
                                    + e.getMessage());
                            // Continue to next line even if one handler fails on a line it thought it could
                            // handle
                        }
                    }
                }
                if (!handled) {
                    // Optional: Log lines that are not handled by any processor
                    // System.out.println("WARN: Line " + lineNumber + " was not handled: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log file '" + filePath + "': " + e.getMessage());
            // e.printStackTrace(); // For more detailed error
            return; // Stop processing if file can't be read
        }

        // After processing all lines, instruct each handler to write its results
        System.out.println("LogParser: Finished reading file. Writing aggregated results...");
        for (LogHandler handler : handlers) {
            try {
                handler.writeResultToFile();
            } catch (IOException e) {
                System.err.println("Error writing output file " + handler.getOutputFileName() + ": " + e.getMessage());
                // e.printStackTrace(); // For more detailed error
            }
        }
        System.out.println("LogParser: All results written.");
    }
}