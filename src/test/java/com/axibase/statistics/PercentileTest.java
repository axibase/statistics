package com.axibase.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class PercentileTest {
    private double[] values;

    @BeforeClass
    void initRandomValues() throws IOException {
        values = ValueGenerator.generateRandom(100_000);
    }

    @Test
    void testSingleValue() throws IOException, IndexAccessException {
        testWith(
                new double[]{1.0},
                new double[]{25.0, 50.0, 75.0, 100.0}
        );
    }

    @Test
    void testTwoValues() throws IOException, IndexAccessException {
        testWith(
                new double[]{1.0, 2.0},
                new double[]{25.0, 50.0, 75.0, 100.0}
        );
    }

    @Test
    void testRandomValuesIntegerPercentiles() throws IOException, IndexAccessException {
        testWith(values, ValueGenerator.generateByIndex(100));
    }

    @Test
    void testRandomValuesRandomPercentiles() throws IOException, IndexAccessException {
        testWith(values, ValueGenerator.generateRandom(1_000, 100.0));
    }

    private void testWith(double[] values, double[] percentiles) throws IndexAccessException {
        DescriptiveStatistics desc = new DescriptiveStatistics();
        DoubleIndex d = new MemoryIndex(values);
        for (double v : values) {
            desc.addValue(v);
        }

        Percentile percentile = new Percentile(d);
        for (double p : percentiles) {
            double expected = desc.getPercentile(p);
            double actual = 0;
            actual = percentile.getPercentile(p);
            assertEquals(actual, expected, "Incorrect percentile value");
        }
    }
}
