package com.research.firewall.beans;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.research.firewall.MVpnService;
import com.research.util.ByteBufferUtil;

public class UDPOutputBean implements Callable<String>{
	private MVpnService mVpn;
	//
	private ConcurrentLinkedQueue<PacketBean> queue;
	//
	private Selector selector;
	//
	private String state = "";
	//
	private MLinkedHashMap<String, DatagramChannel> hashMap = new MLinkedHashMap<String, DatagramChannel>(50, new MVPNInterface<String, DatagramChannel>() {
		@Override
		public void cleanup(Entry<String, DatagramChannel> entry) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}); 
	
	public UDPOutputBean(ConcurrentLinkedQueue pQueue,Selector pSelector,MVpnService pVpnService) {
		this.queue = pQueue;
		this.selector = pSelector;
		this.mVpn = pVpnService;
	}
	
	@Override
	public String call(){
		state = "running";
		//current thread
		Thread curThread = Thread.currentThread();
		while(true){
			PacketBean packet = queue.poll();
			if(packet == null){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//
				if(!curThread.isInterrupted()){
					continue;
				}
			}
			if(curThread.isInterrupted()){
				break;
			}
			DatagramChannel open = null;
			InetAddress inetAddress = packet.ip4Header.destinationAddress;
			int destinationPort = packet.udpHeader.destinationPort;
			String datagramChannelKey = inetAddress.getHostAddress()+":"+":"+packet.udpHeader.sourcePort;
			DatagramChannel datagramChannel = hashMap.get(datagramChannelKey);
			if(datagramChannel == null){
				try {
					open = DatagramChannel.open();
					open.connect(new InetSocketAddress(inetAddress, destinationPort));
					open.configureBlocking(false);
					packet.swapSourceAndDestination();
					selector.wakeup();
					open.register(selector, 1, packet);
					mVpn.protect(open.socket());
					hashMap.put(datagramChannelKey, open);
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						open.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//release byte buffer
					ByteBufferUtil.release(packet.byteBuffer);
					//release hashmap
					Iterator it = this.hashMap.entrySet().iterator();
			        while (it.hasNext()) {
			            try {
			                ((DatagramChannel) ((Entry) it.next()).getValue()).close();
			            } catch (IOException e) {
			            }
			            it.remove();
			        }

				}
			}else{
				open = datagramChannel;
			}
			//
			try {
				ByteBuffer byteBuffer = packet.byteBuffer;
				while(byteBuffer.hasRemaining()){
					open.write(byteBuffer);
				}
			} catch (Exception e) {
				e.printStackTrace();
				hashMap.remove(datagramChannelKey);
			}finally{
				try {
					open.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ByteBufferUtil.release(packet.byteBuffer);
		}
		state = "";
		return state;
	}

}
