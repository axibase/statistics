package com.axibase.statistics;

public interface Selector {
    int length();

    double select(int index) throws IndexAccessException;
}
