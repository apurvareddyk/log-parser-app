package com.apurva.logprocessor;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "log-parser", mixinStandardHelpOptions = true, version = "LogParser 1.0", description = "Parses log files and generates JSON aggregation reports.")
public class App implements Callable<Integer> {

    @Option(names = { "--file" }, required = true, description = "Input log file name (e.g., input.txt)")
    private File inputFile; // Picocli will convert the string argument to a File object

    @Override
    public Integer call() throws Exception {
        System.out.println("Processing log file: " + inputFile.getAbsolutePath());

        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Error: Input file not found or is not a regular file: " + inputFile.getAbsolutePath());
            return 1; // Error exit code
        }

        // We will create and call the LogParser here
        LogParser logParser = new LogParser();
        logParser.processLogFile(inputFile.getAbsolutePath());

        System.out.println("Log processing complete.");
        System.out.println("Output files should be: apm.json, application.json, request.json");
        return 0; // Success exit code
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}