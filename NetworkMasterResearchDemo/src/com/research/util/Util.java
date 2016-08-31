package com.research.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.research.firewall.beans.TCPBean;
import com.research.network.main.MainActivity;

public class Util {
	private final static String TAG = "NETDEMO";
	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 20, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
	
	/***
	 * current system version 
	 * @return true if >= android 5.0.1
	 */
	public static boolean isGreaterEqualsThanAndroid21(){
		boolean result = false;
		if(Build.VERSION.SDK_INT >= 21){
			result = true;
		}
		return result;
	}
	
	/***
	 * check ip:port is used
	 * @param ip
	 * @param port
	 * @param timeout
	 * @return
	 */
	public static boolean isPortOpen(String ip, int port, int timeout) {
        boolean result = false;
        Socket socket = null;
        
        try {
        	socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port),timeout);
			result = true;
		} catch (Exception e) {
		//	e.printStackTrace();
		}finally{
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return result;
    }

	
	/**
	 * 
	 */
	public static ArrayList<String> calculateIpPool(MainActivity main) {
		//
		ArrayList<String> arrayList = new ArrayList<String>();
		// get wifimanager
		WifiManager wifiManager = null;
		wifiManager = (WifiManager) main.getSystemService("wifi");
		// conn
		WifiInfo connectionInfo = wifiManager.getConnectionInfo();
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		//
		if (!(connectionInfo == null || dhcpInfo == null)) {
			try {
				int netPrefix = 0;
				int nCount = 0;
				//
				if (VERSION.SDK_INT >= 21) {
					NetworkInterface byInetAddress = NetworkInterface
							.getByInetAddress(InetAddress
									.getByAddress(BigInteger.valueOf(
											IPUtil.ipToInt(dhcpInfo.ipAddress))
											.toByteArray()));
					if (byInetAddress != null) {
						for (InterfaceAddress interfaceAddress : byInetAddress
								.getInterfaceAddresses()) {
							if (interfaceAddress.getBroadcast() != null) {
								netPrefix = interfaceAddress
										.getNetworkPrefixLength();
								break;
							}
						}
					}
				}
				if (netPrefix >= 32 || netPrefix <= 0) {
					netPrefix = IPUtil.getNetPrefix(dhcpInfo.netmask);
					nCount = (-1 - netPrefix) - 1;
				} else {
					nCount = (1 << (32 - netPrefix)) - 2;
					netPrefix = ((1 << (32 - netPrefix)) - 1) ^ -1;
				}
				main.Log("NetMask:" + dhcpInfo.netmask);
				main.Log("Gateway:" + netPrefix);
				main.Log("nCount  :" + nCount);
				// ipHost
				int ip = IPUtil.ipToInt(dhcpInfo.ipAddress);
				netPrefix &= ip;
				main.Log("ipHost   :"
						+ IPUtil.ipToString(IPUtil.ipToInt(netPrefix)));
				//
				netPrefix++;
				if (nCount >= 1024) {
					nCount = (ip & -256) + 1;
					netPrefix = 254;
				} else {
					int tmpInt = netPrefix;
					netPrefix = nCount;
					nCount = tmpInt;
				}
				String tmpString = "";
				main.Log("cur ip    :" + IPUtil.ipToString(IPUtil.ipToInt(ip)));
				tmpString = IPUtil.ipToString(IPUtil.ipToInt(nCount));
				arrayList.add(tmpString);
				int count = 0;
				while (count < netPrefix) {
					ip = nCount + 1;
					tmpString = IPUtil.ipToString(IPUtil.ipToInt(nCount));
					arrayList.add(tmpString);
					nCount = ip;
					count++;
				}
				// remove itself
				arrayList.remove(IPUtil.ipToString(dhcpInfo.ipAddress));
				arrayList.remove(IPUtil.ipToString(dhcpInfo.gateway));
				// log
				main.Log("IP Pool Size:" + arrayList.size());
				main.Log("---------------------");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return arrayList;
	}

	/**
	 * Send empty Packet to Ip:137
	 * @param ip
	 */
	public static void sendPack(final String ip) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					byte[] bytes = new byte[32];
					DatagramSocket datagramSocket = new DatagramSocket();
					datagramSocket.send(new DatagramPacket(bytes, 32, InetAddress
							.getByName(ip), 137));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		//
		if(threadPoolExecutor != null){
			threadPoolExecutor.submit(runnable);
		}
	}

	/***
	 * cache arp set
	 */
	public static void cacheArp(MainActivity main) {
		ArrayList<String> ipPool = calculateIpPool(main);
		for (int i = 0; i < ipPool.size(); i++) {
			String ipStr = ipPool.get(i);
			main.Log("IP Pool->" + ipStr);
			//send pack to ip
			sendPack(ipStr);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/***
	 * cache arp set
	 */
	public static void cacheArp(MainActivity main,ArrayList<String>ipPool) {
		for (int i = 0; i < ipPool.size(); i++) {
			String ipStr = ipPool.get(i);
			main.Log("IP Pool->" + ipStr);
			//send pack to ip
			sendPack(ipStr);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * show accessibility setting
	 * 
	 * @param context
	 * @return
	 */
	public static void showAccessibilitySettings(Activity context) {
		Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
		int v1 = 1586;
		try {
			context.startActivityForResult(intent, v1);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

	}


	/***
	 * check pc port 137 current unused
	 * 
	 * @param ip
	 * @return response
	 * @throws Exception
	 */
	public static String[] checkPort137(String str) throws Exception {
		Log.w(TAG, "" + str);
		DatagramSocket datagramSocket = new DatagramSocket();
		byte[] bArr = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
				(byte) 0, (byte) 0, (byte) 32, (byte) 67, (byte) 75, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 65,
				(byte) 65, (byte) 65, (byte) 65, (byte) 65, (byte) 0, (byte) 0,
				(byte) 33, (byte) 0, (byte) 1 };
		datagramSocket.setSoTimeout(300);
		datagramSocket.send(new DatagramPacket(bArr, 50, InetAddress
				.getByName(str), 137));
		bArr = new byte[256];
		datagramSocket.receive(new DatagramPacket(bArr, 256));
		datagramSocket.close();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				bArr);
		byteArrayInputStream.skip(50);
		byteArrayInputStream.skip(4);
		byteArrayInputStream.skip(2);
		int read = byteArrayInputStream.read();
		String[] strArr = new String[read];
		for (int i = 0; i < read; i++) {
			byte[] bArr2 = new byte[18];
			byteArrayInputStream.read(bArr2);
			String str2 = "UTF-8";
			Locale locale = Locale.getDefault();
			if (locale.getLanguage().equalsIgnoreCase("zh")) {
				str2 = locale.getCountry().equalsIgnoreCase("CN") ? "GBK"
						: "Big5";
			}
			strArr[i] = new String(bArr2, 0, 16, str2).trim();
		}
		return strArr;
	}

	/**
	 * get ip address
	 * 
	 * @return
	 */
	public static String getIpAddress() {
		String hostAddress;
		String str = "";
		try {
			Enumeration networkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				Enumeration inetAddresses = ((NetworkInterface) networkInterfaces
						.nextElement()).getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) inetAddresses
							.nextElement();
					if (inetAddress.isSiteLocalAddress()) {
						hostAddress = inetAddress.getHostAddress();
					} else {
						hostAddress = str;
					}
					str = hostAddress;
				}
			}
			Log.w(TAG, "getIpAddress: " + str);
			return str;
		} catch (Exception e) {
			Exception exception = e;
			hostAddress = str;
			Log.w(TAG, "getIpAddress exception: " + exception.getMessage());
			return hostAddress;
		}
	}

	/***
	 * goto pkgName's setting detail view
	 * 
	 * @param pkgName
	 */
	public static void gotoSettings(Activity activity, String pkgName) {
		// Display display = activity.getWindowManager().getDefaultDisplay();
		// LayoutParams layoutParams = activity.getWindow().getAttributes();
		// layoutParams.width = display.getWidth();
		// activity.getWindow().setAttributes(layoutParams);

		Intent intent = new Intent();
		intent.setFlags(536936448);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		int sdkVer = Build.VERSION.SDK_INT;
		if (sdkVer >= 19) {
			intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			intent.setData(Uri.fromParts("package", pkgName, null));
		} else {
			String tmpStr = sdkVer == 8 ? "pkg"
					: "com.android.settings.ApplicationPkgName";
			intent.setAction("android.intent.action.VIEW");
			intent.setClassName("com.android.settings",
					"com.android.settings.InstalledAppDetails");
			intent.putExtra(tmpStr, pkgName);
		}
		try {
			activity.startActivityForResult(intent, 1024);
			activity.overridePendingTransition(0, 0);
			System.err.println("123");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * kill process by pkgName
	 * 
	 * @param main
	 * @param pkgName
	 */
	public static void killProcess(Activity main, String pkgName) {
		ActivityManager aManager = (ActivityManager) main
				.getSystemService("activity");
		// restart pkg
//		aManager.restartPackage(pkgName);
//		// force stop need permission
//		try {
//			Method declaredMethod = aManager.getClass().getDeclaredMethod(
//					"forceStopPackage", new Class[] { String.class });
//			declaredMethod.setAccessible(true);
//			declaredMethod.invoke(aManager, new Object[] { pkgName });
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		// kill background processes
		aManager.killBackgroundProcesses(pkgName);
	}

	/***
	 * shutdown thread pool
	 */
	public static void onDestroy(){
		threadPoolExecutor.shutdown();
		threadPoolExecutor = null;
	}
}
