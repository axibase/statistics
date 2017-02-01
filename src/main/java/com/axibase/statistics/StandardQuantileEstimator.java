package com.axibase.statistics;

import java.io.IOException;

public class StandardQuantileEstimator implements QuantileEstimator {
    private final Selector selector;

    StandardQuantileEstimator(Selector selector) {
        this.selector = selector;
    }

    @Override
    public double estimate(int quantiles, double kth) throws IOException {
        double selectionIndex = (kth / quantiles) * (selector.length() + 1);
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
