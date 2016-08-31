package com.research.firewall.beans;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.research.util.ByteBufferUtil;

public class UDPInputBean implements Callable<String> {
	private Selector selector;
	private ConcurrentLinkedQueue<ByteBuffer> concurrentLinkedQueue;
	private String state;

	public UDPInputBean(ConcurrentLinkedQueue<ByteBuffer> pQueue, Selector pSelector) {
		concurrentLinkedQueue = pQueue;
		selector = pSelector;
	}

	@Override
	public String call() {
		state = "running";
		while (!Thread.interrupted()) {
			try {
				if(selector.select() == 0){
					Thread.sleep(10);
				}else{
					Iterator iterator = selector.selectedKeys().iterator();
					while(iterator.hasNext() && !Thread.interrupted()){
						SelectionKey selectionKey = (SelectionKey) iterator.next();
						if(selectionKey.isValid() && selectionKey.isReadable()){
							iterator.remove();
							ByteBuffer acquireBuffer = ByteBufferUtil.acquire();
							acquireBuffer.position(28);
							int read = ((DatagramChannel) selectionKey.channel()).read(acquireBuffer);
							
							((PacketBean) selectionKey.attachment()).updateUDPBuffer(acquireBuffer,read);
							acquireBuffer.position(read + 28);
							concurrentLinkedQueue.offer(acquireBuffer);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return state;
	}
}
