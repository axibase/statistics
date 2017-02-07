package com.axibase.statistics;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Files;

/**
 * This class stores indexed double-valued data on disk
 * and provides simple caching strategy for faster access
 */
public class CachedFileDoubleIndex extends BaseDoubleIndex {
    private static final int DEFAULT_PAGE_SIZE = 1 << 16;
    private static final int DEFAULT_MAX_PAGES = 10;

    private int pageSize, maxPages;

    private int length;
    private File indexPath;
    private RandomAccessFile indexFile;
    private CacheNode currentPage;
    private CacheNode[] pages;
    private int cachedCount = 0;
    private int currentIndex = -1;

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

    /**
     * Creates a new CachedFileDoubleIndex. The new file is created if it doesn't exist,
     * otherwise the files is overwritten
     *
     * @param path     the path to underlying file of this index
     * @param pageSize the size of singe caching uint (page)
     * @param maxPages the maximum number of pages that can be stored in memory for this index
     */
    public CachedFileDoubleIndex(String path, int pageSize, int maxPages) throws IOException {
        this.indexPath = new File(path);
        indexFile = new RandomAccessFile(path, "rw");
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    /**
     * Adds new value at the end of index. This changes the {@link #length() length}
     *
     * @param value the value to append
     * @throws IndexAccessException is thrown if the new value could not be added
     */
    public void addValue(double value) throws IndexAccessException {
        if (!Double.isNaN(value)) {
            length++;
            set(length - 1, value);
        }
    }

    /**
     * Completes insertion. This method should be called after all values inserted
     * and before any element accessed
     */
    public void completeInsertion() {
        int size = length * DOUBLE_SIZE;
        int pageCount = (size + pageSize) / pageSize;
        currentPage = null;
        pages = new CacheNode[pageCount];
    }

    /**
     * Closes and removes underlying file
     *
     * @throws IOException if the file can't be closed or deleted
     */
    public void close() throws IOException {
        indexFile.close();
        Files.delete(indexPath.toPath());
    }

    @Override
    public int length() {
        return length;
    }

    /**
     * Loads requested page in memory, unloads old pages
     * if <code>maxPages</code> limit is reached.
     */
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

        indexFile.seek(c.offset);
        indexFile.read(c.data);
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

    /**
     * Update the state of requested page in cache, or load it if needed
     */
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

    /**
     * Remove old pages from cache
     */
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

        indexFile.seek(out.offset);
        indexFile.write(out.data);


        return out;
    }

    /**
     * Get or load load requested page by index
     */
    private CacheNode getPageFor(int i) throws IOException {
        int needPage = i * DOUBLE_SIZE / pageSize;
        touchPage(needPage);

        if (pages == null)
            return currentPage;
        else
            return pages[needPage];
    }

    @Override
    public double get(int i) throws IndexAccessException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        try {
            return getPageFor(i).doubleBuffer.get(i % (pageSize / DOUBLE_SIZE));
        } catch (IOException e) {
            throw new IndexAccessException("Get value I/O error" + e.toString(), e);
        }
    }

    @Override
    public void set(int i, double v) throws IndexAccessException {
        if (i < 0 || i >= length)
            throw new IndexOutOfBoundsException();

        try {
            getPageFor(i).doubleBuffer.put(i % (pageSize / DOUBLE_SIZE), v);
        } catch (IOException e) {
            throw new IndexAccessException("Set value I/O error" + e.toString(), e);
        }
    }
}
