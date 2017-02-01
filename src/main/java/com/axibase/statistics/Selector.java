package com.axibase.statistics;

import java.io.IOException;

public interface Selector {
    void setData(DoubleIndex data);

    int length();

    double select(int index) throws IOException;
}
