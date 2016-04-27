package com.tokool.bra.activity;

import com.tokool.bra.R;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConnectionActivity extends Activity {

	private ImageView btnBack;
	private TextView tvConnectionLeft, tvConnectionRight;
	private RelativeLayout btnConnectionLeft, btnConnectionRight;
	private MyOnClickListener onClickListener;
	private MyOnLongClickListener onLongClickListener;
	private MyBroadcastReceiver receiver;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_connection);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("device_connected");
		filter.addAction("device_disconnected");
		registerReceiver(receiver, filter);
		
		onClickListener=new MyOnClickListener();
		
		onLongClickListener=new MyOnLongClickListener();
		
		handler=new Handler();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(onClickListener);
		
		tvConnectionLeft=(TextView)findViewById(R.id.tv_connection_left);
		if(BleService.isLeftConnected){
			tvConnectionLeft.setText(BleService.leftAddress);
		}
		
		tvConnectionRight=(TextView)findViewById(R.id.tv_connection_right);
		if(BleService.isRightConnected){
			tvConnectionRight.setText(BleService.rightAddress);
		}
		
		btnConnectionLeft=(RelativeLayout)findViewById(R.id.btn_connection_left);
		btnConnectionLeft.setOnClickListener(onClickListener);
		btnConnectionLeft.setOnLongClickListener(onLongClickListener);
		
		btnConnectionRight=(RelativeLayout)findViewById(R.id.btn_connection_right);
		btnConnectionRight.setOnClickListener(onClickListener);
		btnConnectionRight.setOnLongClickListener(onLongClickListener);
	}	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		handler.removeCallbacksAndMessages(null);
	}


	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
				
			case R.id.btn_connection_left:
				startActivity(new Intent(ConnectionActivity.this, ConnectionLeftActivity.class));
				break;

			case R.id.btn_connection_right:
				startActivity(new Intent(ConnectionActivity.this, ConnectionRightActivity.class));
				break;
				
			
			}
		}
		
	}
	
	private class MyOnLongClickListener implements OnLongClickListener{

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub			
			switch (v.getId()) {			
			case R.id.btn_connection_left:
				if(BleService.isLeftConnected){
					Vibrator vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(300);
					View dialog=getLayoutInflater().inflate(R.layout.dialog_message, null);
					((TextView)dialog.findViewById(R.id.tv_dialog_message)).setText(getString(R.string.unbind));
					new AlertDialog.Builder(ConnectionActivity.this)
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							sendBroadcast(new Intent("stop_left_massage"));
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Intent intent1=new Intent();
									intent1.setAction("request_disconnect");
									intent1.putExtra("which", "L");
									sendBroadcast(intent1);
								}
							}, 1000);							
							tvConnectionLeft.setText(getString(R.string.unbound));
						}
					})
					.setNegativeButton(getString(R.string.cancel), null)
					.create()
					.show();
				}				
				break;

			case R.id.btn_connection_right:
				if(BleService.isRightConnected){
					Vibrator vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(300);					
					View dialog=getLayoutInflater().inflate(R.layout.dialog_message, null);
					((TextView)dialog.findViewById(R.id.tv_dialog_message)).setText(getString(R.string.unbind));
					new AlertDialog.Builder(ConnectionActivity.this)
					.setView(dialog)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							sendBroadcast(new Intent("stop_right_massage"));
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Intent intent2=new Intent();
									intent2.setAction("request_disconnect");
									intent2.putExtra("which", "R");
									sendBroadcast(intent2);
								}
							}, 800);														
							tvConnectionRight.setText(getString(R.string.unbound));
						}
					})
					.setNegativeButton(getString(R.string.cancel), null)
					.create()
					.show();
				}
				
				break;
			}			
			return true;
		}
		
	}
		
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("device_connected")){
				String address=intent.getStringExtra("address");
				if(intent.getStringExtra("name").endsWith("L")){
					tvConnectionLeft.setText(address);
				}else{
					tvConnectionRight.setText(address);
				}
				
			}
			if(action.equals("device_disconnected")){
				if(!BleService.isLeftConnected){
					tvConnectionLeft.setText(getString(R.string.unbound));
				}
				if(!BleService.isRightConnected){
					tvConnectionRight.setText(getString(R.string.unbound));
				}				
			}
		}
		
	}
}
