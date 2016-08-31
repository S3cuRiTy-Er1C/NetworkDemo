package com.research.firewall.beans;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.text.TextUtils;

import com.research.firewall.MVpnService;
import com.research.util.ByteBufferUtil;
import com.research.util.ProcNetUtil;

/***
 * bug 可能比较多,2016-08-26
 * @author eric li
 *
 */
public class MainBean implements Callable<String> {
	private MVpnService mVpn = null;
	//
	private ConcurrentLinkedQueue<PacketBean> udpOutputQueue;
	private ConcurrentLinkedQueue<PacketBean> tcpOutputQueue;
	private ConcurrentLinkedQueue<ByteBuffer> inputQueue;
	//
	private String state = "";

	public MainBean(MVpnService pVpn,ConcurrentLinkedQueue<PacketBean> pUdpOutput,ConcurrentLinkedQueue<PacketBean> pTcpOutput,ConcurrentLinkedQueue<ByteBuffer>pInput){
		mVpn = pVpn;
		udpOutputQueue = pUdpOutput;
		tcpOutputQueue = pTcpOutput;
		inputQueue = pInput;
	}
	
	@Override
	public String call() throws Exception {
		int tmpInt = 1;
		int tmpInt2 = 0;
		state = "running";
		ByteBuffer byteBuffer = null;
		PacketBean packet = null;
		Object obj = null;
		while(true){
			//
			if(Thread.interrupted()){
				mVpn.closeVPN();
				break;
			}
			//
			try {
				if(tmpInt != 0){
					byteBuffer = ByteBufferUtil.acquire();
				}else{
					byteBuffer.clear();
				}
				//restart callable
				MVpnService.restartTask(mVpn);
				//
				if(mVpn.fileDescInputChannel.read(byteBuffer) <= 0){
					tmpInt = 0;
					try {
						obj = inputQueue.poll();
						if(obj != null){
							((ByteBuffer) obj).flip();
							//禁止发包
							String pkgName = ProcNetUtil.getPkgBySrcIp(mVpn, new PacketBean(((ByteBuffer)obj).duplicate()).ip4Header.sourceAddress.getHostAddress());
							if(!TextUtils.isEmpty(pkgName) && mVpn.pkgNameList.contains(pkgName)){
								((ByteBuffer)obj).clear();
								continue;
							}
							//
							while(((ByteBuffer)obj).hasRemaining()){
								mVpn.fileDescOutputChannel.write((ByteBuffer) obj);
							}
							ByteBufferUtil.release((ByteBuffer) obj);
							tmpInt2 = 1;
							//
							if(tmpInt == 0 && tmpInt2 == 0){
								Thread.sleep(60);
							}
							//
							Thread.sleep(10);
							continue;
						}else{
							tmpInt2 = 0;
						}
					} catch (Exception e) {
						//	e.printStackTrace();
						mVpn.Log("main thread exception but still running:"+e.getMessage());
						continue;
					}
				}
			} catch (Exception e) {
				mVpn.Log("main thread exception but still running:"+e.getMessage());
				continue;
				//e.printStackTrace();
			}
			//
			try {
				byteBuffer.flip();
				packet = new PacketBean(byteBuffer);
				//
				String pkgName = ProcNetUtil.getPkgByDestIp(mVpn, packet.ip4Header.destinationAddress.getHostAddress());
				if(!TextUtils.isEmpty(pkgName) && mVpn.pkgNameList.contains(pkgName)){
					byteBuffer.clear();
					tmpInt = 0;
					continue;
				}
				//
				try {
					if(packet.isUDP()){
						udpOutputQueue.offer(packet);
						tmpInt = 1;
					}else if(packet.isTCP()){
						tcpOutputQueue.offer(packet);
						tmpInt = 1;
					}else{
						mVpn.Log("unknown packet type"+packet.ip4Header.toString());
						tmpInt = 0;
					}
					//
					try {
						obj = inputQueue.poll();
						if(obj != null){
							((ByteBuffer) obj).flip();
							//禁止发包
							pkgName = "";
							pkgName = ProcNetUtil.getPkgBySrcIp(mVpn, new PacketBean(((ByteBuffer)obj).duplicate()).ip4Header.sourceAddress.getHostAddress());
							if(!TextUtils.isEmpty(pkgName) && mVpn.pkgNameList.contains(pkgName)){
								((ByteBuffer)obj).clear();
								continue;
							}
							//
							while(((ByteBuffer)obj).hasRemaining()){
								mVpn.fileDescOutputChannel.write((ByteBuffer) obj);
							}
							ByteBufferUtil.release((ByteBuffer) obj);
							//
							if(tmpInt == 0 && tmpInt2 == 0){
								Thread.sleep(60);
							}
							//
							Thread.sleep(10);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
				//	e.printStackTrace();
					tmpInt = 1;
					mVpn.Log("main thread exception but staill running:"+e.getMessage());
				}
			} catch (Exception e) {
				tmpInt = 1;
				mVpn.Log("main thread exception but staill running:"+e.getMessage());
			}
			
		}
		return state;
	}

}
