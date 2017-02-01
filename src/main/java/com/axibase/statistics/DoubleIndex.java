package com.axibase.statistics;

import java.io.IOException;

public abstract class DoubleIndex {
    static final int DOUBLE_SIZE = 8;

    abstract int length();

    abstract double get(int index) throws IOException;

    abstract void set(int index, double value) throws IOException;

    void swap(int i, int j) throws IOException {
        if (i == j)
            return;
        double t = get(i);
        set(i, get(j));
        set(j, t);
    }
}
