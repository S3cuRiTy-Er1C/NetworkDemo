package com.research.netspeed;

import android.util.Log;

public class NetworkApp implements Comparable{
	private boolean isSystemApp;
	private boolean isIgnoreApp;
	private int tcpEstablishedCount = 0;
	private int tcpListencount = 0;
	private int uid = 0;
    private long downSpeedLong = 0;
    private long upSpeedLong = 0;
    private long lastDownData = 0;
    private long lastUpData = 0;
    private long lastRefreshTime = 0;
    private long size = 0;
    private long date = 0;
    private String name = "";
    private String downSpeed = "";
    private String upspeed = "";
    private String pName = "";
    private String version = "";
    
    public NetworkApp() {
    	this.isSystemApp = false;
    	this.isIgnoreApp = false;
        this.tcpEstablishedCount = 0;
        this.tcpListencount = 0;
        this.uid = 0;
        this.downSpeedLong = 0;
        this.upSpeedLong = 0;
        this.lastDownData  = 0;
        this.lastUpData = 0;
        this.lastRefreshTime = 0;
        this.size = 0;
        this.date = 0;
	}
    
	@Override
	public int compareTo(Object another) {
		int result = -1;
		NetworkApp tmpApp = (NetworkApp) another;
		if(getDownSpeedLong() + getUpSpeedLong() > tmpApp.getDownSpeedLong() + tmpApp.getUpSpeedLong()){
			result = -1;
			return result;
		}
		if(getDownSpeedLong() + getUpSpeedLong() < tmpApp.getDownSpeedLong() + tmpApp.getUpSpeedLong()){
			result = 1;
			return result;
		}
		if(getTcpListencount() + getTcpEstablishedCount() > tmpApp.getTcpListencount() + tmpApp.getTcpEstablishedCount()){
			result = -1;
			return result;
		}
		if(getTcpListencount() + getTcpEstablishedCount() < tmpApp.getTcpListencount() + tmpApp.getTcpEstablishedCount()){
			result = 1;
			return result;
		}
		if(!isSystemApp() && tmpApp.isSystemApp()){
			result = -1;
			return result;
		}
		if(isSystemApp() && !tmpApp.isSystemApp()){
			result = 1;
			return result;
		}
		if(getLastDownData() > tmpApp.getLastDownData()){
			result = -1;
			return result;
		}
		if(getLastDownData() < tmpApp.getLastDownData()){
			result = 1;
			return result;
		}
		tmpApp = null;
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDownSpeedLong() {
		return downSpeedLong;
	}

	public void setDownSpeedLong(long downSpeedLong) {
		this.downSpeedLong = downSpeedLong;
	}

	public String getDownSpeed() {
		return downSpeed;
	}

	public void setDownSpeed(String downSpeed) {
		this.downSpeed = downSpeed;
	}

	public long getUpSpeedLong() {
		return upSpeedLong;
	}

	public void setUpSpeedLong(long upspeedLong) {
		if(upspeedLong < 0){
//			if(upspeedLong < 0){
//				Log.w("NETDEMO", "under 0 pkgName:"+getPkgName() + " tmpUpspeedLong:"+upspeedLong);
//			}
			upspeedLong = 0;
		}
		this.upSpeedLong = upspeedLong;
	}

	public String getUpspeed() {
		return upspeed;
	}

	public void setUpSpeed(String upspeed) {
		this.upspeed = upspeed;
	}

	public String getPkgName() {
		return pName;
	}

	public void setPkgName(String pName) {
		this.pName = pName;
	}

	public long getLastDownData() {
		return lastDownData;
	}

	public void setLastDownData(long lastDownData) {
		this.lastDownData = lastDownData;
	}

	public long getLastUpData() {
		return lastUpData;
	}

	public void setLastUpData(long lastUpData) {
		this.lastUpData = lastUpData;
	}

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getDate() {
		return date;
	}

	public void setFirstInstallTime(long date) {
		this.date = date;
	}

	public boolean isSystemApp() {
		return isSystemApp;
	}

	public void setSystemApp(boolean pIsSysApp) {
		this.isSystemApp = pIsSysApp;
	}

	public boolean isIgnoreApp() {
		return isIgnoreApp;
	}

	public void setIgnoreApp(boolean isIgnoreApp) {
		this.isIgnoreApp = isIgnoreApp;
	}

	public int getTcpEstablishedCount() {
		return tcpEstablishedCount;
	}

	public void setTcpEstablishedCount(int tcpEstablishedCount) {
		this.tcpEstablishedCount = tcpEstablishedCount;
	}

	public int getTcpListencount() {
		return tcpListencount;
	}

	public void setTcpListencount(int tcpListencount) {
		this.tcpListencount = tcpListencount;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	
	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("app:"+getName());
		sBuilder.append("&& pkg:"+getPkgName());
		sBuilder.append("&& download speed:"+getDownSpeed());
		sBuilder.append("&& upload speed:"+getUpspeed());
		sBuilder.append("&& upload total:"+getLastUpData());
		sBuilder.append("&& tcp conn count:"+(getTcpEstablishedCount()+ getTcpListencount()));
		return sBuilder.toString();
	}
}
