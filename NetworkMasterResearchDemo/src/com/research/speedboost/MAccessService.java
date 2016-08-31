package com.research.speedboost;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;

public class MAccessService extends AccessibilityService {

	private static final int sdkInt = Build.VERSION.SDK_INT;

	private String[] forceStopStrings = new String[] { "是", "ok", "确定",
			"موافق", "Aceptar", "Oke", "Tamam", "yes", "ОК", "Да", "Во ред",
			"ΘĶ", "‏نعم", "Ja", "Sí", "Oui", "Ya", "Si", "はい", "Sim", "Evet",
			"예", "យល់ព្រម", "လုပ်မည်", "ใช่", "ඔව්", "ಹೌದು", "Bəli", "Da",
			"Ano", "Bai", "Já", "Ndiyo", "‏بەڵێ", "Jā", "Taip", "Igen", "Ha",
			"Tak", "Áno", "Kyllä", "Có", "Ναι", "Иә", "Так", "დიახ", "Այո",
			"‏بله", "አዎ", "ठीक छ", "होय", "हो", "हाँ", "হ্যাঁ", "ਹਾਂ", "હા",
			"ஆம்", "Oldu", "Onartu", "კარგი", "ALLOW", "结束运行", "强行停止", "强制停止",
			"فرض الإيقاف", "Beenden erzwingen", "Forzar detención",
			"Forcer l\'arrêt", "Termina", "強制停止", "Forçar parada",
			"Durmaya zorla", "FORCE STOP", "强制停止" };
	private String[] stopStrings = new String[] { "FORCE STOP", "结束运行", "强行停止",
			"強行停止", "强制停止", "結束運行", "結束操作", "فرض الإيقاف", "Beenden erzwingen",
			"Forzar detención", "Forcer l\'arrêt", "Termina", "強制停止",
			"Forçar parada", "Durmaya zorla" };
	/** monitor package name of apps */
	private String[] pkgs = { "com.android.browser", "com.android.chrome" };

	private boolean isForceButtonExist = false;
	private String curSysForceStopStr = "";
	String[] confirmBtn = new String[3];

	/**
	 * event.toString can log all about thing with current event
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// netDemo(event);
		speedBoostDemo(event);
	}

	/**
	 * interrupt method
	 */
	@Override
	public void onInterrupt() {
		Log.w("TCLSecurity", "onInterrupt");
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.accessibilityservice.AccessibilityService#onServiceConnected()
	 *      do something with config
	 */
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		Log.w("TCLSecurity", "onServiceConnected");
		curSysForceStopStr = getSysFroceStopButtonStr();
		isForceButtonExist = curSysForceStopStr == null ? false : true;
		//
		confirmBtn[0] = "OK";
		confirmBtn[1] = getString(0x104000A);
		confirmBtn[2] = getString(0x1040013);

		AccessibilityServiceInfo aInfo = new AccessibilityServiceInfo();
		aInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
				| AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		aInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
				| AccessibilityServiceInfo.FEEDBACK_GENERIC;
		aInfo.notificationTimeout = 100;
		// set monitor packages
		// aInfo.packageNames = pkgs;
		setServiceInfo(aInfo);
	}

	/***
	 * 超级加速 demo method
	 * 
	 * @param event
	 */
	public void speedBoostDemo(AccessibilityEvent event) {
		AccessibilityNodeInfo nodeInfo = event.getSource();
		if (nodeInfo != null
				&& event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			// get class name
			String clazzName = event.getClassName().toString();
			if (!clazzName.endsWith("InstalledAppDetailsTop")
					&& !clazzName
							.equals("com.android.settings.applications.InstalledAppDetailsActivity")) {
				if (clazzName.endsWith("AlertDialog")) {
					int tmpResult = -1;
					// find nodeinfo and perform click action
					tmpResult = findNodeInfoByID(nodeInfo, new String[0]);
					Log.w("BOOST", "step1");
					// find confirm string
					if (tmpResult != 1) {
						tmpResult = findNodeInfoByText(nodeInfo, confirmBtn);
						Log.w("BOOST", "step6");
					}
					// find force stop string
					if (tmpResult != 1) {
						tmpResult = findNodeInfoByText(nodeInfo,
								forceStopStrings);
						Log.w("BOOST", "step7");
					}
					// find force stop string
				} else {
					Log.w("BOOST", "step2");
				}
				return;
			}
			//
			int tmpResult = -1;
			// click force stop button
			tmpResult = findNodeInfoByID(
					nodeInfo,
					new String[] { "com.android.settings:id/force_stop_button" });
			// find confirm string
			if (tmpResult != 1) {
				tmpResult = findNodeInfoByText(nodeInfo, confirmBtn);
			}
			// find force stop string
			if (tmpResult != 1) {
				tmpResult = findNodeInfoByText(nodeInfo, forceStopStrings);
				Log.w("BOOST", "step8");
			}
			// find force stop string
			if (tmpResult != 1 && isForceButtonExist) {
				tmpResult = findNodeInfoByText(nodeInfo,
						new String[] { curSysForceStopStr });
				Log.w("BOOST", "step3");
			}
			//
			if (tmpResult != 1) {
				tmpResult = findNodeInfoByText(nodeInfo, new String[0]);
				Log.w("BOOST", "step4");
			}
			//
			if (tmpResult != 1) {
				tmpResult = findNodeInfoByText(nodeInfo, stopStrings);
				Log.w("BOOST", "step5");
			}
			return;
		}
	}

	@SuppressLint("NewApi")
	private int findNodeInfoByID(AccessibilityNodeInfo nodeInfo, String[] strs) {
		// android 18 findAccessibilityNodeInfosByViewId api在18以后才出现.
		if (sdkInt >= 18) {
			// click button
			for (int i = 0; i < strs.length; i++) {
				List nodeInfos = nodeInfo
						.findAccessibilityNodeInfosByViewId(strs[i]);
				// 获取屏幕中所有控件
				if (nodeInfos != null && !nodeInfos.isEmpty()) {
					for (int j = 0; j < nodeInfos.size(); j++) {
						Object accessibilityNodeInfo = nodeInfos.get(j);
						// serach the button widgets
						if (accessibilityNodeInfo != null
								&& ((AccessibilityNodeInfo) accessibilityNodeInfo)
										.getClassName().equals(
												Button.class.getName())) {
							// is button
							// check if is enabled
							// perform click action
							if (((AccessibilityNodeInfo) accessibilityNodeInfo)
									.isEnabled()
									&& ((AccessibilityNodeInfo) accessibilityNodeInfo)
											.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
								// 执行完点击回收.
								((AccessibilityNodeInfo) accessibilityNodeInfo)
										.recycle();
								return 1;
							}
						}
					}
				}
			}
		}
		return -1;
	}

	private int findNodeInfoByText(AccessibilityNodeInfo nodeInfo, String[] strs) {
		for (int i = 0; i < strs.length; i++) {
			List nodeInfos = nodeInfo.findAccessibilityNodeInfosByText(strs[i]);
			// 获取屏幕中所有控件
			if (nodeInfos != null && !nodeInfos.isEmpty()) {
				for (int j = 0; j < nodeInfos.size(); j++) {
					Object accessibilityNodeInfo = nodeInfos.get(j);
					// serach the button widgets
					if (accessibilityNodeInfo != null
							&& ((AccessibilityNodeInfo) accessibilityNodeInfo)
									.getClassName().equals(
											Button.class.getName())) {
						// is button
						// check if is enabled
						// perform click action
						if (((AccessibilityNodeInfo) accessibilityNodeInfo)
								.isEnabled()
								&& ((AccessibilityNodeInfo) accessibilityNodeInfo)
										.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
							// 执行完点击回收.
							((AccessibilityNodeInfo) accessibilityNodeInfo)
									.recycle();
							return 1;
						}
					}
				}
			}
		}
		return -1;
	}

	/***
	 * 获取当前系统中的 ForceStop 资源
	 * 
	 * @return
	 */
	private String getSysFroceStopButtonStr() {
		String result = "";
		try {
			Context context = createPackageContext("com.android.settings", 3);
			result = context.getString(context.getResources().getIdentifier(
					"force_stop", "string", "com.android.settings"));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

}
