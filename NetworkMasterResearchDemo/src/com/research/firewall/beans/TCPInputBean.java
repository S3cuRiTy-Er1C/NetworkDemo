package com.research.firewall.beans;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.research.util.ByteBufferUtil;

import android.util.Log;

public class TCPInputBean implements Callable<String>{
	private String state = "";
	//queue
	private ConcurrentLinkedQueue<ByteBuffer> queue;
	//selector
	private Selector selector = null;
	
	public TCPInputBean(ConcurrentLinkedQueue<ByteBuffer> pQueue,Selector pSelector) {
		this.queue = pQueue;
		this.selector = pSelector;
	}
	
	@Override
	public String call() throws Exception {
		state = "runing";
		while(!Thread.interrupted()){
			try {
				if(selector.select() == 0){
					Thread.sleep(10);
				}else{
					Iterator iterator = selector.selectedKeys().iterator();
					while(iterator.hasNext() && !Thread.interrupted()){
						SelectionKey selectionKey  = (SelectionKey) iterator.next();
						if(selectionKey.isValid()){
							TCPConnection tcpConn = null;
							PacketBean packet = null;
							ByteBuffer acquire;
							if(selectionKey.isConnectable()){
								//connect able
								tcpConn = (TCPConnection) selectionKey.attachment();
								packet = tcpConn.packet;
								//
								try {
									if(tcpConn.socketChannel.finishConnect()){
										iterator.remove();
										tcpConn.f = 2;
										acquire = ByteBufferUtil.acquire();
										packet.updateTCPBuffer(acquire, (byte) 18, tcpConn.randomNum, tcpConn.sequenceNum2, 0);
										queue.offer(acquire);
										tcpConn.randomNum ++;
										selectionKey.interestOps(1);
									}
								} catch (Exception e) {
									e.printStackTrace();
									acquire = ByteBufferUtil.acquire();
									packet.updateTCPBuffer(acquire, (byte) 4, 9, tcpConn.sequenceNum2, 0);
									queue.offer(acquire);
									TCPConnection.closeTCB(tcpConn);
								}
							}else if(selectionKey.isReadable()){
								//read able
								iterator.remove();
								acquire = ByteBufferUtil.acquire();
								acquire.position(40);
								tcpConn = (TCPConnection) selectionKey.attachment();
								synchronized ("11") {
									packet = tcpConn.packet;
									try {
										int read = ((SocketChannel)selectionKey.channel()).read(acquire);
										if(read == -1){
											selectionKey.interestOps(0);
											tcpConn.i = false;
											if(tcpConn.f != 4){
												ByteBufferUtil.release(acquire);
											}else{
												tcpConn.f = 5;
												packet.updateTCPBuffer(acquire, (byte) 1, tcpConn.randomNum, tcpConn.sequenceNum2, 0);
												tcpConn.randomNum ++;
											}
										}else{
											packet.updateTCPBuffer(acquire, (byte) 24, tcpConn.randomNum, tcpConn.sequenceNum2, read);
											tcpConn.randomNum += read;
											acquire.position(read+40);
										}
										queue.offer(acquire);
									} catch (Exception e) {
										e.printStackTrace();
										Log.w("NETDEMO","TCPINPUT Network read error");
										packet.updateTCPBuffer(acquire, (byte) 4, 0, tcpConn.sequenceNum2, 0);
										queue.offer(acquire);
										TCPConnection.closeTCB(tcpConn);
									}
								}
							}else{
								continue;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.w("NETDEMO","tcp input stopping exception");
				Log.w("NETDEMO","tcp input thread finally end.");
				e.printStackTrace();
			}
		}
		state = "";
		return state;
	}

}
