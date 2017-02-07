package com.axibase.statistics;

public class Percentile {
    private static final int PERCENTILES_COUNT = 100;

    private Selector selector;

    public Percentile(DoubleIndex index) {
        this.selector = new QuickSelector(index);
    }

    /**
     * Computes p-th percentile using estimation method described in
     * <a href="http://www.itl.nist.gov/div898/handbook/prc/section2/prc252.htm">this document</a>
     *
     * @param p the index of percentile to compute
     * @return the computed value for p-th percentile
     * @throws IndexAccessException is thrown if index access issue
     *                              happens during calculation
     */
    public double getPercentile(double p) throws IndexAccessException {
        if (p < 0.0 || p > PERCENTILES_COUNT)
            throw new IllegalArgumentException("Percentile index should be in range [0, " +
                    PERCENTILES_COUNT + "]");

        int elementsCount = selector.length();
        double selectionIndex = (p / PERCENTILES_COUNT) * (elementsCount + 1);
        int integerPart = (int) selectionIndex;
        double fractionalPart = selectionIndex - integerPart;

        if (integerPart == 0) {
            return selector.select(0);
        } else if (integerPart >= elementsCount) {
            return selector.select(elementsCount - 1);
        } else {
            return selector.select(integerPart - 1) + fractionalPart *
                    (selector.select(integerPart) - selector.select(integerPart - 1));
        }
    }
}
