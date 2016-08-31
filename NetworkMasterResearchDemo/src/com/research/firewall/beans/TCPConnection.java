package com.research.firewall.beans;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map.Entry;

public class TCPConnection {
	public String key;//a
    public long randomNum;//b
    public long sequenceNum1;//c
    public long sequenceNum2;//d
    public long acknowlegementNum;//e
    public int f;
    public PacketBean packet;//g
    public SocketChannel socketChannel;//h
    public boolean i;
    public SelectionKey selectionKey;//j
    private static MLinkedHashMap<String,TCPConnection> hashMap = new MLinkedHashMap<String, TCPConnection>(50, new MVPNInterface<String, TCPConnection>() {

		@Override
		public void cleanup(Entry<String, TCPConnection> entry) {
			try {
				entry.getValue().socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	});
    
    public TCPConnection(String pKey, long pRandomNum, long pSequenceNum1, long psequenceNum2, long pAcknowlegementNum, SocketChannel pSocketChannel, PacketBean pPacket) {
        super();
        this.key = pKey;
        this.randomNum = pRandomNum;
        this.sequenceNum1 = pSequenceNum1;
        this.sequenceNum2 = psequenceNum2;
        this.acknowlegementNum = pAcknowlegementNum;
        this.socketChannel = pSocketChannel;
        this.packet = pPacket;
    }
    
    /***
     * get tcp conn
     * @param str
     * @return
     */
    public static TCPConnection getTCB(String str) {
    	TCPConnection result = null;
        synchronized (hashMap) {
        	result = (TCPConnection) hashMap.get(str);
        }
        return result;
    }

    
    /***
     * clean cache
     */
    public static void cleanTcbCache() {
        hashMap.clear();
    }
    
    /**
     * put
     * @param str
     * @param conn
     */
    public static void putTCB(String str, TCPConnection conn) {
        synchronized (hashMap) {
            hashMap.put(str, conn);
        }
    }

    
    public static void closeAll() {
        synchronized ("1") {
            Iterator it = hashMap.entrySet().iterator();
            while (it.hasNext()) {
            	try {
					((Entry<String, TCPConnection>) it.next()).getValue().socketChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                it.remove();
            }
        }
    }
    
    public static void closeTCB(TCPConnection conn) {
    	try {
			conn.socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        synchronized ("1") {
        	hashMap.remove(conn.key);
        }
    }


}
