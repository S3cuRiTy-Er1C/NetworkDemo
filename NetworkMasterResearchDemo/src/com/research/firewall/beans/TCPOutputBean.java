package com.research.firewall.beans;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;

import com.research.firewall.MVpnService;
import com.research.util.ByteBufferUtil;

public class TCPOutputBean implements Callable<String> {
	private MVpnService mVpn;
	private ConcurrentLinkedQueue<PacketBean> packets;// b
	private ConcurrentLinkedQueue<ByteBuffer> byteBuffers;// c
	private Selector selector;
	private String state;
	private Random random;

	public TCPOutputBean(ConcurrentLinkedQueue pQueue1,
			ConcurrentLinkedQueue pQueue2, Selector arg4, MVpnService pVpn) {
		super();
		this.state = "";
		this.random = new Random();
		this.packets = pQueue1;
		this.byteBuffers = pQueue2;
		this.selector = arg4;
		this.mVpn = pVpn;
	}

	@Override
	public String call() throws Exception {
		state = "running";
		Log.w("NETDEMO", "TCP output stared");
		Thread currentThread = Thread.currentThread();
		while (true) {
			PacketBean packet = packets.poll();
			if (packet == null) {
				Thread.sleep(10);
				if (!currentThread.isInterrupted()) {
					continue;
				}
			}
			if (currentThread.isInterrupted()) {
				break;
			}
			ByteBuffer byteBuffer = packet.byteBuffer;
			packet.byteBuffer = null;
			ByteBuffer acquire = ByteBufferUtil.acquire();
			InetAddress inetAddress = packet.ip4Header.destinationAddress;
			// tcp header
			TCPHeader tcpHeader = packet.tcpHeader;
			int port = tcpHeader.destinationPort;
			String key = inetAddress.getHostAddress() + ":" + port + ":"
					+ tcpHeader.sourcePort;
			TCPConnection tcpConn = TCPConnection.getTCB(key);
			if (tcpConn == null) {
				packet.swapSourceAndDestination();
				if (tcpHeader.isSYN()) {//握手协议
					// syn
					SocketChannel open = SocketChannel.open();
					open.configureBlocking(false);
					mVpn.protect(open.socket());
					TCPConnection tmpConn = new TCPConnection(key,
							(long) random.nextInt(32768),
							tcpHeader.sequenceNumber,
							tcpHeader.sequenceNumber + 1,
							tcpHeader.acknowledgementNumber, open, packet);
					TCPConnection.putTCB(key, tmpConn);
					//
					try {
						open.connect(new InetSocketAddress(inetAddress, port));
						if (open.finishConnect()) {
							tmpConn.f = 2;
							packet.updateTCPBuffer(acquire, (byte) 18,
									tmpConn.randomNum, tmpConn.sequenceNum2, 0);
							tmpConn.randomNum++;
						} else {
							tmpConn.f = 1;
							selector.wakeup();
							tmpConn.selectionKey = open.register(selector, 8,
									tmpConn);
						}
					} catch (Exception e) {
						Log.w("NETDEMO",
								"TCP Output connection error:" + e.getMessage());
						e.printStackTrace();
						packet.updateTCPBuffer(acquire, (byte) 4, 0,
								tcpHeader.acknowledgementNumber, 0);
						TCPConnection.closeTCB(tmpConn);
					}
				} else {
					packet.updateTCPBuffer(acquire, (byte) 4, 0,
							tcpHeader.sequenceNumber + 1, 0);
				}
				//
				byteBuffers.offer(acquire);
			} else if (tcpHeader.isSYN()) {
				//is syn
				synchronized ("2") {
					if (tcpConn.f == 1) {
						tcpConn.sequenceNum2 = tcpHeader.sequenceNumber + 1;
					} else {
						updateAndCloseTCP(tcpConn,1,acquire);
					}
				}
			}else if(tcpHeader.isRST()){
				//is rst
				closeTCPAndBuffer(tcpConn,acquire);
			}else if(tcpHeader.isFIN()){
				synchronized ("123") {
					packet = tcpConn.packet;
					tcpConn.sequenceNum2 = tcpHeader.sequenceNumber + 1;
					tcpConn.acknowlegementNum = tcpHeader.acknowledgementNumber;
					if(tcpConn.i){
						tcpConn.f = 4;
						packet.updateTCPBuffer(acquire, (byte) 16, tcpConn.randomNum, tcpConn.sequenceNum2, 0);
					}else{
						tcpConn.f = 5;
						packet.updateTCPBuffer(acquire, (byte) 17, tcpConn.randomNum, tcpConn.sequenceNum2, 0);
						tcpConn.randomNum ++;
					}
				}
				//
				try {
					byteBuffers.offer(acquire);
				} catch (Exception e) {
					Log.w("NETDEMO", "TCPOutput stopping execption");
					e.printStackTrace();
					TCPConnection.closeAll();
				}
			}else if(tcpHeader.isACK()){
				operateACK(tcpConn,tcpHeader,byteBuffer,acquire);
			}
			if(acquire.position() == 0){
				ByteBufferUtil.release(acquire);
			}
			ByteBufferUtil.release(byteBuffer);
		}
		Log.w("NETDEMO","TCP output finally thread end");
		TCPConnection.closeAll();
		state = "";
		return state;
	}
	
	private void operateACK(TCPConnection pTCPConn,TCPHeader pTcpHeader,ByteBuffer byteBuffer,ByteBuffer byteBuffer2){
		int limit = byteBuffer.limit() - byteBuffer.position();
        synchronized (pTCPConn) {
            SocketChannel socketChannel = pTCPConn.socketChannel;
            if (pTCPConn.f == 2) {
                pTCPConn.f = 3;
                selector.wakeup();
                try {
					pTCPConn.selectionKey = socketChannel.register(selector, 1, pTCPConn);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
                pTCPConn.i = true;
            } else if (pTCPConn.f == 5) {
            	closeTCPAndBuffer(pTCPConn, byteBuffer2);
                return;
            }
            if (limit == 0) {
                return;
            }
            if (!pTCPConn.i) {
                selector.wakeup();
                pTCPConn.selectionKey.interestOps(1);
                pTCPConn.i = true;
            }
            while (byteBuffer.hasRemaining()) {
                try {
                    socketChannel.write(byteBuffer);
                } catch (IOException e) {
                    Log.w("NETDEMO","TCPOutput Network write error");
                    updateAndCloseTCP(pTCPConn, limit, byteBuffer2);
                    return;
                }
            }
            pTCPConn.sequenceNum2 = ((long) limit) + pTcpHeader.sequenceNumber;
            pTCPConn.acknowlegementNum = pTcpHeader.acknowledgementNumber;
            pTCPConn.packet.updateTCPBuffer(byteBuffer2, (byte) 16, pTCPConn.randomNum, pTCPConn.sequenceNum2, 0);
            byteBuffers.offer(byteBuffer2);
        }
    }


	private void updateAndCloseTCP(TCPConnection tcpConn, int i, ByteBuffer byteBuffer) {
		tcpConn.packet.updateTCPBuffer(byteBuffer, (byte) 4, 0, tcpConn.sequenceNum2 + ((long)i), 0);
		//insert to tail
		byteBuffers.offer(byteBuffer);
		TCPConnection.closeTCB(tcpConn);
	}
	
	
	private static void closeTCPAndBuffer(TCPConnection iVar, ByteBuffer byteBuffer) {
        ByteBufferUtil.release(byteBuffer);
        TCPConnection.closeTCB(iVar);
    }


}
