package com.axibase.statistics;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class CachedFileDoubleIndex extends DoubleIndex {
    private static final int PAGE_SIZE = 1 << 16;
    private static final int MAX_PAGES = 10;

    private int length;
    private RandomAccessFile file;
    private CacheNode currentPage;
    private CacheNode[] pages;
    private int cachedCount = 0, currentIndex = -1;

    private CacheNode head, tail;

    class CacheNode {
        CacheNode next, prev;
        int index;
        long offset;
        byte[] data;
        ByteBuffer buffer;
        DoubleBuffer doubleBuffer;
    }

    public CachedFileDoubleIndex(String path) throws IOException {
        file = new RandomAccessFile(path, "rw");
    }

    public void addValue(double value) throws IOException {
        if (!Double.isNaN(value)) {
            length++;
            set(length - 1, value);
        }
    }

    public void completeCreation() {
        int size = length * DOUBLE_SIZE;
        int pageCount = (size + PAGE_SIZE) / PAGE_SIZE;
        currentPage = null;
        pages = new CacheNode[pageCount];
    }

    public int length() {
        return length;
    }

    private void load(int pageIndex) throws IOException {
        CacheNode c;

        if ((pages == null && currentPage == null) ||
                (pages != null && cachedCount < MAX_PAGES)) {
            c = new CacheNode();
            c.data = new byte[PAGE_SIZE];
        } else {
            c = dropOutdated();
        }

        c.index = pageIndex;
        c.offset = pageIndex * PAGE_SIZE;

        file.seek(c.offset);
        file.read(c.data);
        c.buffer = ByteBuffer.wrap(c.data);
        c.doubleBuffer = c.buffer.asDoubleBuffer();

        if (pages != null) {
            if (head == null) {
                head = tail = c;
            } else {
                c.next = head;
                head.prev = c;
                head = c;
            }

            pages[pageIndex] = c;
            cachedCount++;
        } else {
            currentPage = c;
            currentIndex = pageIndex;
        }
    }

    private void touchPage(int pageIndex) throws IOException {
        CacheNode page;

        if (pages != null)
            page = pages[pageIndex];
        else
            page = currentPage;

        if (page == null || (pages == null && pageIndex != currentIndex))
            load(pageIndex);
        else if (pages != null && page != head) {
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
        CacheNode out;

        if (pages != null) {
            out = tail;
            tail = out.prev;
            tail.next = null;
            pages[out.index] = null;

            cachedCount--;
        } else
            out = currentPage;

        file.seek(out.offset);
        file.write(out.data);


        return out;
    }

    private CacheNode getPageFor(int i) throws IOException {
        int needPage = i * DOUBLE_SIZE / PAGE_SIZE;
        touchPage(needPage);

        if (pages == null)
            return currentPage;
        else
            return pages[needPage];
    }

    public double get(int i) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        return getPageFor(i).doubleBuffer.get(i % (PAGE_SIZE / DOUBLE_SIZE));
    }

    public void set(int i, double v) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        getPageFor(i).doubleBuffer.put(i % (PAGE_SIZE / DOUBLE_SIZE), v);
    }
}