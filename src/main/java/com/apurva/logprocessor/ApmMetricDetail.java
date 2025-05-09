package com.apurva.logprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApmMetricDetail {
    private double minimum = Double.MAX_VALUE; // Initialize to easily find the actual min
    private double median;
    private double average;
    private double maximum = Double.MIN_VALUE; // Initialize to easily find the actual max

    // This 'transient' keyword means Jackson (our JSON library) will ignore this
    // field during serialization.
    // We only need this list for calculations.
    private transient List<Double> values = new ArrayList<>();

    // A no-argument constructor is good practice, especially for libraries like
    // Jackson.
    public ApmMetricDetail() {
    }

    public void addValue(double value) {
        this.values.add(value);
        if (value < this.minimum) {
            this.minimum = value;
        }
        if (value > this.maximum) {
            this.maximum = value;
        }
    }

    public void calculateAggregates() {
        if (this.values.isEmpty()) {
            // As per the project description, fields should be present in JSON.
            // Let's default to 0 if there are no values for a metric.
            this.minimum = 0;
            this.median = 0;
            this.average = 0;
            this.maximum = 0;
            return;
        }

        // Calculate Average
        double sum = 0;
        for (double val : this.values) {
            sum += val;
        }
        this.average = sum / this.values.size();

        // Calculate Median
        Collections.sort(this.values); // Sort the list to find the median
        int middle = this.values.size() / 2;
        if (this.values.size() % 2 == 1) { // Odd number of values
            this.median = this.values.get(middle);
        } else { // Even number of values (and not empty)
            this.median = (this.values.get(middle - 1) + this.values.get(middle)) / 2.0;
        }
        // Note: Minimum and Maximum were already updated in addValue()
    }

    // --- Getters required by Jackson for JSON serialization ---
    public double getMinimum() {
        return this.minimum;
    }

    public double getMedian() {
        return this.median;
    }

    public double getAverage() {
        return this.average;
    }

    public double getMaximum() {
        return this.maximum;
    }

    // --- Setters (optional, but good practice if you ever need to create these
    // objects from data) ---
    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }
}