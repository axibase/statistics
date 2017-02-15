package com.axibase.statistics;

public class MemoryIndex extends BaseDoubleIndex {
    private double values[];

    MemoryIndex(double[] values) {
        this.values = values;
    }

    @Override
    public int length() {
        return values.length;
    }

    @Override
    public double get(int index) throws IndexAccessException {
        return values[index];
    }

    @Override
    public void set(int index, double value) throws IndexAccessException {
        values[index] = value;
    }
}
