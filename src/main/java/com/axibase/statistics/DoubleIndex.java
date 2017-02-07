package com.axibase.statistics;

public interface DoubleIndex {
    /**
     * Returns the number of elements that can be accessed by this index
     *
     * @return the number of elements
     */
    int length();

    /**
     * Gets value of element at specified index
     *
     * @param index the index of element to get
     * @return the number of elements
     * @throws IndexAccessException thrown if the index can't be accessed
     */
    double get(int index) throws IndexAccessException;

    /**
     * Sets value of element at specified index
     *
     * @param index the index of element to get
     * @param value the value to be set at the index
     * @throws IndexAccessException thrown if the index can't be accessed
     */
    void set(int index, double value) throws IndexAccessException;

    /**
     * Swaps elements at specified indices
     *
     * @param i index of first element to swap
     * @param j index of second element to swap
     * @throws IndexAccessException thrown if the index can't be accessed
     */
    void swap(int i, int j) throws IndexAccessException;
}
