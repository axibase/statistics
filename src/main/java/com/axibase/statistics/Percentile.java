package com.axibase.statistics;

public class Percentile {
    private static final int PERCENTILES_COUNT = 100;

    private Selector selector;

    public Percentile(BaseDoubleIndex index) {
        this.selector = new QuickSelector(index);
    }

    public double getPercentile(double p) throws IndexAccessException {
        if (p < 0.0 || p > PERCENTILES_COUNT)
            throw new IllegalArgumentException("Percentile index should be in range [0, " +
                    PERCENTILES_COUNT + "]");

        double selectionIndex = (p / PERCENTILES_COUNT) * (selector.length() + 1);
        int integerPart = (int) selectionIndex;
        double fractionalPart = selectionIndex - integerPart;

        if (integerPart == 0)
            return selector.select(0);
        else if (integerPart >= selector.length())
            return selector.select(selector.length() - 1);
        else
            return selector.select(integerPart - 1) + fractionalPart *
                    (selector.select(integerPart) - selector.select(integerPart - 1));
    }
}
