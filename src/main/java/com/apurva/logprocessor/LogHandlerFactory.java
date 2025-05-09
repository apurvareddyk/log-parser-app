package com.apurva.logprocessor;

import java.util.List;
import java.util.ArrayList; // Or use List.of() if you prefer immutable

public class LogHandlerFactory {

    /**
     * Creates and returns a list containing instances of all known LogHandler
     * implementations.
     * This is the central place to register new handlers.
     * 
     * @return A list of LogHandler objects.
     */
    public static List<LogHandler> createAllHandlers() {
        List<LogHandler> handlers = new ArrayList<>();

        handlers.add(new ApplicationLogHandler());
        handlers.add(new ApmLogHandler());
        handlers.add(new RequestLogHandler());
        // Future: Add new handlers here, e.g., handlers.add(new SecurityLogHandler());

        return handlers;
    }
}