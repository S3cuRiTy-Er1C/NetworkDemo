package com.research.netspeed;

import java.util.ArrayList;
import java.util.List;

import com.research.network.main.MainActivity;
import com.research.network.main.R;
import com.research.util.ImageUtil;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NetworkSpeedAdapter extends BaseAdapter{
	//context
	private NetSpeedActivity main = null;
	//pkg manager
	private PackageManager pkgManager = null;
	//
	private LayoutInflater mInflater = null;
	public NetworkSpeedAdapter(NetSpeedActivity main) {
		this.main = main;
		//
		pkgManager = main.getPackageManager();
	}
	
	@Override
	public int getCount() {
		return main.listViewData.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(main).inflate(R.layout.network_speed_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);
			holder.name = (TextView) convertView.findViewById(R.id.item_app_name);
			holder.downloadSpeedNum = (TextView) convertView.findViewById(R.id.item_download_speed);
			holder.uploadSpeedNum = (TextView) convertView.findViewById(R.id.item_upload_speed);
			holder.connectCount = (TextView) convertView.findViewById(R.id.item_network_connect_count);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		NetworkApp app = main.listViewData.get(position);
		if(app != null){
			//set icon
			ImageUtil.setImage(app.getPkgName(), pkgManager, holder.icon);
			//set app name
			holder.name.setText(app.getName());
			//
			holder.downloadSpeedNum.setGravity(3);
			holder.uploadSpeedNum.setGravity(3);
			//set download speed num
			holder.downloadSpeedNum.setText(app.getDownSpeed());
			//set upload speed
			holder.uploadSpeedNum.setText(app.getUpspeed());
			//
			int connNum = app.getTcpListencount() + app.getTcpEstablishedCount();
			holder.connectCount.setText(connNum+"");
		}
		return convertView;
	}
	
	static class ViewHolder{
		ImageView icon;
		TextView name;
		TextView downloadSpeedNum;
		TextView uploadSpeedNum;
		TextView connectCount;
	}

}
