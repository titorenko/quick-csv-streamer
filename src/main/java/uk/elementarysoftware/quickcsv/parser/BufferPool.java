package uk.elementarysoftware.quickcsv.parser;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** Pools large, long-living byte arrays to minimise old generation GC */
class BufferPool {

    private final int bufferSize;
    private final AtomicInteger buffersCreated = new AtomicInteger(0);
    private final Queue<byte[]> buffers = new ConcurrentLinkedQueue<byte[]>();

    BufferPool(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    byte[] getBuffer() {
        byte[] result = buffers.poll();
        if (result == null) {
            buffersCreated.incrementAndGet();
            return new byte[bufferSize];
        } else {
            return result;
        }
    }

    void handBack(byte[] buffer) {
        buffers.add(buffer);
        if (buffers.size() >= buffersCreated.get()) {
            clear();
        }
    }

    private void clear() {
        buffers.clear();
        buffersCreated.set(0);
    }
}