package com.research.util;

import java.util.Locale;

public class IPUtil {
	/**
	 * ip to int
	 * @param i
	 * @return
	 */
	public static int ipToInt(int ip) {
		return ((((ip & 255) << 24) + (((ip >> 8) & 255) << 16)) + (((ip >> 16) & 255) << 8))
				+ ((ip >> 24) & 255);
	}

	/***
	 * format ip to string
	 * @param ip
	 * @return
	 */
	public static String ipToString(int i) {
		return String.format(
				Locale.US,
				"%d.%d.%d.%d",
				new Object[] { Integer.valueOf((i >> 0) & 255),
						Integer.valueOf((i >> 8) & 255),
						Integer.valueOf((i >> 16) & 255),
						Integer.valueOf((i >> 24) & 255) });
	}

	/**
	 * 
	 * @param ip
	 * @return
	 */
	public static int getNetPrefix(int ip) {
		int ipInteger = ipToInt(ip);
		int i2 = 1;
		for (int i = 0; i < 32; i++) {
			ip >>= 1;
			if ((ip & 1) != 0) {
				return ((1 << i2) - 1) ^ -1;
			}
			i2++;
		}
		return ipInteger;
	}
}
