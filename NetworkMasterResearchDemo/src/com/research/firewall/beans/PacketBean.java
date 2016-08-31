package com.research.firewall.beans;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class PacketBean {
	/**a*/
	public IP4Header ip4Header;  // e a
	/**b*/
    public TCPHeader tcpHeader;  // g b
    /**c*/
    public UDPHeader udpHeader;  // h c
    /**d*/
    public ByteBuffer byteBuffer;
    private boolean isTCP;
    private boolean isUDP;

    public PacketBean(ByteBuffer byteBuffer) throws Exception {
        super();
        this.ip4Header = new IP4Header(byteBuffer, (byte) 0);
        if(this.ip4Header.headerType == HeaderType.TCP) {
            this.tcpHeader = new TCPHeader(byteBuffer, (byte)0);
            this.isTCP = true;
        }
        else if(this.ip4Header.headerType == HeaderType.UDP) {
            this.udpHeader = new UDPHeader(byteBuffer, (byte)0);
            this.isUDP = true;
        }

        this.byteBuffer = byteBuffer;
    }


    public final boolean isTCP() {
        return this.isTCP;
    }

    public final boolean isUDP() {
        return this.isUDP;
    }

    private void fillHeader(ByteBuffer byteBuffer) {
        this.ip4Header.fillHeader(byteBuffer);
        if(this.isUDP) {
            UDPHeader tmpUDPHeader = this.udpHeader;
            byteBuffer.putShort(((short)tmpUDPHeader.sourcePort));
            byteBuffer.putShort(((short)tmpUDPHeader.destinationPort));
            byteBuffer.putShort(((short)tmpUDPHeader.length));
            byteBuffer.putShort(((short)tmpUDPHeader.checksum));
        }
        else if(this.isTCP) {
            TCPHeader tmpTCPHeader = this.tcpHeader;
            byteBuffer.putShort(((short)tmpTCPHeader.sourcePort));
            byteBuffer.putShort(((short)tmpTCPHeader.destinationPort));
            byteBuffer.putInt(((int)tmpTCPHeader.sequenceNumber));
            byteBuffer.putInt(((int)tmpTCPHeader.acknowledgementNumber));
            byteBuffer.put(tmpTCPHeader.e);
            byteBuffer.put(tmpTCPHeader.flags);
            byteBuffer.putShort(((short)tmpTCPHeader.window));
            byteBuffer.putShort(((short)tmpTCPHeader.checksum));
            byteBuffer.putShort(((short)tmpTCPHeader.j));
        }
    }
    
    public final void swapSourceAndDestination() {
        int v0_1;
        InetAddress inetAddress = this.ip4Header.destinationAddress;
        this.ip4Header.destinationAddress = this.ip4Header.sourceAddress;
        this.ip4Header.sourceAddress = inetAddress;
        if(this.isUDP) {
            v0_1 = this.udpHeader.destinationPort;
            this.udpHeader.destinationPort = this.udpHeader.sourcePort;
            this.udpHeader.sourcePort = v0_1;
        }
        else if(this.isTCP) {
            v0_1 = this.tcpHeader.destinationPort;
            this.tcpHeader.destinationPort = this.tcpHeader.sourcePort;
            this.tcpHeader.sourcePort = v0_1;
        }
    }
    
    public final String toString() {
        StringBuilder sBuilder = new StringBuilder("Packet{");
        try {
        	sBuilder.append("ip4Header=").append(this.ip4Header);
            if(this.isTCP) {
            	sBuilder.append(", tcpHeader=").append(this.tcpHeader);
            }
            else if(this.isUDP) {
            	sBuilder.append(", udpHeader=").append(this.udpHeader);
            }

            if(this.byteBuffer != null) {
            	sBuilder.append(", payloadSize position=").append(this.byteBuffer.position());
            	sBuilder.append(", payloadSize=").append(this.byteBuffer.limit() - this.byteBuffer.position());
            	sBuilder.append('}');
            }
        }
        catch(Exception e) {
        	e.printStackTrace();
        }

        return sBuilder.toString();
    }
    
    public final void updateTCPBuffer(ByteBuffer pByteBuffer, byte flags, long sequenceNum, long acknowledgementNum, int arg16) {
        int v6 = 36;
        int v4 = 65535;
        pByteBuffer.position(0);
        this.fillHeader(pByteBuffer);
        this.byteBuffer = pByteBuffer;
        this.tcpHeader.flags = flags;
        this.byteBuffer.put(33, flags);
        this.tcpHeader.sequenceNumber = sequenceNum;
        this.byteBuffer.putInt(24, ((int)sequenceNum));
        this.tcpHeader.acknowledgementNumber = acknowledgementNum;
        this.byteBuffer.putInt(28, ((int)acknowledgementNum));
        this.tcpHeader.e = 80;
        this.byteBuffer.put(32, (byte) 80);
        int v0 = arg16 + 20;
        ByteBuffer v1 = ByteBuffer.wrap(this.ip4Header.sourceAddress.getAddress());
        int v1_1 = (v1.getShort() & v4) + (v1.getShort() & v4);
        ByteBuffer v2 = ByteBuffer.wrap(this.ip4Header.destinationAddress.getAddress());
        v1_1 = v1_1 + ((v2.getShort() & v4) + (v2.getShort() & v4)) + (HeaderType.TCP.getNumber() + v0);
        ByteBuffer v3 = this.byteBuffer.duplicate();
        v3.putShort(v6, (short) 0);
        v3.position(20);
        int v7 = v0;
        v0 = v1_1;
        v1_1 = v7;
        while(v1_1 > 1) {
            v1_1 += -2;
            v0 = (v3.getShort() & v4) + v0;
        }

        if(v1_1 > 0) {
            v0 += (((short)(v3.get() & 255))) << 8;
        }

        while(v0 >> 16 > 0) {
            v0 = (v0 >> 16) + (v0 & v4);
        }

        v0 ^= -1;
        this.tcpHeader.checksum = v0;
        this.byteBuffer.putShort(v6, ((short)v0));
        v0 = arg16 + 40;
        this.byteBuffer.putShort(2, ((short)v0));
        this.ip4Header.totalLength = v0;
        setIP4CheckSum();
    }
    
    private void setIP4CheckSum() {
        int v0 = 0;
        ByteBuffer tmpByteBuffer = this.byteBuffer.duplicate();
        tmpByteBuffer.position(0);
        tmpByteBuffer.putShort(10, (short) 0);
        int v1;
        for(v1 = this.ip4Header.c; v1 > 0; v1 += -2) {
            v0 += tmpByteBuffer.getShort() & 65535;
        }

        while(v0 >> 16 > 0) {
            v0 = (v0 >> 16) + (v0 & 65535);
        }

        v0 ^= -1;
        this.ip4Header.headerChecksum = v0;
        this.byteBuffer.putShort(10, ((short)v0));
    }
    
    public final void updateUDPBuffer(ByteBuffer arg6, int arg7) {
        arg6.position(0);
        this.fillHeader(arg6);
        this.byteBuffer = arg6;
        int v0 = arg7 + 8;
        this.byteBuffer.putShort(24, ((short)v0));
        this.udpHeader.length = v0;
        this.byteBuffer.putShort(26, (short) 0);
        this.udpHeader.checksum = 0;
        v0 += 20;
        this.byteBuffer.putShort(2, ((short)v0));
        this.ip4Header.totalLength = v0;
        this.setIP4CheckSum();
    }
}