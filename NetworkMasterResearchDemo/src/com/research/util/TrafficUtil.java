package com.research.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Build.VERSION;
import android.os.Process;
import android.util.Log;

import com.research.netspeed.TotalTrafficBean;
import com.research.netspeed.TrafficStatsInfo;
import com.research.network.main.R;

/***
 * 流量统计工具类 当前项目未使用
 * 主要思路是通过UID获取应用收发包
 * 方式1:读取 /proc/uid_stat/uid/tcp_rcv || tcp_snd(某些设备上没有这个文件)
 * 方式2:读取/proc/net/xt_qtaguid/stats 文件(较方式比较全面、稳定)
 * @author Eric li
 *
 */
public class TrafficUtil {
	/**不包括移动流量接口名称*/
	public static final List<String> otherInterfaces = Arrays.asList(new String[]{"wlan", "lo", "p2p", "bluetooth", "tun", "eth0"});
	/**不包括非网络流量接口名称*/
    public static final List<String> noneFlowInterfaces = Arrays.asList(new String[]{"lo", "p2p", "bluetooth", "tun"});
    //
    private static Set<String> mobileInterfacesNoCache;
    //
    private static Boolean isSupportNoCache;
    //
    private List<TrafficStatsInfo> allTrafficStatsInfos;
    //
    private Map<Integer, List<TrafficStatsInfo>> trafficStatsInfos;
    //
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    //
    private Context context;

    public TrafficUtil(Context context) {
        this.context = context;
    }
    
    public static boolean isMobileInterface(Context context, String str) {
        Set mobileIfaces = getMobileIfaces(context);
        if (mobileIfaces != null && mobileIfaces.size() > 0) {
            return mobileIfaces.contains(str);
        }
        for (String contains : otherInterfaces) {
            if (str.contains(contains)) {
                return false;
            }
        }
        return true;
    }
    
    public static List<TrafficStatsInfo> readAllTrafficStatsInfo(String str) {
    	ArrayList<TrafficStatsInfo> arrayList = new ArrayList();
        Scanner scanner = new Scanner(new String(ProcNetUtil.readFile(str)));
        Pattern compile = Pattern.compile("([\\d]+) ([\\w]+) 0x[\\da-fA-F]+ ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+) ([\\d]+)");
        while (scanner.hasNextLine()) {
            Object nextLine = scanner.nextLine();
            Matcher matcher = compile.matcher((CharSequence) nextLine);
            if (matcher.find()) {
                Object obj;
                TrafficStatsInfo trafficStatsInfo = new TrafficStatsInfo();
                trafficStatsInfo.idx = Long.parseLong(matcher.group(1));
                trafficStatsInfo.iface = matcher.group(2);
                for (String contains : noneFlowInterfaces) {
                    if (trafficStatsInfo.iface.contains(contains)) {
                        obj = 1;
                        break;
                    }
                }
                obj = null;
                if (obj == null) {
                    try {
                        trafficStatsInfo.uid_tag_int = Integer.parseInt(matcher.group(3));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Throwable th) {
                        scanner.close();
                    }
                    trafficStatsInfo.cnt_set = Long.parseLong(matcher.group(4));
                    trafficStatsInfo.rx_bytes = Long.parseLong(matcher.group(5));
                    trafficStatsInfo.rx_packets = Long.parseLong(matcher.group(6));
                    trafficStatsInfo.tx_bytes = Long.parseLong(matcher.group(7));
                    trafficStatsInfo.tx_packets = Long.parseLong(matcher.group(8));
                    trafficStatsInfo.rx_tcp_bytes = Long.parseLong(matcher.group(9));
                    trafficStatsInfo.rx_tcp_packets = Long.parseLong(matcher.group(10));
                    trafficStatsInfo.rx_udp_bytes = Long.parseLong(matcher.group(11));
                    trafficStatsInfo.rx_udp_packets = Long.parseLong(matcher.group(12));
                    trafficStatsInfo.rx_other_bytes = Long.parseLong(matcher.group(13));
                    trafficStatsInfo.rx_other_packets = Long.parseLong(matcher.group(14));
                    trafficStatsInfo.tx_tcp_bytes = Long.parseLong(matcher.group(15));
                    trafficStatsInfo.tx_tcp_packets = Long.parseLong(matcher.group(16));
                    trafficStatsInfo.tx_udp_bytes = Long.parseLong(matcher.group(17));
                    trafficStatsInfo.tx_udp_packets = Long.parseLong(matcher.group(18));
                    trafficStatsInfo.tx_other_bytes = Long.parseLong(matcher.group(19));
                    trafficStatsInfo.tx_other_packets = Long.parseLong(matcher.group(20));
                    if (trafficStatsInfo.uid_tag_int == 10082) {
                    //    x.e("Traffic", nextLine);
                    //    x.e("Traffic", "iface = " + bdVar.b + " rx =" + bdVar.f + " tx = " + bdVar.h);
                    }
                    arrayList.add(trafficStatsInfo);
                } else {
                    continue;
                }
            }
        }
        scanner.close();
        return arrayList;
    }

    /***
     * 
     * @param context
     * @return
     */
    public static Set<String> getMobileIfaces(Context context) {
        Set<String> mobileIfacesNoCache = getMobileIfacesNoCache();
        if ((mobileIfacesNoCache == null || mobileIfacesNoCache.size() == 0) && mobileInterfacesNoCache != null) {
            mobileIfacesNoCache = mobileInterfacesNoCache;
        } else {
            mobileInterfacesNoCache = mobileIfacesNoCache;
        }
        if (mobileIfacesNoCache == null || (mobileIfacesNoCache.size() == 0 && context != null)) {
            return context.getSharedPreferences("remote_service_config", 0).getStringSet("traffic_utils_mobile_ifaces", mobileIfacesNoCache);
        }
        mobileInterfacesNoCache = mobileIfacesNoCache;
        return mobileIfacesNoCache;
    }

    /***
     * 获得所有mobile类型的interface
     * @return
     */
    public static Set<String> getMobileIfacesNoCache() {
    	HashSet<String> hashSet = new HashSet();
        try {
            Collections.addAll(hashSet, (String[])( ReflectUtil.callMethod(TrafficStats.class, "getMobileIfaces", null, new Class[0], new Object[0])));
        } catch (Throwable e) {
            Log.e("TrafficUtils", "getMobileIfaces Error", e);
        }
        return hashSet;
    }

    public void reload() {
        load("/proc/net/xt_qtaguid/stats");
    }

    
    public void load(String str) {
        Lock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();
        this.allTrafficStatsInfos = readAllTrafficStatsInfo(str);
        this.trafficStatsInfos = new HashMap();
        TrafficStatsInfo bdVar2 = null;
        for (TrafficStatsInfo bdVar : this.allTrafficStatsInfos) {
            List list = (List) this.trafficStatsInfos.get(Integer.valueOf(bdVar2.uid_tag_int));
            if (list == null) {
                list = new ArrayList();
                this.trafficStatsInfos.put(Integer.valueOf(bdVar2.uid_tag_int), list);
            }
            list.add(bdVar2);
        }
        if (this.trafficStatsInfos.size() < 2 && this.trafficStatsInfos.get(Integer.valueOf(android.os.Process.myUid())) != null) {
            String[] list2 = new File("/proc/uid_stat/").list();
            if (list2 != null) {
                for (String parseInt : list2) {
                    int parseInt2 = 0;
                    int i = -1;
                    try {
                        parseInt2 = Integer.parseInt(parseInt);
                    } catch (Exception e) {
                        e.printStackTrace();
                        parseInt2 = i;
                    } catch (Throwable th) {
                        writeLock.unlock();
                    }
                    List list3 = (List) this.trafficStatsInfos.get(Integer.valueOf(parseInt2));
                    if (list3 == null || list3.size() == 0) {
                        long a = a("/proc/uid_stat/" + parseInt2 + "/udp_snd");
                        long a2 = a("/proc/uid_stat/" + parseInt2 + "/udp_rcv");
                        long a3 = a("/proc/uid_stat/" + parseInt2 + "/tcp_snd");
                        long a4 = a("/proc/uid_stat/" + parseInt2 + "/tcp_rcv");
                        bdVar2 = new TrafficStatsInfo();
                        bdVar2.iface = "wlan0";
                        bdVar2.rx_bytes = a2 + a4;
                        bdVar2.tx_bytes = a + a3;
                        list3 = Arrays.asList(new TrafficStatsInfo[]{bdVar2});
                        if (list3 != null) {
                            this.trafficStatsInfos.put(Integer.valueOf(parseInt2), list3);
                        }
                    }
                }
            }
        }
        writeLock.unlock();
    }

    private static long a(String str) {
        return Long.valueOf(new String(ProcNetUtil.readFile(str)).trim()).longValue();
    }

    public final TotalTrafficBean getUidGroupedTrafficInfo(int i) {
        Lock readLock = this.readWriteLock.readLock();
        try {
            readLock.lock();
            TotalTrafficBean bcVar = new TotalTrafficBean();
            if (this.trafficStatsInfos == null) {
                return bcVar;
            }
            List<TrafficStatsInfo> list = (List) this.trafficStatsInfos.get(Integer.valueOf(i));
            if (list == null || list.size() == 0) {
                readLock.unlock();
                return bcVar;
            }
            for (TrafficStatsInfo bdVar : list) {
                if (NetSpeedUtil.isSimNormal(this.context) && isMobileInterface(this.context, bdVar.iface)) {
                    if (bdVar.cnt_set == 0) {
                        bcVar.g += bdVar.rx_bytes;
                        bcVar.h += bdVar.tx_bytes;
                    } else {
                        bcVar.e += bdVar.rx_bytes;
                        bcVar.f += bdVar.tx_bytes;
                    }
                } else if (bdVar.cnt_set == 0) {
                    bcVar.c += bdVar.rx_bytes;
                    bcVar.d += bdVar.tx_bytes;
                } else {
                    bcVar.a += bdVar.rx_bytes;
                    bcVar.b += bdVar.tx_bytes;
                }
            }
            readLock.unlock();
            return bcVar;
        } finally {
            readLock.unlock();
        }
    }

    public final List<TrafficStatsInfo> getTrafficStatsInfoByUid(int i) {
        if (this.trafficStatsInfos != null) {
            return (List) this.trafficStatsInfos.get(Integer.valueOf(i));
        }
        return null;
    }

    public static synchronized boolean isSupport() {
        boolean booleanValue;
        synchronized ("111") {
            if (isSupportNoCache == null) {
                isSupportNoCache = Boolean.valueOf(isSupportNoCache());
            }
            booleanValue = isSupportNoCache.booleanValue();
        }
        return booleanValue;
    }

    public static boolean isSupportNoCache() {
        if (VERSION.SDK_INT > 18) {
            return true;
        }
        int myUid = Process.myUid();
        for (TrafficStatsInfo bdVar : readAllTrafficStatsInfo("/proc/net/xt_qtaguid/stats")) {
		    if (bdVar.uid_tag_int != 0 && bdVar.uid_tag_int != myUid) {
		        return true;
		    }
		}
        return false;
    }

    public static String formatFileSize(Context context, long j) {
        return a(context, j, true, null);
    }

    /***
     * 加上单位
     * @param context
     * @param size
     * @param defaultStr
     * @return
     */
    public static String formatSpeedNum(Context context, long size, String defaultStr) {
        return a(context, size, true, defaultStr);
    }

    public static String formatFileSize(Context context, long j, boolean z) {
        return a(context, j, z, "0");
    }

    public static String getFileSizeUnit(Context context, long j) {
        String result = "B";
        if (j > 0) {
            float f = (float) j;
            if (f > 900.0f) {
                result = "KB";
                f /= 1024.0f;
            }
            if (f > 900.0f) {
                result = "MB";
                f /= 1024.0f;
            }
            if (f > 900.0f) {
            	result = "GB";
                f /= 1024.0f;
            }
            if (f > 900.0f) {
            	result = "TB";
                f /= 1024.0f;
            }
            if (f > 900.0f) {
            	result = "PB";
            }
        }
        return result;
    }

    private static String a(Context context, long flow, boolean z, String defStr) {
        if (context == null) {
            return "";
        }
        String[] formatFileSizeValueSuffix = formatFileSizeValueSuffix(context, flow, true);
        if (formatFileSizeValueSuffix == null) {
            return defStr == null ? "None" : defStr;
        } else {
            if (!z) {
                return formatFileSizeValueSuffix[0];
            }
            return context.getResources().getString(R.string.fileSizeSuffix, new Object[]{formatFileSizeValueSuffix[0], formatFileSizeValueSuffix[1]});
        }
    }

    public static String[] formatFileSizeValueSuffix(Context context, long flow, boolean z) {
        if (context == null || flow <= 0) {
            return null;
        }
        float resultFlow;
        String resultUnit;
        float tmpFlow = (float) flow;
        String result = "B";
        if (tmpFlow > 900.0f) {
            result = "KB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	result = "MB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	result = "GB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	result = "TB";
            tmpFlow /= 1024.0f;
        }
        if (tmpFlow > 900.0f) {
        	result = "PB";
            resultFlow = tmpFlow / 1024.0f;
        } else {
            resultFlow = tmpFlow;
        }
        if (resultFlow < 1.0f) {
            resultUnit = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
        } else if (resultFlow < 10.0f) {
            if (z) {
                resultUnit = String.format(null, "%.1f", new Object[]{Float.valueOf(resultFlow)});
            } else {
                resultUnit = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
            }
        } else if (resultFlow >= 100.0f || z) {
            resultUnit = String.format(null, "%.0f", new Object[]{Float.valueOf(resultFlow)});
        } else {
            resultUnit = String.format(null, "%.2f", new Object[]{Float.valueOf(resultFlow)});
        }
        return new String[]{resultUnit, result};
    }

    public static String formatFileSizeInteger(Context context, long j) {
        String result = "MB";
        if (context == null) {
            return "None";
        }
        long j2;
        if (j == -1) {
            j2 = 0;
        } else {
            j2 = j;
        }
        if (j2 != 0) {
            if (j2 >= 1073741824) {
                j2 /= 1073741824;
                result = "GB";
            } else {
                j2 /= 1048576;
            }
        }
        String format = String.format(null, "%.0f", new Object[]{Float.valueOf((float) j2)});
        return context.getResources().getString(R.string.fileSizeSuffix, new Object[]{format, result});
    }

    public static String formatFileSizeInteger(Context context, long j, boolean z) {
        String result = "MB";
        if (context == null) {
            return "None";
        }
        long j2;
        if (j == -1) {
            j2 = 0;
        } else {
            j2 = j;
        }
        if (j2 != 0) {
            if (j2 >= 1073741824) {
                j2 /= 1073741824;
                result = "GB";
            } else {
                j2 /= 1048576;
            }
        }
        if (!z) {
            return result;
        }
        return String.format(null, "%.0f", new Object[]{Float.valueOf((float) j2)});
    }

//    public static String updateResultTime(Context context, long j) {
//        String str = "";
//        if (j <= 0) {
//            return str;
//        }
//        long currentTimeMillis = System.currentTimeMillis() - j;
//        StringBuilder append = new StringBuilder().append("Last refresh:");
//        if (currentTimeMillis <= 60000) {
//            str = "Just now";
//        } else if (currentTimeMillis <= 3600000) {
//            str = context.getString(R.string.device_time_minutes, new Object[]{Long.valueOf(currentTimeMillis / 60000)});
//        } else if (currentTimeMillis <= 86400000) {
//            if (((int) (currentTimeMillis / 3600000)) > 1) {
//                str = context.getString(R.string.device_time_hour, new Object[]{Integer.valueOf((int) (currentTimeMillis / 3600000))});
//            } else {
//                str = "1 hour ago";
//            }
//        } else {
//            if (((int) (currentTimeMillis / 86400000)) > 1) {
//                str = context.getString(R.string.device_time_day, new Object[]{Integer.valueOf((int) (currentTimeMillis / 86400000))});
//            } else {
//                str = "a day ago";
//            }
//        }
//        return append.append(str).toString();
//    }

//    public static Long[] getDataPlanFlow(x xVar, long j, long j2) {
//        long j3;
//        long j4;
//        long j5 = -1;
//        if (xVar != null) {
//            j3 = xVar.getLong("total_data_flow", -1);
//            j4 = xVar.getLong("use_data_flow", -1);
//            j5 = j4;
//            j4 = xVar.getLong("last_total_data_flow", 0);
//        } else {
//            j4 = 0;
//            j3 = -1;
//        }
//        if (j3 > 0) {
//            if (j5 < 0) {
//                j5 = 0;
//            }
//            j5 += j2 - j4;
//            if (xVar != null) {
//                xVar.setLong("use_data_flow", j5);
//            }
//        }
//        if (xVar != null) {
//            xVar.setLong("last_total_data_flow", j2);
//            xVar.setLong("last_day_data_flow", j);
//        }
//        return new Long[]{Long.valueOf(j3), Long.valueOf(j5)};
//    }
}
