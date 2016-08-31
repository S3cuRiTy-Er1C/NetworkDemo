package com.research.util;

import com.research.network.main.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class NetSpeedUtil {

	/***
	 * 删除String前后的 ' " '
	 * 
	 * @param string
	 * @return
	 */
	public static String removeQuotedString(String string) {
		int tmpInt = 34;
		if (TextUtils.isEmpty(((CharSequence) string))) {
			string = "";
		} else if (string.charAt(0) == tmpInt
				&& string.charAt(string.length() - 1) == tmpInt) {
			string = string.substring(1, string.length() - 1);
		}

		return string;
	}

	/***
	 * get network info
	 * 
	 * @param networkInfo
	 * @return
	 */
	public static int getType(NetworkInfo networkInfo) {
		int result = -1;
		if (networkInfo == null || !networkInfo.isConnected()) {
			result = -1;
		}

		if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			// wifi
			result = 4096;
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			// mobile
			switch (networkInfo.getSubtype()) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:// api<8 : replace by 11
				// 2g
				result = 8193;
				break;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 : replace by 14
			case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 : replace by 12
			case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 : replace by 15
				//3g
				result = 8194;
				break;
			case TelephonyManager.NETWORK_TYPE_LTE:
				//4g
				result = 8196;
			}
		}
		return result;

	}
	
	/***
	 * 判断是否sim为就绪状态
	 * @param context
	 * @return
	 */
	public static boolean isSimNormal(Context context) {
        switch (((TelephonyManager) context.getSystemService("phone")).getSimState()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return false;
            case 5:
                return true;
            default:
                return false;
        }
    }

	/***
     * 加上单位
     * ex: size传入1024b 返回1kb
     * 注意这里format时未传入locale对象,传入的null,最好需要传入
     * @param context
     * @param size
     * @param defaultStr
     * @return
     */
    public static String formatSpeedNum(Context context, long size, String defaultStr) {
        return baseFormatSpeedNum(context, size, true, defaultStr);
    }

    /***
     * 
     * @param context context
     * @param flow    需要优化的速度
     * @param haveUnit 是否返回单位
     * @param defStr 默认值
     * @return
     */
    private static String baseFormatSpeedNum(Context context, long flow, boolean haveUnit, String defStr) {
        if (context == null) {
            return "";
        }
        String[] formatFileSizeValueSuffix = formatFileSizeValueSuffix(context, flow, true);
        if (formatFileSizeValueSuffix == null) {
            return defStr == null ? "None" : defStr;
        } else {
            if (!haveUnit) {
                return formatFileSizeValueSuffix[0];
            }
            return context.getResources().getString(R.string.fileSizeSuffix, new Object[]{formatFileSizeValueSuffix[0], formatFileSizeValueSuffix[1]});
        }
    }

    public static String[] formatFileSizeValueSuffix(Context context, long speed, boolean z) {
        if (context == null || speed <= 0) {
            return null;
        }
        float resultFlow;
        String speedNum;
        float tmpFlow = (float) speed;
        String speedUnit = "B";
        if (tmpFlow > 900.0f) {
            speedUnit = "KB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	speedUnit = "MB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	speedUnit = "GB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	speedUnit = "TB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	speedUnit = "PB";
            resultFlow = tmpFlow / 1024.0f;
        } else {
            resultFlow = tmpFlow;
        }
        //format
        if (resultFlow < 1.0f) {
            speedNum = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
        } else if (resultFlow < 10.0f) {
            if (z) {
                speedNum = String.format(null, "%.1f", new Object[]{Float.valueOf(resultFlow)});
            } else {
                speedNum = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
            }
        } else if (resultFlow >= 100.0f || z) {
            speedNum = String.format(null, "%.0f", new Object[]{Float.valueOf(resultFlow)});
        } else {
            speedNum = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
        }
        return new String[]{speedNum, speedUnit};
    }
}
