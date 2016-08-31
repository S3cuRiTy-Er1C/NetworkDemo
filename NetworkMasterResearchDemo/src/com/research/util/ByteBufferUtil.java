package com.research.util;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ByteBufferUtil {
	private static ConcurrentLinkedQueue<ByteBuffer> concurrentLinkedQueue = new ConcurrentLinkedQueue();

	public static ByteBuffer acquire() {
		ByteBuffer byteBuffer = (ByteBuffer) concurrentLinkedQueue.poll();
		if (byteBuffer == null) {
			return ByteBuffer.allocateDirect(32767);
		}
		return byteBuffer;
	}

	public static void release(ByteBuffer byteBuffer) {
		byteBuffer.clear();
		concurrentLinkedQueue.offer(byteBuffer);
	}

	public static void clear() {
		concurrentLinkedQueue.clear();
	}

}
