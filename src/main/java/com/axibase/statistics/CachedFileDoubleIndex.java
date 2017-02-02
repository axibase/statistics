package com.axibase.statistics;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

class CachedFileDoubleIndex extends DoubleIndex {
    private static final int PAGE_SIZE = 1 << 16;
    private static final int MAX_PAGES = 10;

    private int length;
    private RandomAccessFile file;
    private CacheNode[] pages;
    private int cachedCount = 0;

    private CacheNode head, tail;

    class CacheNode {
        CacheNode next, prev;
        int index, offset;
        byte[] data;
        ByteBuffer buffer;
        DoubleBuffer doubleBuffer;
    }

    CachedFileDoubleIndex(String path, double[] data) throws IOException {
        file = new RandomAccessFile(path, "rw");
    }

    void addValue(double value) throws IOException {
        if (!Double.isNaN(value)) {
            set(length, value);
            length++;
        }
    }

    void completeCreation() {
        int size = length * DOUBLE_SIZE;
        int pageCount = (size + PAGE_SIZE) / PAGE_SIZE;
        pages = new CacheNode[pageCount];
    }

    public int length() {
        return length;
    }

    private void load(int pageIndex) throws IOException {
        CacheNode c;

        if (cachedCount == MAX_PAGES) {
            c = dropOutdated();
        } else {
            c = new CacheNode();
            c.data = new byte[PAGE_SIZE];
        }

        c.index = pageIndex;
        c.offset = pageIndex * PAGE_SIZE;

        if (head == null) {
            head = tail = c;
        } else {
            c.next = head;
            head.prev = c;
            head = c;
        }

        file.seek(c.offset);
        file.read(c.data);
        c.buffer = ByteBuffer.wrap(c.data);
        c.doubleBuffer = c.buffer.asDoubleBuffer();

        pages[pageIndex] = c;

        cachedCount++;
    }

    private void touchPage(int pageIndex) throws IOException {
        CacheNode page = pages[pageIndex];
        if (page == null)
            load(pageIndex);
        else if (page != head) {
            if (page.prev != null)
                page.prev.next = page.next;
            if (page.next != null)
                page.next.prev = page.prev;
            if (page == tail)
                tail = page.prev;
            page.prev = null;
            page.next = head;
            page.next.prev = page;
            head = page;
        }
    }

    private CacheNode dropOutdated() throws IOException {
        CacheNode out = tail;
        tail = out.prev;
        tail.next = null;
        pages[out.index] = null;

        file.seek(out.offset);
        file.write(out.data);

        cachedCount--;

        return out;
    }

    private int pageIndexOf(int i) {
        return i * DOUBLE_SIZE / PAGE_SIZE;
    }

    public double get(int i) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        int needPage = pageIndexOf(i);
        touchPage(needPage);
        return pages[needPage].doubleBuffer.get(i % (PAGE_SIZE / DOUBLE_SIZE));
    }

    public void set(int i, double v) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        int needPage = pageIndexOf(i);
        touchPage(needPage);
        pages[needPage].doubleBuffer.put(i % (PAGE_SIZE / DOUBLE_SIZE), v);
    }
}