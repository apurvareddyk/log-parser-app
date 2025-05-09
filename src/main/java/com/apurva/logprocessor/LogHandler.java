package com.apurva.logprocessor; // Make sure this matches your package

import java.io.IOException;

public interface LogHandler {
    /**
     * Checks if this handler can process the given log line.
     * 
     * @param logLine The log line to check.
     * @return true if the handler can process this line, false otherwise.
     */
    boolean canHandle(String logLine);

    /**
     * Parses the log line and updates internal aggregation data.
     * Assumes canHandle() returned true for this line.
     * 
     * @param logLine The log line to process.
     */
    void process(String logLine);

    /**
     * Returns the final aggregated results that will be written to JSON.
     * 
     * @return An object representing the aggregated data.
     */
    Object getAggregatedResults();

    /**
     * Gets the name of the output file for this handler's results.
     * 
     * @return The output file name (e.g., "apm.json").
     */
    String getOutputFileName();

    /**
     * Writes the aggregated results to the appropriate JSON file.
     * This method will typically use a JSON library like Jackson.
     * 
     * @throws IOException if there's an error writing the file.
     */
    void writeResultToFile() throws IOException;
}