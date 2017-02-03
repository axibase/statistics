package com.axibase.statistics;

public interface DoubleIndex {
    int length();

    double get(int index) throws IndexAccessException;

    void set(int index, double value) throws IndexAccessException;
}
