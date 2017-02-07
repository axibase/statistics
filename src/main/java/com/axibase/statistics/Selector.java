package com.axibase.statistics;

public interface Selector {
    /**
     * Returns length of selectable data
     *
     * @return length of data
     */
    int length();

    /**
     * Computes the k-th smallest element in index
     *
     * @param k the index of element as if array was sorted
     * @return the value of k-th smallest element
     * @throws IndexAccessException is thrown on index access issues
     */
    double select(int k) throws IndexAccessException;
}
