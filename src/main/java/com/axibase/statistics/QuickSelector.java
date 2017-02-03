package com.axibase.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class QuickSelector implements Selector {
    private final Random rand = new Random();

    private BaseDoubleIndex data;
    private BinaryNode root;
    private Map<Integer, Double> computed;

    private class Range {
        int leftBound, rightBound;

        Range(int leftBound, int rightBound) {
            this.leftBound = leftBound;
            this.rightBound = rightBound;
        }
    }

    private class BinaryNode {
        BinaryNode left, right;
        int key;

        BinaryNode(int key) {
            this.key = key;
        }
    }

    public QuickSelector(BaseDoubleIndex data) {
        this.data = data;
        root = null;
        computed = new HashMap<>();
    }

    public int length() {
        return data.length();
    }

    private void put(int k) {
        if (root == null) {
            root = new BinaryNode(k);
            return;
        }
        BinaryNode current = root;
        while (true) {
            if (k < current.key) {
                if (current.left == null) {
                    current.left = new BinaryNode(k);
                    break;
                } else
                    current = current.left;
            } else if (k > current.key) {
                if (current.right == null) {
                    current.right = new BinaryNode(k);
                    break;
                } else
                    current = current.right;
            } else
                break;
        }
    }

    private Range getRange(int index) {
        int leftBound = 0, rightBound = data.length() - 1;
        BinaryNode current = root;
        while (current != null) {
            if (index < current.key) {
                rightBound = current.key - 1;
                current = current.left;
            } else if (index > current.key) {
                leftBound = current.key + 1;
                current = current.right;
            } else
                return new Range(index, index);
        }
        return new Range(leftBound, rightBound);
    }

    private int partition(int leftBound, int rightBound) throws IndexAccessException {
        double pivotElement = data.get(rightBound);
        int i = leftBound - 1;
        for (int j = leftBound; j < rightBound; j++) {
            double y = data.get(j);
            if (y < pivotElement || (y == pivotElement && rand.nextBoolean())) {
                i++;
                data.swap(i, j);
            }
        }
        data.swap(i + 1, rightBound);
        return i + 1;
    }

    private int randomPartition(int leftBound, int rightBound) throws IndexAccessException {
        if (leftBound == rightBound)
            return leftBound;
        int u = rand.nextInt(rightBound - leftBound) + leftBound;
        data.swap(u, rightBound);
        return partition(leftBound, rightBound);
    }

    private double selectInRange(int k, int leftBound, int rightBound) throws IndexAccessException {
        if (leftBound == rightBound)
            return data.get(k);
        while (true) {
            int middle = randomPartition(leftBound, rightBound);

            put(middle);
            computed.put(middle, data.get(middle));

            if (middle == k) {
                return data.get(middle);
            } else if (k < middle) {
                rightBound = middle - 1;
            } else {
                leftBound = middle + 1;
            }
        }
    }

    public double select(int k) throws IndexAccessException {
        if (computed.containsKey(k))
            return computed.get(k);

        Range range = getRange(k);
        return selectInRange(k, range.leftBound, range.rightBound);
    }
}
