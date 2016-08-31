package com.research.netspeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.research.network.main.R;
import com.research.util.NetSpeedUtil;
import com.research.util.ProcNetUtil;

public class NetSpeedActivity extends Activity {
	// tmp total running list size
	private int runningListSize = 0;
	// last update app list time
	private long lastReloadTCPConnCountTime = 0;
	// total speed download
	public long totalDownSpeed = 0;
	public long totalUpSpeed = 0;
	private long totalDownloadBytes = 0;
	private long totalUploadBytes = 0;
	// update total ui time
	private long updateTotalUITime = 0;
	// net name
	private TextView netTypeName = null;
	//
	private TextView totalSpeedDown = null;
	//
	private TextView totalSpeedUp = null;
	// list view
	private ListView netSpeedListView = null;
	// adapter
	private NetworkSpeedAdapter adapter = null;
	// app list
	public ArrayList<NetworkApp> dataList = new ArrayList<NetworkApp>();
	// list view data
	public ArrayList<NetworkApp> listViewData = new ArrayList<NetworkApp>();
	// installed pkgs
	private List<PackageInfo> installedPkgs = null;
	// refresh data thread
	private RefreshDataThread thread = null;
	// Wifi Manager
	private WifiManager wifiManager = null;
	// Telephony Manger
	private TelephonyManager telephonyManager = null;
	// Connectivity Manager
	private ConnectivityManager connectivityManager = null;
	// Activity Manager
	private ActivityManager activityManager = null;
	// Package Manager
	private PackageManager pkgManager = null;
	// Handler
	public Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				ArrayList<NetworkApp> arrayList = (ArrayList<NetworkApp>) msg.obj;
				if(arrayList == null || arrayList.size() == 0){
					return;
				}
				// update ui
				listViewData.clear();
				for (int i = 0; i < arrayList.size(); i++) {
					if(i == 6) break;
					listViewData.add(arrayList.get(i));
				}
				// update total ui
				updateTotalUI();
				//
				adapter.notifyDataSetChanged();
//				netSpeedListView.setAdapter(adapter);
				arrayList.clear();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//
		setContentView(R.layout.network_speed);
		// init wifi manager
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		// init telephonyManger
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// connectivityManager
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// activity manger
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// pkg Manager
		pkgManager = getPackageManager();
		// init view
		initView(this);
		// set net type name
		setNetTypeName();
	}

	private void initView(NetSpeedActivity context) {
		// 运营商名
		netTypeName = (TextView) findViewById(R.id.carrieroperator);
		// 总下载数
		totalSpeedDown = (TextView) findViewById(R.id.network_download_speed_num);
		// 总上传数
		totalSpeedUp = (TextView) findViewById(R.id.network_upload_speed_num);
		// listView
		netSpeedListView = (ListView) findViewById(R.id.net_speed_listview);
		// init adapter
		adapter = new NetworkSpeedAdapter(context);
		//
		netSpeedListView.setAdapter(adapter);
	}

	/***
	 * set net type Name
	 */
	private void setNetTypeName() {
		// 注意国外运营商可能返回null 需通过mnc号查询
		String netTypeName = telephonyManager.getNetworkOperatorName();
		// get networkType
		NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
		int networkType = NetSpeedUtil.getType(mNetworkInfo);
		setNetworkState(networkType, netTypeName);
	}

	public void setNetworkState(int netType, String str) {
		CharSequence charSequence = "";
		switch (netType) {
		case 4096:
			charSequence = NetSpeedUtil.removeQuotedString(wifiManager
					.getConnectionInfo().getSSID());
			break;
		case 8193:
			if (!TextUtils.isEmpty(str)) {
				charSequence = str + " " + "2G网络";
				break;
			} else {
				charSequence = "2G网络" + " " + "移动网络";
				break;
			}
		case 8194:
			if (!TextUtils.isEmpty(str)) {
				charSequence = str + " " + "3G网络";
				break;
			} else {
				charSequence = "3G网络" + " " + "移动网络";
				break;
			}
		case 8196:
			if (!TextUtils.isEmpty(str)) {
				charSequence = str + " " + "4G网络";
				break;
			} else {
				charSequence = "4G网络" + " " + "移动网络";
				break;
			}
		case -1:
			charSequence = "未连接任何网络";
			break;
		}
		netTypeName.setText(charSequence);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (thread == null || !thread.isAlive()) {
			thread = new RefreshDataThread(this);
			thread.start();
		}
	}

	/***
	 * update listview apps
	 * 
	 * @param paramBool
	 */
	public synchronized void updateAppList(boolean paramBool) {
		List<RunningAppProcessInfo> runningAppProcesses = activityManager
				.getRunningAppProcesses();
		List<RunningServiceInfo> runningServices = activityManager
				.getRunningServices(500);

		if (paramBool
				|| !(runningAppProcesses == null || runningServices == null || runningAppProcesses
						.size() + runningServices.size() == runningListSize)) {
			// current time
			long curTime = System.currentTimeMillis();
			// reload tcp conn count
			if (curTime - lastReloadTCPConnCountTime > 10000) {
				System.err.println("111");
				ProcNetUtil.reloadActiveTcpConnections(this);
				lastReloadTCPConnCountTime = System.currentTimeMillis();
			}
			//
			synchronized ("11") {
				installedPkgs = pkgManager.getInstalledPackages(0);
			}
			// 优化ignore list(white list) 此处省略
			//
			runningListSize = runningAppProcesses.size()
					+ runningServices.size();
			// reset listview data
			dataList.clear();
			Map hashMap = new HashMap();
			for (int i = 0; i < runningAppProcesses.size(); i++) {
				hashMap.put(runningAppProcesses.get(i).processName,
						runningAppProcesses.get(i));
			}
			for (int i = 0; i < runningServices.size(); i++) {
				hashMap.put(runningServices.get(i).service.getPackageName(),
						runningServices.get(i));
			}
			//
			for (PackageInfo pkgInfo : installedPkgs) {
				if (!(hashMap.get(pkgInfo.packageName) == null /* || whitelist */)) {
					NetworkApp app = new NetworkApp();
					app.setName((String) pkgInfo.applicationInfo
							.loadLabel(pkgManager));
					app.setVersion(pkgInfo.versionName);
					app.setFirstInstallTime(pkgInfo.firstInstallTime);
					app.setSystemApp(((pkgInfo.applicationInfo.flags & 1) != 0));
					app.setPkgName(pkgInfo.packageName);
					app.setUid(pkgInfo.applicationInfo.uid);
					app.setLastDownData(TrafficStats
							.getUidRxBytes(pkgInfo.applicationInfo.uid));
					app.setLastUpData(TrafficStats
							.getUidRxBytes(pkgInfo.applicationInfo.uid));
					app.setLastRefreshTime(System.currentTimeMillis());
					app.setTcpEstablishedCount(ProcNetUtil.getTcpConnections(
							app.getPkgName(), ProcNetUtil.TCP_ESTABLISHED));
					app.setTcpListencount(ProcNetUtil.getTcpConnections(
							app.getPkgName(), ProcNetUtil.TCP_LISTEN));
					//
					if (app.getDownSpeedLong() + app.getUpSpeedLong() > 0
							&& app.getTcpEstablishedCount()
									+ app.getTcpListencount() <= 0) {
						app.setTcpEstablishedCount(1);
					}
					dataList.add(app);
				}
			}
		}
	}

	/**
	 * update ui
	 */
	private void updateTotalUI(){
		String uploadSpeedStr = null;
		String downloadSpeedStr = null;
		//总的发送字节数，包含Mobile和WiFi等 
		long totalTxBytes = TrafficStats.getTotalTxBytes();
		//获取总的接受字节数，包含Mobile和WiFi等
		long totalRxBytes = TrafficStats.getTotalRxBytes();
		long deltaUpdateUITime = System.currentTimeMillis() - this.updateTotalUITime;
		if(deltaUpdateUITime != 0){
			long tmpDownloadSpeed = (totalRxBytes - this.totalDownloadBytes) * 1000 / deltaUpdateUITime;
			//download speed correct
			if(tmpDownloadSpeed > this.totalDownSpeed){
				if(listViewData.size() > 0){
					listViewData.get(0).setDownSpeedLong(listViewData.get(0).getDownSpeedLong() + tmpDownloadSpeed - this.totalDownSpeed);
					listViewData.get(0).setDownSpeed(NetSpeedUtil.formatSpeedNum(this, listViewData.get(0).getDownSpeedLong(), "0") + "/s");
				}
				downloadSpeedStr = NetSpeedUtil.formatSpeedNum(this, tmpDownloadSpeed, "0");
			}else{
				downloadSpeedStr = NetSpeedUtil.formatSpeedNum(this, this.totalDownSpeed, "0");
			}
			//upload speed correct
			long tmpUploadSpeed = (totalTxBytes - this.totalUploadBytes) * 1000 / deltaUpdateUITime;
			if(tmpUploadSpeed > this.totalUpSpeed){
				if(listViewData.size() > 0){
					listViewData.get(0).setUpSpeedLong(listViewData.get(0).getUpSpeedLong() + tmpUploadSpeed - this.totalUpSpeed);
					listViewData.get(0).setUpSpeed(NetSpeedUtil.formatSpeedNum(this, listViewData.get(0).getUpSpeedLong(), "0") + "/s");
				}
				uploadSpeedStr = NetSpeedUtil.formatSpeedNum(this, tmpUploadSpeed, "0");
			}else{
				uploadSpeedStr = NetSpeedUtil.formatSpeedNum(this, this.totalUpSpeed, "0");
			}
			
			totalSpeedUp.setText(uploadSpeedStr + "/s");
			totalSpeedDown.setText(downloadSpeedStr + "/s");
			this.updateTotalUITime = System.currentTimeMillis();
			this.totalDownloadBytes = totalRxBytes;
			this.totalUploadBytes = totalTxBytes;
			this.totalDownSpeed = 0;
			this.totalUpSpeed = 0;
		}
	}
}
