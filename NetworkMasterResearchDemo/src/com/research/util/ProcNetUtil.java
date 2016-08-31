package com.research.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.research.firewall.beans.TCPBean;

/***
 * @author lenvo
 * proc/net/tcp util
 *
 */
public class ProcNetUtil {
	/**TCP STATUS 为连接状态*/
	public static final int TCP_ESTABLISHED = 1;
	/**TCp STATUS 为监听状态*/
	public static final int TCP_LISTEN = 10;
	/**读取 /proc/net/tcp6 中的每条数据保存到这个list中*/
	private static Map<String, List<TCPBean>> tcp6List = new HashMap();
	
	
	/***
	 * 返回包名对应uid在tcp6文件中对应 ST为status的所有条数的总和
	 * @param pkgName
	 * @param tcp status
	 * @return
	 */
	public static int getTcpConnections(String pkgName, int status) {
        int result = 0;
        List<TCPBean> list = tcp6List.get(pkgName);
        if(list != null) {
            Iterator<TCPBean> iterator = list.iterator();
            while(iterator.hasNext()) {
                int tmpInt = status == iterator.next().status ? result + 1 : result;
                result = tmpInt;
            }
        }

        return result;
    }
	
	/***
	 * 重新加载tcp6表到list
	 * @param context
	 * @return
	 */
	public static Map reloadActiveTcpConnections(Context context) {
		tcp6List.clear();
		
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            Scanner scanner = new Scanner(new String(readFile("/proc/net/tcp6")));
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String[] split;
                CharSequence pkgStr = null;
                List list;
                String[] split2 = scanner.nextLine().trim().split("\\s+");
                int length = split2.length;
                TCPBean tcpBean = new TCPBean();
                CharSequence charSequence = "";
                if (length > 1) {
                    split = split2[1].split(":");
                    if (split.length > 0) {
                        tcpBean.localAddress = getAddress(split[0].substring(24, split[0].length()));
                    }
                    if (split.length > 1) {
                        tcpBean.localPort = getPort(split[1]);
                    }
                }
                if (length > 2) {
                    split = split2[2].split(":");
                    if (split.length > 0) {
                        tcpBean.remoteAddress = getAddress(split[0].substring(24, split[0].length()));
                    }
                    if (split.length > 1) {
                        tcpBean.remotePort = getPort(split[1]);
                    }
                }
                if (length > 3) {
                    try {
                        tcpBean.status = Integer.parseInt(split2[3], 16); 
                    } catch (Exception e) {
                    	e.printStackTrace();
                    	tcpBean.status = 0;
                    }
                }
                if (length > 7) {
                    try {
                        int tmpUid = Integer.parseInt(split2[7]);
                        tcpBean.uid = tmpUid;
                        String nameForUid = packageManager.getNameForUid(tmpUid);
                        if(nameForUid.indexOf(58) > 0){
                        	pkgStr = nameForUid.substring(1, nameForUid.indexOf(':'));
                        }else{
                        	pkgStr = nameForUid;
                        }
                    } catch (Exception e3) {
                    	e3.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(pkgStr)) {
                        list = tcp6List.get(pkgStr);
                        if (list == null) {
                            list = new ArrayList();
                            tcp6List.put((String) pkgStr, list);
                        }
                        list.add(tcpBean);
                    }
                }
                pkgStr = charSequence;
                if (!TextUtils.isEmpty(pkgStr)) {
                    list = tcp6List.get(pkgStr);
                    if (list == null) {
                        list = new ArrayList();
                        tcp6List.put((String) pkgStr, list);
                    }
                    list.add(tcpBean);
                }
            }
        }
        return tcp6List;
    }
	
	/***
	 * 在tcp6表中索引,通过传入目标地址IP返回对应的包名
	 * @param context
	 * @param desIP ip6地址
	 * @return
	 */
	public static String getPkgByDestIp(Context context ,String desIP) {
		//从新加载tcp6表
		reloadActiveTcpConnections(context);
		//
        String result = "";
        if (TextUtils.isEmpty(desIP) || TextUtils.isEmpty(desIP.trim()) || desIP.trim().equals("0.0.0.0")) {
            return result;
        }
        String trim = desIP.trim();
        if (tcp6List != null) {
            for (String packageName : tcp6List.keySet()) {
                List<TCPBean> list = tcp6List.get(packageName);
                if (list != null) {
                    for (TCPBean tcpBean : list) {
                        if (tcpBean != null && !TextUtils.isEmpty(tcpBean.remoteAddress) && tcpBean.remoteAddress.equals(trim) && tcpBean.uid != 0) {
                            return packageName;
                        }
                    }
                    continue;
                }
            }
        }
        return result;
    }


	/***
	 * 
	 * @param context
	 * @param srcIP
	 * @return
	 */
	public static final String getPkgBySrcIp(Context context ,String srcIP) {
		//重新加载 tcp6到内存.
		reloadActiveTcpConnections(context);
		//
        String result = "";
        if (TextUtils.isEmpty(srcIP) || TextUtils.isEmpty(srcIP.trim()) || srcIP.trim().equals("0.0.0.0")) {
            return result;
        }
        String trim = srcIP.trim();
        if (tcp6List != null) {
            for (String packageName : tcp6List.keySet()) {
                List<TCPBean> list = tcp6List.get(packageName);
                if (list != null) {
                    for (TCPBean tcpBean : list) {
                        if (tcpBean != null) {
                            Object localAddress = tcpBean.localAddress;
                            Object remoteAddress = tcpBean.remoteAddress;
                            if (!TextUtils.isEmpty((CharSequence) localAddress) && localAddress.equals(trim) && tcpBean.uid != 0) {
                                return packageName;
                            }
                            if (!(TextUtils.isEmpty((CharSequence) remoteAddress) || !remoteAddress.equals(trim) || tcpBean.uid == 0)) {
                                return packageName;
                            }
                        }
                    }
                    continue;
                }
            }
        }
        return result;
    }

	
	/***
	 * 例如: 传入 "10" 返回  "A"
	 * @param str
	 * @return
	 */
	public static String getPort(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Integer.parseInt(str, 16));
        return stringBuilder.toString();
    }
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String a(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int length = str.length() - 1; length >= 0; length -= 2) {
            stringBuilder.append(Integer.parseInt(str.substring(length - 1, length + 1), 16));
            stringBuilder.append(".");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1).toString();
    }

	
	/***
	 * 
	 * @param str
	 * @return
	 */
	public static String getAddress(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        if (str.length() == 32) {
            for (int i = 0; i < str.length(); i += 8) {
                stringBuilder.append(a(str.substring(i, i + 8)));
                stringBuilder.append(".");
            }
            return stringBuilder.substring(0, stringBuilder.length() - 1).toString();
        } else if (str.length() != 8) {
            return "0.0.0.0";
        } else {
            return a(str);
        }
    }

	
	/***
	 * read file
	 * @param str
	 * @return
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static byte[] readFile(String arg5) {
		
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);
        FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(arg5);
			byte[] bytes = new byte[4096];
			while(true) {
				int count = fileInputStream.read(bytes);
				if(count == -1) {
					break;
				}
				
				byteArrayOutputStream.write(bytes, 0, count);
			}
			
			fileInputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			byte[] result = byteArrayOutputStream.toByteArray();
			try {
				byteArrayOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
    }


}
