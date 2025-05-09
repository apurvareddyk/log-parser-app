package com.apurva.logprocessor;

import java.util.ArrayList;
import java.util.List;

class RequestRouteData {
    private List<Long> responseTimesMs = new ArrayList<>();
    private List<Integer> statusCodes = new ArrayList<>();

    public void addResponseTime(long timeMs) {
        this.responseTimesMs.add(timeMs);
    }

    public void addStatusCode(int code) {
        this.statusCodes.add(code);
    }

    public List<Long> getResponseTimesMs() {
        return responseTimesMs;
    }

    public List<Integer> getStatusCodes() {
        return statusCodes;
    }
}