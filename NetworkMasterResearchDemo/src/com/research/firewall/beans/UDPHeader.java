package com.research.firewall.beans;

import java.nio.ByteBuffer;

public class UDPHeader {
	public int sourcePort;
    public int destinationPort;
    public int length;
    public int checksum;

    public UDPHeader(ByteBuffer arg1, byte arg2) {
        this(arg1);
    }

    private UDPHeader(ByteBuffer arg3) {
        super();
        this.sourcePort = arg3.getShort() & 65535;
        this.destinationPort = arg3.getShort() & 65535;
        this.length = arg3.getShort() & 65535;
        this.checksum = arg3.getShort() & 65535;
    }

    public final String toString() {
        StringBuilder v0 = new StringBuilder("UDPHeader{");
        v0.append("sourcePort=").append(this.sourcePort);
        v0.append(", destinationPort=").append(this.destinationPort);
        v0.append(", length=").append(this.length);
        v0.append(", checksum=").append(this.checksum);
        v0.append('}');
        return v0.toString();
    }
}
