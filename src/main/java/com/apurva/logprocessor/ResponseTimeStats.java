package com.apurva.logprocessor;

import com.fasterxml.jackson.annotation.JsonProperty; // Import this

public class ResponseTimeStats {
    private long min; // Using long for time in ms, as per response_time_ms

    @JsonProperty("50_percentile") // To match the JSON output key
    private long p50Percentile;

    @JsonProperty("90_percentile")
    private long p90Percentile;

    @JsonProperty("95_percentile")
    private long p95Percentile;

    @JsonProperty("99_percentile")
    private long p99Percentile;

    private long max;

    // Default constructor for Jackson
    public ResponseTimeStats() {
    }

    // Constructor to initialize all fields (useful after calculation)
    public ResponseTimeStats(long min, long p50, long p90, long p95, long p99, long max) {
        this.min = min;
        this.p50Percentile = p50;
        this.p90Percentile = p90;
        this.p95Percentile = p95;
        this.p99Percentile = p99;
        this.max = max;
    }

    // --- Getters ---
    public long getMin() {
        return min;
    }

    @JsonProperty("50_percentile")
    public long getP50Percentile() {
        return p50Percentile;
    }

    @JsonProperty("90_percentile")
    public long getP90Percentile() {
        return p90Percentile;
    }

    @JsonProperty("95_percentile")
    public long getP95Percentile() {
        return p95Percentile;
    }

    @JsonProperty("99_percentile")
    public long getP99Percentile() {
        return p99Percentile;
    }

    public long getMax() {
        return max;
    }

    // --- Setters (optional, but good practice) ---
    public void setMin(long min) {
        this.min = min;
    }

    public void setP50Percentile(long p50Percentile) {
        this.p50Percentile = p50Percentile;
    }

    public void setP90Percentile(long p90Percentile) {
        this.p90Percentile = p90Percentile;
    }

    public void setP95Percentile(long p95Percentile) {
        this.p95Percentile = p95Percentile;
    }

    public void setP99Percentile(long p99Percentile) {
        this.p99Percentile = p99Percentile;
    }

    public void setMax(long max) {
        this.max = max;
    }
}