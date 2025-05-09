package com.apurva.logprocessor;

import org.junit.Test; // For the @Test annotation
import static org.junit.Assert.*; // For assertion methods like assertEquals

public class ApmMetricDetailTest {

    @Test
    public void testApmCalculations_OddNumberOfValues() {
        ApmMetricDetail detail = new ApmMetricDetail();
        detail.addValue(10.0);
        detail.addValue(20.0);
        detail.addValue(30.0);
        detail.addValue(0.0); // This will be the minimum
        detail.addValue(40.0); // This will be the maximum

        detail.calculateAggregates();

        // For double comparisons, it's good to include a "delta" (a small tolerance)
        assertEquals("Minimum should be 0.0", 0.0, detail.getMinimum(), 0.001);
        assertEquals("Maximum should be 40.0", 40.0, detail.getMaximum(), 0.001);
        // Sum = 10+20+30+0+40 = 100. Count = 5. Average = 100/5 = 20.0
        assertEquals("Average should be 20.0", 20.0, detail.getAverage(), 0.001);
        // Sorted values: 0.0, 10.0, 20.0, 30.0, 40.0. Median is the middle value (20.0)
        assertEquals("Median should be 20.0", 20.0, detail.getMedian(), 0.001);
    }

    @Test
    public void testApmCalculations_EvenNumberOfValues() {
        ApmMetricDetail detail = new ApmMetricDetail();
        detail.addValue(10.0);
        detail.addValue(20.0);
        detail.addValue(25.0); // Max
        detail.addValue(15.0); // Min

        detail.calculateAggregates();

        // Sorted values: 10.0, 15.0, 20.0, 25.0
        assertEquals("Minimum should be 10.0", 10.0, detail.getMinimum(), 0.001);
        assertEquals("Maximum should be 25.0", 25.0, detail.getMaximum(), 0.001);
        // Sum = 10+15+20+25 = 70. Count = 4. Average = 70/4 = 17.5
        assertEquals("Average should be 17.5", 17.5, detail.getAverage(), 0.001);
        // Median for even numbers: (values[N/2 - 1] + values[N/2]) / 2
        // (15.0 + 20.0) / 2 = 17.5
        assertEquals("Median should be 17.5", 17.5, detail.getMedian(), 0.001);
    }

    @Test
    public void testApmCalculations_SingleValue() {
        ApmMetricDetail detail = new ApmMetricDetail();
        detail.addValue(77.0);

        detail.calculateAggregates();

        assertEquals("Minimum should be 77.0", 77.0, detail.getMinimum(), 0.001);
        assertEquals("Maximum should be 77.0", 77.0, detail.getMaximum(), 0.001);
        assertEquals("Average should be 77.0", 77.0, detail.getAverage(), 0.001);
        assertEquals("Median should be 77.0", 77.0, detail.getMedian(), 0.001);
    }

    @Test
    public void testApmCalculations_NoValues() {
        ApmMetricDetail detail = new ApmMetricDetail();
        // No values added

        detail.calculateAggregates(); // Should handle empty list gracefully

        // Based on your ApmMetricDetail implementation, these should be 0.0 for empty
        // lists
        assertEquals("Minimum should be 0.0 for no values", 0.0, detail.getMinimum(), 0.001);
        assertEquals("Maximum should be 0.0 for no values", 0.0, detail.getMaximum(), 0.001);
        assertEquals("Average should be 0.0 for no values", 0.0, detail.getAverage(), 0.001);
        assertEquals("Median should be 0.0 for no values", 0.0, detail.getMedian(), 0.001);
    }
}