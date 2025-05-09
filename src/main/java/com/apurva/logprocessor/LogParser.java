// File: src/main/java/com/apurva/logprocessor/LogParser.java
package com.apurva.logprocessor; // Make sure this matches your package

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LogParser {
    private List<LogHandler> handlers;

    public LogParser() {
        this.handlers = LogHandlerFactory.createAllHandlers();
    }

    public void processLogFile(String filePath) {
        System.out.println("LogParser: Starting to process file - " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
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

                        }
                    }
                }
                if (!handled) {
                    // System.out.println("WARN: Line " + lineNumber + " was not handled: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log file '" + filePath + "': " + e.getMessage());
            return;
        }

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