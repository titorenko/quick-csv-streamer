package uk.elementarysoftware.quickcsv.parser;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Pools large, long-living byte arrays to minimise old generation GC */
class BufferPool {
	
	private final int bufferSize;
	
	private final Queue<byte[]> buffers = new ConcurrentLinkedQueue<byte[]>();


	BufferPool(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	byte[] getBuffer() {
		byte[] result = buffers.poll();
		return result == null ? new byte[bufferSize] : result;
	}
	
	void handBack(byte[] buffer) {
		buffers.add(buffer);
	}
	
	void clear() {
		buffers.clear();
	}
}