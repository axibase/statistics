package com.axibase.statistics;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static org.testng.Assert.assertEquals;

public class CacheTest {

    private static final String testFileName = "test.dat";

    @Test
    void testTrivial() throws IOException, IndexAccessException {
        double value = 1.0;
        try (CachedFileDoubleIndex index = new CachedFileDoubleIndex(testFileName)) {
            index.addValue(value);
            index.completeInsertion();

            double got = index.get(0);

            assertEquals(got, value, "Incorrect value at index 0");
        }
    }

    @Test
    void testInitAndRead() throws IOException, IndexAccessException {
        int count = 5_000_000;
        try (CachedFileDoubleIndex index = new CachedFileDoubleIndex(testFileName)) {

            for (int i = 0; i < count; i++)
                index.addValue(i);
            index.completeInsertion();

            for (int i = 0; i < count; i++) {
                double got = index.get(i);
                assertEquals(got, (double) i, "Incorrect value at index " + i);
            }
        }
    }

    @Test
    void testSwap() throws IOException, IndexAccessException {
        int count = 100_000, swapCount = 500_000;
        Random rand = new Random(19);
        double[] values = new double[count];

        try (CachedFileDoubleIndex index = new CachedFileDoubleIndex(testFileName)) {
            for (int i = 0; i < count; i++) {
                values[i] = i;
                index.addValue(i);
            }
            index.completeInsertion();

            for (int i = 0; i < swapCount; i++) {
                int firstToSwap = rand.nextInt(count);
                int secondToSwap = rand.nextInt(count);

                index.swap(firstToSwap, secondToSwap);

                double t = values[firstToSwap];
                values[firstToSwap] = values[secondToSwap];
                values[secondToSwap] = t;
            }

            for (int i = 0; i < count; i++) {
                double got = index.get(i);
                assertEquals(got, values[i], "Incorrect value at index " + i);
            }
        }
    }

}
