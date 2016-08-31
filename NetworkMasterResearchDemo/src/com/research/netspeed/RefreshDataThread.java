package com.research.netspeed;

import java.util.ArrayList;
import java.util.Collections;

import android.net.TrafficStats;
import android.os.Message;
import android.util.Log;

import com.research.util.NetSpeedUtil;

/***
 * 流量更新线程
 * 
 * @author Eric
 * 
 */
public class RefreshDataThread extends Thread {
	private NetSpeedActivity main;

	public RefreshDataThread(NetSpeedActivity main) {
		this.main = main;
	}

	@Override
	public void run() {
		while (main != null && !main.isFinishing()) {
			try {
				// get current time
				long currentTimeMillis = System.currentTimeMillis();
				//
				final ArrayList<NetworkApp> arrayList = new ArrayList<NetworkApp>();
				//set app data
				for (int i = 0; i < main.dataList.size(); i++) {
					NetworkApp app = main.dataList.get(i);
					// 计算上次更新状态时间差
					long deltaUpdateTime = System.currentTimeMillis()
							- app.getLastRefreshTime();
					if (deltaUpdateTime != 0) {
						//set download
						// 获取某个网络UID的接受字节数
						long tmpUidRxBytes = TrafficStats.getUidRxBytes(app.getUid());
						long lastDownSpeed = (tmpUidRxBytes - app.getLastDownData()) * 1000 / deltaUpdateTime;
						app.setDownSpeed(NetSpeedUtil.formatSpeedNum(main, lastDownSpeed, "0B") + "/s");
						app.setDownSpeedLong(lastDownSpeed);
						app.setLastDownData(tmpUidRxBytes);
						//set upload
						long tmpUidTxBytes = TrafficStats.getUidTxBytes(app.getUid());
						long lastUpSpeed = (tmpUidTxBytes - app.getLastUpData()) * 1000 / deltaUpdateTime;
						app.setUpSpeed(NetSpeedUtil.formatSpeedNum(main, lastUpSpeed, "0B") + "/s");
						app.setUpSpeedLong(lastUpSpeed);
						app.setLastUpData(tmpUidTxBytes);
						//
						app.setLastRefreshTime(System.currentTimeMillis());
						//
						arrayList.add(app);
						
						//set total download speed long
						main.totalDownSpeed += app.getDownSpeedLong();
						//set total upload speed long
						main.totalUpSpeed += app.getUpSpeedLong();
					}
				}
//				Log.w("NETDEMO", "total up speed:"+main.totalUpSpeed);
				//sort item
				Collections.sort(arrayList);
				//update main app list
				main.updateAppList(false);
				//update ui
				Message message = main.handler.obtainMessage();
				message.obj = arrayList;
				message.what = 1;
				main.handler.sendMessage(message);
				//sleep
				if(System.currentTimeMillis() - currentTimeMillis < 1000){
					if(main.listViewData.size() == 0){
						Thread.sleep(0);
					}else{
						Thread.sleep(1000 - (System.currentTimeMillis() - currentTimeMillis));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		main.totalDownSpeed = 0;
		main.totalUpSpeed = 0;
	}
}
