package com.research.firewall.beans;

/***
 * 这个类是/proc/net/tcp6一行数据代表的对象
 * @author lenvo
 *
 */
public class TCPBean {
	/**a*/
	public String localAddress;
	/**b*/
    public String remoteAddress;
    /**c*/
    public String localPort;
    /**d*/
    public String remotePort;
    /**e*/
    public int status;
    /**f*/
    public int uid;
    
    public TCPBean() {
    	super();
        this.uid = -1;
        this.localAddress = "0.0.0.0";
        this.localPort = "0";
        this.remoteAddress = "0.0.0.0";
        this.remotePort = "0";
        this.status = 0;
	}
}
