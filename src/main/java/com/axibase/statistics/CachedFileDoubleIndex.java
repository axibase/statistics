package com.axibase.statistics;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class CachedFileDoubleIndex extends DoubleIndex {
    private static final int DEFAULT_PAGE_SIZE = 1 << 16;
    private static final int DEFAULT_MAX_PAGES = 10;

    private int pageSize, maxPages;

    private int length;
    private File file;
    private RandomAccessFile raf;
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
        this(path, DEFAULT_PAGE_SIZE, DEFAULT_MAX_PAGES);
    }

    public CachedFileDoubleIndex(String path, int pageSize, int maxPages) throws IOException {
        file = new File(path);
        raf = new RandomAccessFile(path, "rw");
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public void addValue(double value) throws IOException {
        if (!Double.isNaN(value)) {
            length++;
            set(length - 1, value);
        }
    }

    public void completeCreation() {
        int size = length * DOUBLE_SIZE;
        int pageCount = (size + pageSize) / pageSize;
        currentPage = null;
        pages = new CacheNode[pageCount];
    }

    public void close() throws IOException {
        raf.close();
        file.delete();
    }

    public int length() {
        return length;
    }

    private void load(int pageIndex) throws IOException {
        CacheNode c;

        if ((pages == null && currentPage == null) ||
                (pages != null && cachedCount < maxPages)) {
            c = new CacheNode();
            c.data = new byte[pageSize];
        } else {
            c = dropOutdated();
        }

        c.index = pageIndex;
        c.offset = pageIndex * pageSize;

        raf.seek(c.offset);
        raf.read(c.data);
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

        raf.seek(out.offset);
        raf.write(out.data);


        return out;
    }

    private CacheNode getPageFor(int i) throws IOException {
        int needPage = i * DOUBLE_SIZE / pageSize;
        touchPage(needPage);

        if (pages == null)
            return currentPage;
        else
            return pages[needPage];
    }

    public double get(int i) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        return getPageFor(i).doubleBuffer.get(i % (pageSize / DOUBLE_SIZE));
    }

    public void set(int i, double v) throws IOException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        getPageFor(i).doubleBuffer.put(i % (pageSize / DOUBLE_SIZE), v);
    }
}