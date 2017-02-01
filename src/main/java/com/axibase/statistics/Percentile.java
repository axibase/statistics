package com.axibase.statistics;

import java.io.IOException;

public class Percentile {
    private QuantileEstimator estimator;

    public Percentile(Selector selector) {
        this.estimator = new StandardQuantileEstimator(selector);
    }

    public double getPercentile(double p) throws IOException {
        return estimator.estimate(100, p);
    }
}
