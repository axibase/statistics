package com.axibase.statistics;

import java.io.IOException;

public interface QuantileEstimator {
    double estimate(int quantiles, double kth) throws IOException;
}
