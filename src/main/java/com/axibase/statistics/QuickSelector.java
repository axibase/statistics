package com.axibase.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This class implements quick selection algorithm
 * <p>
 * Details are described in Introduction to Algorithms, 3rd ed., ch. 9
 * Additionally, the binary tree ihelps to reuse results from previous
 * computations
 */
public class QuickSelector implements Selector {
    private final Random rand = new Random();

    private DoubleIndex data;
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

    /**
     * Creates selector based on {@link DoubleIndex} as data
     *
     * @param data the double-value data accessed by index
     */
    QuickSelector(DoubleIndex data) {
        this.data = data;
        root = null;
        computed = new HashMap<>();
    }

    @Override
    public int length() {
        return data.length();
    }

    /**
     * Inserts the key into binary tree
     *
     * @param key the key to insert
     */
    private void put(int key) {
        if (root == null) {
            root = new BinaryNode(key);
            return;
        }
        BinaryNode current = root;
        while (key != current.key) {
            if (key < current.key) {
                if (current.left == null) {
                    current.left = new BinaryNode(key);
                    break;
                } else
                    current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new BinaryNode(key);
                    break;
                } else
                    current = current.right;
            }
        }
    }

    /**
     * Computes restricted range for index. Using information in binary tree
     *
     * @param index the index of element as if array was sorted
     * @return the smallest range where selection should be performed
     */
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
            } else {
                return new Range(index, index);
            }
        }
        return new Range(leftBound, rightBound);
    }

    /**
     * Partitions elements in range with rightmost element as pivotal
     *
     * @param leftBound  begin of range to partition inclusively
     * @param rightBound end of range to partition inclusively
     * @return index of partitioning element in range
     * @throws IndexAccessException is thrown on index access issues
     */
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

    /**
     * Selects pivot element randomly before partitioning.
     * Randomness helps to avoid O(n^2) complexity if,
     * for example, array is already sorted
     *
     * @param leftBound  begin of range to partition
     * @param rightBound end of range to partition
     * @return index of partitioning element in range
     * @throws IndexAccessException is thrown on index access issues
     */
    private int randomPartition(int leftBound, int rightBound) throws IndexAccessException {
        if (leftBound == rightBound)
            return leftBound;
        int u = rand.nextInt(rightBound - leftBound) + leftBound;
        data.swap(u, rightBound);
        return partition(leftBound, rightBound);
    }

    /**
     * Computes the k-th smallest element in index, searching only in restricted range
     *
     * @param k          the index of element as if array was sorted
     * @param leftBound  the left bound of restricted range
     * @param rightBound the right bound of restricted range
     * @return the value of k-th smallest element
     * @throws IndexAccessException is thrown on index access issues
     */
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

    @Override
    public double select(int k) throws IndexAccessException {
        if (computed.containsKey(k))
            return computed.get(k);

        Range range = getRange(k);
        return selectInRange(k, range.leftBound, range.rightBound);
    }
}
