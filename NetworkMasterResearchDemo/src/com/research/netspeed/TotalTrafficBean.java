package com.research.netspeed;

public class TotalTrafficBean {
	public long a;
    public long b;
    public long c;
    public long d;
    public long e;
    public long f;
    public long g;
    public long h;

    public final long getTotalWifi() {
        return ((this.a + this.b) + this.c) + this.d;
    }

    public final long getTotalMobile() {
        return ((this.e + this.f) + this.g) + this.h;
    }

    public final long getTotalBackground() {
        return ((this.c + this.d) + this.g) + this.h;
    }

}
