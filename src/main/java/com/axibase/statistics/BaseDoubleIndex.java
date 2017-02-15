package com.axibase.statistics;

public abstract class BaseDoubleIndex implements DoubleIndex {
    static final int DOUBLE_SIZE = 8;

    public void swap(int i, int j) throws IndexAccessException {
        if (i == j)
            return;
        double t = get(i);
        set(i, get(j));
        set(j, t);
    }
}
