package com.research.firewall.beans;

import java.nio.ByteBuffer;

public class TCPHeader {
	public int sourcePort;//a
    public int destinationPort;//b
    public long sequenceNumber;//c
    public long acknowledgementNumber;//d
    public byte e;//e
    public int headerLength;//f
    public byte flags;//g
    public int window;//h
    public int checksum;//i
    public int j;
    public byte[] k;
    
    public TCPHeader(ByteBuffer byteBuffer, byte arg2) {
        this(byteBuffer);
    }

    private TCPHeader(ByteBuffer byteBuffer) {
        super();
        this.sourcePort = byteBuffer.getShort() & 65535;
        this.destinationPort = byteBuffer.getShort() & 65535;
        this.sequenceNumber = (((long)byteBuffer.getInt())) & 4294967295L;
        this.acknowledgementNumber = (((long)byteBuffer.getInt())) & 4294967295L;
        this.e = byteBuffer.get();
        this.headerLength = (this.e & 240) >> 2;
        this.flags = byteBuffer.get();
        this.window = byteBuffer.getShort() & 65535;
        this.checksum = byteBuffer.getShort() & 65535;
        this.j = byteBuffer.getShort() & 65535;
        int v0 = this.headerLength - 20;
        if(v0 > 0) {
            this.k = new byte[v0];
            byteBuffer.get(this.k, 0, v0);
        }
    }
    
    public final boolean isACK() {
        boolean v0 = (this.flags & 16) == 16 ? true : false;
        return v0;
    }

    public final boolean isFIN() {
        boolean v0 = true;
        if((this.flags & 1) != 1) {
            v0 = false;
        }

        return v0;
    }

    public final boolean isPSH() {
        boolean v0 = (this.flags & 8) == 8 ? true : false;
        return v0;
    }

    public final boolean isRST() {
        boolean v0 = (this.flags & 4) == 4 ? true : false;
        return v0;
    }

    public final boolean isSYN() {
        boolean v0 = (this.flags & 2) == 2 ? true : false;
        return v0;
    }

    public final boolean isURG() {
        boolean v0 = (this.flags & 32) == 32 ? true : false;
        return v0;
    }
    
    public final String toString() {
        StringBuilder sBuilder = new StringBuilder("TCPHeader{");
        sBuilder.append("sourcePort=").append(this.sourcePort);
        sBuilder.append(", destinationPort=").append(this.destinationPort);
        sBuilder.append(", sequenceNumber=").append(this.sequenceNumber);
        sBuilder.append(", acknowledgementNumber=").append(this.acknowledgementNumber);
        sBuilder.append(", headerLength=").append(this.headerLength);
        sBuilder.append(", window=").append(this.window);
        sBuilder.append(", checksum=").append(this.checksum);
        sBuilder.append(", flags=");
        if(this.isFIN()) {
            sBuilder.append(" FIN");
        }

        if(this.isSYN()) {
            sBuilder.append(" SYN");
        }

        if(this.isRST()) {
            sBuilder.append(" RST");
        }

        if(this.isPSH()) {
            sBuilder.append(" PSH");
        }

        if(this.isACK()) {
            sBuilder.append(" ACK");
        }

        if(this.isURG()) {
            sBuilder.append(" URG");
        }

        sBuilder.append('}');
        return sBuilder.toString();
    }
}
