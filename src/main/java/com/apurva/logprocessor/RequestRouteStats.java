package com.apurva.logprocessor;

import com.fasterxml.jackson.annotation.JsonProperty; // Import this

public class RequestRouteStats {
    @JsonProperty("response_times") // To match JSON output key
    private ResponseTimeStats responseTimes;

    @JsonProperty("status_codes") // To match JSON output key
    private StatusCodeCounts statusCodes;

    public RequestRouteStats() {
        this.responseTimes = new ResponseTimeStats(); // Initialize with default values
        this.statusCodes = new StatusCodeCounts(); // Initialize with zero counts
    }

    public RequestRouteStats(ResponseTimeStats responseTimes, StatusCodeCounts statusCodes) {
        this.responseTimes = responseTimes;
        this.statusCodes = statusCodes;
    }

    // --- Getters ---
    public ResponseTimeStats getResponseTimes() {
        return responseTimes;
    }

    public StatusCodeCounts getStatusCodes() {
        return statusCodes;
    }

    // --- Setters ---
    public void setResponseTimes(ResponseTimeStats responseTimes) {
        this.responseTimes = responseTimes;
    }

    public void setStatusCodes(StatusCodeCounts statusCodes) {
        this.statusCodes = statusCodes;
    }
}