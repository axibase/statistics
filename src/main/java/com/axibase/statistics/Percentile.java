package com.axibase.statistics;

import java.io.IOException;

public class Percentile {
    private Selector selector;

    public Percentile(Selector selector) {
        this.selector = selector;
    }

    public double getPercentile(double p) throws IOException {
        double selectionIndex = (p / 100) * (selector.length() + 1);
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
