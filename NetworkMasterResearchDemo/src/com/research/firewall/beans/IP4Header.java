package com.research.firewall.beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IP4Header {
	public byte version;
    public byte ihl;
    public int c;
    public short serviceType;
    public int totalLength;
    public int identificationAndFlagsAndFragmentOffset;
    public short ttl;
    public HeaderType headerType;
    public int headerChecksum;
    public InetAddress sourceAddress;
    public InetAddress destinationAddress;//k
    private short headerTypeInt;

    public IP4Header(ByteBuffer byteBuffer,byte pbyte) throws Exception{
    	this(byteBuffer);
    }
    
    private IP4Header(ByteBuffer byteBuffer) throws Exception {
    	int v0 = byteBuffer.get();
        this.version = ((byte)(v0 >> 4));
        this.ihl = ((byte)(v0 & 15));
        this.c = this.ihl << 2;
        this.serviceType = ((short)(byteBuffer.get() & 255));
        this.totalLength = byteBuffer.getShort() & 65535;
        this.identificationAndFlagsAndFragmentOffset = byteBuffer.getInt();
        this.ttl = ((short)(byteBuffer.get() & 255));
        this.headerTypeInt = ((short)(byteBuffer.get() & 255));
        this.headerType = HeaderType.getHeaderType(headerTypeInt);
        this.headerChecksum = byteBuffer.getShort() & 65535;
        byte[] v0_1 = new byte[4];
        byteBuffer.get(v0_1, 0, 4);
        this.sourceAddress = InetAddress.getByAddress(v0_1);
        byteBuffer.get(v0_1, 0, 4);
        this.destinationAddress = InetAddress.getByAddress(v0_1);
    }

    public final void fillHeader(ByteBuffer byteBuffer) {
    	byteBuffer.put(((byte)(this.version << 4 | this.ihl)));
        byteBuffer.put(((byte)this.serviceType));
        byteBuffer.putShort(((short)this.totalLength));
        byteBuffer.putInt(this.identificationAndFlagsAndFragmentOffset);
        byteBuffer.put(((byte)this.ttl));
        byteBuffer.put(((byte)this.headerType.getNumber()));
        byteBuffer.putShort(((short)this.headerChecksum));
        byteBuffer.put(this.sourceAddress.getAddress());
        byteBuffer.put(this.destinationAddress.getAddress());
    }

    public final String toString() {
    	StringBuilder sBuilder = new StringBuilder("IP4Header{");
        sBuilder.append("version=").append(this.version);
        sBuilder.append(", IHL=").append(this.ihl);
        sBuilder.append(", typeOfService=").append(this.serviceType);
        sBuilder.append(", totalLength=").append(this.totalLength);
        sBuilder.append(", identificationAndFlagsAndFragmentOffset=").append(this.identificationAndFlagsAndFragmentOffset);
        sBuilder.append(", TTL=").append(this.ttl);
        sBuilder.append(", protocol=").append(this.headerTypeInt).append(":").append(this.headerType);
        sBuilder.append(", headerChecksum=").append(this.headerChecksum);
        sBuilder.append(", sourceAddress=").append(this.sourceAddress.getHostAddress());
        sBuilder.append(", destinationAddress=").append(this.destinationAddress.getHostAddress());
        sBuilder.append('}');
        return sBuilder.toString();
    }
}