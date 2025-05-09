package com.apurva.logprocessor;

import com.fasterxml.jackson.annotation.JsonProperty; // Import this

public class StatusCodeCounts {
    @JsonProperty("2XX") // To match the JSON output key
    private int count2XX;

    @JsonProperty("4XX")
    private int count4XX;

    @JsonProperty("5XX")
    private int count5XX;

    public StatusCodeCounts() {
        this.count2XX = 0;
        this.count4XX = 0;
        this.count5XX = 0;
    }

    public void increment2XX() {
        this.count2XX++;
    }

    public void increment4XX() {
        this.count4XX++;
    }

    public void increment5XX() {
        this.count5XX++;
    }

    // --- Getters ---
    @JsonProperty("2XX")
    public int getCount2XX() {
        return count2XX;
    }

    @JsonProperty("4XX")
    public int getCount4XX() {
        return count4XX;
    }

    @JsonProperty("5XX")
    public int getCount5XX() {
        return count5XX;
    }

    // --- Setters (optional) ---
    public void setCount2XX(int count2xx) {
        this.count2XX = count2xx;
    }

    public void setCount4XX(int count4xx) {
        this.count4XX = count4xx;
    }

    public void setCount5XX(int count5xx) {
        this.count5XX = count5xx;
    }
}