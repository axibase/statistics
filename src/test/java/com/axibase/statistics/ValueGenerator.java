package com.axibase.statistics;

import java.util.Random;

class ValueGenerator {

    private static final int RANDOM_SEED = 19;

    static double[] generateRandom(int count) {
        return generateRandom(count, 1.0);
    }

    static double[] generateRandom(int count, double multiplier) {
        Random rand = new Random(RANDOM_SEED);
        double[] values = new double[count];

        for (int i = 0; i < count; i++)
            values[i] = rand.nextDouble() * multiplier;

        return values;
    }

    static double[] generateByIndex(int count) {
        double[] values = new double[count];

        for (int i = 0; i < count; i++)
            values[i] = i + 1;

        return values;
    }

    static int[] generateIndices(int length) {
        Random rand = new Random(RANDOM_SEED);
        int[] indices = new int[length];

        for (int i = 0; i < length; i++)
            indices[i] = rand.nextInt(length);

        return indices;
    }
}
