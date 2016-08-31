package com.research.network.main;

import java.io.IOException;
import java.util.ArrayList;

import com.research.firewall.MVpnService;
import com.research.netspeed.NetSpeedActivity;
import com.research.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class MainActivity extends Activity {
	public final static String TAG = "NETDEMO";
	//
	private Button send = null;
	//
	private Button btnAppSetting = null;
	//
	private Button btnKillProcess = null;
	//
	private EditText pkgInput = null;
	//
	private Button btnRequest = null;
	//
	private TextView logView = null;
	//
	private Button btnFireWall = null;
	//
	private Button btnNetSpeed = null;
	//
	private ArrayList<String> ipPool = null;
	//
	private WindowManager windowManager = null;
	private LayoutParams layoutParams = null;
	private ImageView floatView = null;
	// handler
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// log to view
			if (msg.what == 1) {
				String logStr = (String) msg.obj;
				logView.append('\n' + logStr);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//
		setContentView(R.layout.activity_main);
		// init view
		initView(this);
		// calculate
		ipPool = Util.calculateIpPool(this);
		// window manager
		initWindowManager();
	}
	
	/***
	 * 初始化window manager 及强力加速相关的资源
	 */
	private void initWindowManager(){
		windowManager = (WindowManager) this.getApplicationContext()
				.getSystemService(WINDOW_SERVICE);
		// layout params
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_PHONE,
				LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_LAYOUT_NO_LIMITS
						| LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.RGBA_8888);
		layoutParams.gravity = Gravity.CENTER;
		// float view
		floatView = (ImageView) getLayoutInflater().inflate(
				R.layout.floatwindow, null, true);
		floatView.setScaleType(ScaleType.CENTER_CROP);
		// scale bitmap to full screen
		float scalX;
		float scalY;
		int width = windowManager.getDefaultDisplay().getWidth();
		int height = windowManager.getDefaultDisplay().getHeight();
		scalX = width / 480.0f;
		scalY = height / 800.0f;
		width = (int) (480.0f * scalX);
		height = (int) (800.0f * scalY);
		// get bitmap
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(getAssets().open(
					"secwindow2.png"));
			if (bitmap != null) {
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
				floatView.setImageBitmap(bitmap);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化view 等控件
	 * @param main
	 */
	private void initView(final Activity main) {
		// cache arp
		send = (Button) findViewById(R.id.btnCacheArp);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.w(TAG, "click!");
				Util.cacheArp((MainActivity) main, ipPool);
			}
		});
		// app setting
		btnAppSetting = (Button) findViewById(R.id.btnSetting);
		btnAppSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				windowManager.addView(floatView, layoutParams);
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, MainActivity.class);
						startActivity(intent);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						windowManager.removeView(floatView);
					}
				}, 5000);
				String pkgStr = pkgInput.getText().toString();
				if (pkgStr.equals("")) {
					pkgStr = "com.lionmobi.netmaster";
				}
				Util.gotoSettings(main, pkgStr);
			}
		});
		// kill process
		btnKillProcess = (Button) findViewById(R.id.btnKillProcess);
		btnKillProcess.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String pkgStr = pkgInput.getText().toString();
				if (pkgStr.equals("")) {
					pkgStr = "com.lionmobi.netmaster";
				}
				Util.killProcess(main, pkgStr);
			}
		});
		// log view
		logView = (TextView) findViewById(R.id.logview);
		logView.setVerticalScrollBarEnabled(true);
		logView.setMovementMethod(ScrollingMovementMethod.getInstance());
		// pkg input
		pkgInput = (EditText) findViewById(R.id.pkgStrInput);
		// request accessibility
		btnRequest = (Button) findViewById(R.id.btnReqAccess);
		btnRequest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.showAccessibilitySettings(main);
			}
		});
		// btn firewall
		btnFireWall = (Button) findViewById(R.id.btnOpenFireWall);
		btnFireWall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Util.openFirewall(main);
				if (Build.VERSION.SDK_INT >= 15) {
					Intent intent = MVpnService.prepare(main);
					if (intent != null) {
						main.startActivityForResult(intent, 0);
					} else {
						onActivityResult(0, Activity.RESULT_OK, null);
					}
					Toast.makeText(main, "开启成功!所有APP将不能访问网络",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(main, "这个功能仅在Android4.0及以上才能使用",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		//去网速界面
		btnNetSpeed = (Button) findViewById(R.id.net_speed_btn);
		btnNetSpeed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						NetSpeedActivity.class);
				startActivity(intent);
			}
		});

		// set ip to title
		String ip = Util.getIpAddress();
		TextView tView = (TextView) findViewById(R.id.carrieroperator);
		if (ip != null && !ip.equals(""))
			tView.append("ip:" + ip);
		else
			tView.append("get ip error");
	}

	/**
	 * Log
	 * 
	 * @param logStr
	 */
	public void Log(String logStr) {
		Message msg = new Message();
		msg.what = 1;
		msg.obj = logStr;
		// send msg
		handler.sendMessage(msg);
		// log
		Log.w(TAG, logStr);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "onDestroy");
		Util.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(this, MVpnService.class);
			startService(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
