package com.tokool.bra.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import com.tokool.bra.R;
import com.tokool.bra.customview.LoadingDialog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionLeftActivity extends Activity {
	
	private ImageView btnBack, bluetoothIcon;
	private ListView lvDevices;
	private ArrayList<HashMap<String, Object>> devices;
	private LinearLayout lvLayout;
	private MyBaseAdapter adapter;
	private Button btnScan;
	private BluetoothAdapter btAdapter;
	private Handler handler;
	private MyBroadcastReceiver receiver;
	private LoadingDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection_left);
		
		handler=new Handler();
		
		dialog=new LoadingDialog(this, getString(R.string.connecting));
		dialog.setCanceledOnTouchOutside(false);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("device_connected");
		registerReceiver(receiver, filter);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		bluetoothIcon=(ImageView)findViewById(R.id.bluetooth_icon);
		
		btnScan=(Button)findViewById(R.id.start_scan);
		btnScan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(btAdapter.isDiscovering()){
					btAdapter.stopLeScan(mLeScanCallback);
				}
				btAdapter.startLeScan(mLeScanCallback);
				btnScan.setEnabled(false);
				btnScan.setText(getString(R.string.searching));
				btnScan.setBackgroundResource(R.drawable.shape_btn_scan_grey);
				bluetoothIcon.setVisibility(View.VISIBLE);
				Animation anim=AnimationUtils.loadAnimation(ConnectionLeftActivity.this, R.anim.anim_scan_rotate);
				bluetoothIcon.startAnimation(anim);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						btAdapter.stopLeScan(mLeScanCallback);
						btnScan.setEnabled(true);
						btnScan.setText(getString(R.string.search_ble));
						btnScan.setBackgroundResource(R.drawable.selector_btn_cyan_to_light);
						//按设备的rssi进行排序
						Collections.sort(devices, new Comparator<HashMap<String, Object>>() {

							@Override
							public int compare(HashMap<String, Object> device1, HashMap<String, Object> device2) {
								// TODO Auto-generated method stub
								int rssi1=(Integer)device1.get("rssi");
								int rssi2=(Integer)device2.get("rssi");
								return rssi2-rssi1;
							}
						});
						adapter.notifyDataSetChanged();
						if(devices.size()>0){
							lvLayout.setVisibility(View.VISIBLE);
						}
						bluetoothIcon.setVisibility(View.INVISIBLE);
					}
				}, 3000);
			}
		});
		
		devices=new ArrayList<HashMap<String,Object>>();
		
		lvDevices=(ListView)findViewById(R.id.device_list);		
		adapter=new MyBaseAdapter();
		lvDevices.setAdapter(adapter);
		lvDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, final int position, long arg3) {
				// TODO Auto-generated method stub
				if((Boolean)devices.get(position).get("status")){
					Toast.makeText(ConnectionLeftActivity.this, getString(R.string.bound), Toast.LENGTH_SHORT).show();
				}else{
					dialog.show();
					
					Intent intent=new Intent();
					intent.setAction("request_connect");
					intent.putExtra("address", (String)devices.get(position).get("address"));
					sendBroadcast(intent);
					
					//10秒还没连接上则提示失败
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(dialog.isShowing()){								
								dialog.dismiss();								
							}
						}
					}, 10000);
				}
			}
		});
		
		lvLayout=(LinearLayout)findViewById(R.id.listview_layout);
		if(devices.size()==0){
			lvLayout.setVisibility(View.INVISIBLE);
		}
		
		btAdapter=((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if(btAdapter!=null && !btAdapter.isEnabled()){
			btAdapter.enable();
		}
		
	}
	
	private LeScanCallback mLeScanCallback=new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice btDevice, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			String name=btDevice.getName();
			if(name!=null && name.equals("miss_bra_L")){
				for(HashMap<String, Object> device: devices){
					if(device.get("address").equals(btDevice.getAddress())){
						return;
					}
				}
				HashMap<String, Object> device=new HashMap<String, Object>();
				device.put("name", name);
				device.put("address", btDevice.getAddress());
				device.put("rssi", rssi);
				device.put("status", false);
				devices.add(device);
			}

		}
	};
		
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		unregisterReceiver(receiver);	
		btAdapter.stopLeScan(mLeScanCallback);
		
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("device_connected")){
				String address=intent.getStringExtra("address");
				for(HashMap<String, Object> device:devices){
					if(device.get("address").equals(address)){
						device.put("status", true);
						continue;
					}
					device.put("status", false);
				}				
				Toast.makeText(ConnectionLeftActivity.this, getString(R.string.binding_finished), Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		}
		
	}
	
	private class MyBaseAdapter extends BaseAdapter{
		
		private class ViewHolder{
			private TextView tvAddress;
			private TextView tvRssi;
			private ImageView ivStatus;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return devices.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return devices.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){
				convertView=getLayoutInflater().inflate(R.layout.list_item_device, null);
				holder=new ViewHolder();
				holder.tvAddress=(TextView)convertView.findViewById(R.id.tv_address);
				holder.tvRssi=(TextView)convertView.findViewById(R.id.tv_rssi);
				holder.ivStatus=(ImageView)convertView.findViewById(R.id.iv_status);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			HashMap<String, Object> device=devices.get(position);
			holder.tvAddress.setText((String)device.get("address"));
			holder.tvRssi.setText("rssi:"+((Integer)device.get("rssi")).toString());
			if((Boolean)device.get("status")){
				holder.ivStatus.setVisibility(View.VISIBLE);
			}else{
				holder.ivStatus.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
		
	}
	
}
