package com.axibase.statistics;

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class QuickSelectorTest {

    @Test
    void testSorted() throws IndexAccessException {
        double[] values, vasluesCopy;
        int[] selectionIndices;

        values = ValueGenerator.generateRandom(100_000);
        selectionIndices = ValueGenerator.generateIndices(1_000);

        Arrays.sort(values);
        vasluesCopy = Arrays.copyOf(values, values.length);

        MemoryIndex memIndex = new MemoryIndex(values);
        Selector selector = new QuickSelector(memIndex);

        for (int selectionIndex : selectionIndices) {
            double expected = vasluesCopy[selectionIndex];
            double actual = selector.select(selectionIndex);
            assertEquals(actual, expected, "Incorrect selection value with k=" + selectionIndex);
        }
    }

    @Test
    void testUnsorted() throws IndexAccessException {
        double[] values, sortedValues;
        int[] selectionIndices;

        values = ValueGenerator.generateRandom(100_000);
        selectionIndices = ValueGenerator.generateIndices(1_000);

        sortedValues = Arrays.copyOf(values, values.length);
        Arrays.sort(sortedValues);

        MemoryIndex memIndex = new MemoryIndex(values);
        Selector selector = new QuickSelector(memIndex);

        for (int selectionIndex : selectionIndices) {
            double expected = sortedValues[selectionIndex];
            double actual = selector.select(selectionIndex);
            assertEquals(actual, expected, "Incorrect selection value with k=" + selectionIndex);
        }
    }
}
