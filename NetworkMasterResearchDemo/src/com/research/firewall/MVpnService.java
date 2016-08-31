package com.research.firewall;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.research.firewall.beans.MainBean;
import com.research.firewall.beans.TCPConnection;
import com.research.firewall.beans.TCPInputBean;
import com.research.firewall.beans.TCPOutputBean;
import com.research.firewall.beans.UDPInputBean;
import com.research.firewall.beans.UDPOutputBean;
import com.research.network.main.MainActivity;
import com.research.util.ByteBufferUtil;
import com.research.util.Util;

public class MVpnService extends VpnService implements Runnable {
	// vpn thread
	private Thread vpnThread = null;
	//
	private ParcelFileDescriptor fileDescriptor = null;
	// block app list
	public ArrayList<String> pkgNameList = new ArrayList<String>();
	//
	private Selector udpSelector = null;
	private Selector tcpSelector = null;
	//
	private ConcurrentLinkedQueue udpOutputQueue = null;
	private ConcurrentLinkedQueue tcpOutputQueue = null;
	private ConcurrentLinkedQueue inputQueue = null;
	// Executor service
	private ExecutorService executorService = null;
	// File Channel
	public FileChannel fileDescInputChannel = null;
	public FileChannel fileDescOutputChannel = null;
	//
	public Future<String> tcpInputFuture;
	public Future<String> udpOutputFuture;
	public Future<String> udpInputFuture;
	public Future<String> tcpOutputFuture;
	public Future<String> mainBeanFuture;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//
		if (vpnThread != null) {
			vpnThread.interrupt();
			vpnThread = null;
		}
		//
		vpnThread = new Thread(this);
		vpnThread.start();
		return 1;
	}

	@Override
	public void onDestroy() {
		if (vpnThread != null) {
			vpnThread.interrupt();
			vpnThread = null;
		}
		if(fileDescriptor != null){
			try {
				fileDescriptor.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeVPN();
		
		super.onDestroy();
	}

	@SuppressLint("NewApi") @Override
	public synchronized void run() {
		synchronized (this) {
			Log("setup vpn");
			if (fileDescriptor == null || !Util.isGreaterEqualsThanAndroid21()) {
				// config vpn
				VpnService.Builder builder = new VpnService.Builder();
				builder.setMtu(1500);
				builder.addAddress("10.0.8.1", 32);
				builder.addRoute("0.0.0.0", 0);
				// version >= 21
				PackageManager packageManager = getPackageManager();
				List<ApplicationInfo> list = packageManager
						.getInstalledApplications(0);
				if (list != null && list.size() > 0) {
					Iterator<ApplicationInfo> iterator = list.iterator();
					while (iterator.hasNext()) {
						ApplicationInfo obj = iterator.next();
						pkgNameList.add(obj.packageName);
						// add disAllowedApplication
						if (Util.isGreaterEqualsThanAndroid21()) {
							try {
								if(!obj.packageName.equals("com.research.datagramsocket") && !obj.packageName.equals("com.fly.wifiadb")){
//									builder.addDisallowedApplication(obj.packageName);
									builder.addAllowedApplication(obj.packageName);
									Log(obj.packageName);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				// set session
				builder.setSession("Security VPN");
				fileDescriptor = builder.establish();
				// -------------------
				Log("block app number:" + pkgNameList.size());
				// target is androi 15~21 [15,21)
				if (!Util.isGreaterEqualsThanAndroid21()) {
					// set block app list
					// ...
					// init
					try {
						// init something
						udpSelector = Selector.open();
						tcpSelector = Selector.open();
						// init concurrent queue
						udpOutputQueue = new ConcurrentLinkedQueue();
						tcpOutputQueue = new ConcurrentLinkedQueue();
						inputQueue = new ConcurrentLinkedQueue();
						// shutdown executorService
						if (executorService != null
								&& !executorService.isTerminated()) {
							executorService.shutdownNow();
						}
						boolean isExecPoolReleased = executorService == null
								|| executorService.awaitTermination(10,
										TimeUnit.SECONDS) ? true : false;
						if (isExecPoolReleased && fileDescriptor != null) {
							// file channel
							fileDescInputChannel = new FileInputStream(
									fileDescriptor.getFileDescriptor())
									.getChannel();
							fileDescOutputChannel = new FileOutputStream(
									fileDescriptor.getFileDescriptor())
									.getChannel();
							// exec pool init
							executorService = Executors.newFixedThreadPool(5);
							// future
							udpInputFuture = executorService
									.submit(new UDPInputBean(inputQueue,
											udpSelector));
							udpOutputFuture = executorService
									.submit(new UDPOutputBean(udpOutputQueue,
											udpSelector, this));
							tcpInputFuture = executorService
									.submit(new TCPInputBean(inputQueue,
											tcpSelector));
							tcpOutputFuture = executorService
									.submit(new TCPOutputBean(tcpOutputQueue,
											inputQueue, tcpSelector, this));
							mainBeanFuture = executorService.submit(new MainBean(this, udpOutputQueue, tcpOutputQueue, inputQueue));
							// initFutureAndSubmit
						}
						Log("setup vpn success");
						//
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}

	}

	public void Log(String logstr) {
		Log.w(MainActivity.TAG, logstr);
	}

	/***
	 * restart future
	 * 
	 * @param mVpn
	 */
	public static void restartTask(MVpnService mVpn) {
		if (mVpn.executorService != null && !mVpn.executorService.isShutdown()) {
			// tcp input restart
			if ((mVpn.tcpInputFuture == null || mVpn.tcpInputFuture.isDone() || mVpn.tcpInputFuture
					.isCancelled()) && !Thread.interrupted()) {
				mVpn.tcpInputFuture = mVpn.executorService
						.submit(new TCPInputBean(mVpn.inputQueue,
								mVpn.tcpSelector));
				mVpn.Log("tcp input task restarted");
			}
			// tcp output restart
			if ((mVpn.tcpOutputFuture == null || mVpn.tcpOutputFuture.isDone() || mVpn.tcpOutputFuture
					.isCancelled()) && !Thread.interrupted()) {
				mVpn.tcpOutputFuture = mVpn.executorService
						.submit(new TCPOutputBean(mVpn.tcpOutputQueue,
								mVpn.inputQueue, mVpn.tcpSelector, mVpn));
				mVpn.Log("tcp output task restarted");
			}
			// udp input task
			if ((mVpn.udpInputFuture == null || mVpn.udpInputFuture.isDone() || mVpn.udpInputFuture
					.isCancelled()) && !Thread.interrupted()) {
				mVpn.udpInputFuture = mVpn.executorService
						.submit(new UDPInputBean(mVpn.inputQueue,
								mVpn.udpSelector));
				mVpn.Log("udp input task restarted");
			}
			// udp output task
			if ((mVpn.udpOutputFuture == null || mVpn.udpOutputFuture.isDone() || mVpn.udpOutputFuture
					.isCancelled()) && !Thread.interrupted()) {
				mVpn.udpOutputFuture = mVpn.executorService
						.submit(new UDPOutputBean(mVpn.udpOutputQueue,
								mVpn.udpSelector, mVpn));
				mVpn.Log("udp output task restarted");
			}
		}
	}
	
	public void closeVPN(){
		try {
			if(fileDescriptor != null){
				try {
					fileDescriptor.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//close pool
				if(executorService != null){
					executorService.shutdownNow();
				}
				//
				udpOutputQueue = null;
				tcpOutputQueue = null;
				inputQueue = null;
				//
				ByteBufferUtil.clear();
				TCPConnection.cleanTcbCache();
				pkgNameList.clear();
				//
				if(fileDescInputChannel != null){
					fileDescInputChannel.close();
				}
				if(fileDescOutputChannel != null){
					fileDescOutputChannel.close();
				}
				if(tcpSelector != null){
					tcpSelector.close();
				}
				if(udpSelector != null){
					udpSelector.close();
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onRevoke() {
		if (vpnThread != null) {
			vpnThread.interrupt();
			vpnThread = null;
		}
		if(fileDescriptor != null){
			try {
				fileDescriptor.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeVPN();
		super.onRevoke();
	}
}
