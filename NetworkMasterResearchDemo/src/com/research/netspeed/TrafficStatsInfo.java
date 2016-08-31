package com.research.netspeed;

public class TrafficStatsInfo {
	public long idx;
    public String iface;
    public long acct_tag_hex;
    public int uid_tag_int = -1;
    public long cnt_set;
    public long rx_bytes;
    public long rx_packets;
    public long tx_bytes;
    public long tx_packets;
    public long rx_tcp_bytes;
    public long rx_tcp_packets;
    public long rx_udp_bytes;
    public long rx_udp_packets;
    public long rx_other_bytes;
    public long rx_other_packets;
    public long tx_tcp_bytes;
    public long tx_tcp_packets;
    public long tx_udp_bytes;
    public long tx_udp_packets;
    public long tx_other_bytes;
    public long tx_other_packets;

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TrafficStatsInfo bdVar = (TrafficStatsInfo) obj;
        if (this.uid_tag_int == bdVar.uid_tag_int && this.cnt_set == bdVar.cnt_set && this.rx_bytes == bdVar.rx_bytes && this.tx_bytes == bdVar.tx_bytes) {
            return this.iface.equals(bdVar.iface);
        }
        return false;
    }

    public final int hashCode() {
        return (((((((this.iface.hashCode() * 31) + this.uid_tag_int) * 31) + ((int) (this.cnt_set ^ (this.cnt_set >>> 32)))) * 31) + ((int) (this.rx_bytes ^ (this.rx_bytes >>> 32)))) * 31) + ((int) (this.tx_bytes ^ (this.tx_bytes >>> 32)));
    }
    
    public final String toString() {
        return "TrafficStatsInfo{idx=" + this.idx + ", iface='" + this.iface + '\'' + ", acct_tag_hex='" + this.acct_tag_hex + '\'' + ", uid_tag_int=" + 
       this.uid_tag_int + ", cnt_set=" + this.cnt_set + ", rx_bytes=" + this.rx_bytes + ", rx_packets=" + this.rx_packets + 
       ", tx_bytes=" + this.tx_bytes + ", tx_packets=" + this.tx_packets + ", rx_tcp_bytes=" + this.rx_tcp_bytes + ", rx_tcp_packets=" + this.rx_tcp_packets + 
       ", rx_udp_bytes=" + this.rx_udp_bytes + ", rx_udp_packets=" + this.rx_udp_packets + ", rx_other_bytes=" + this.rx_other_bytes + ", rx_other_packets=" + this.rx_other_packets + ", tx_tcp_bytes=" + this.tx_tcp_bytes + 
       ", tx_tcp_packets=" + this.tx_tcp_packets + ", tx_udp_bytes=" + this.tx_udp_bytes + ", tx_udp_packets=" + this.tx_udp_packets + ", tx_other_bytes=" + this.tx_other_bytes + ", tx_other_packets=" + this.tx_other_packets + '}';
    }
}