package com.tokool.bra.activity;

import com.tokool.bra.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingActivity extends Activity {

	private ImageView btnBack;
	private RelativeLayout btnConnection, btnNewDevice, btnBroadcast, btnFeedback, btnVersion, btnShop, btnLanguage;
	private MyOnClickListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		
		listener=new MyOnClickListener();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnConnection=(RelativeLayout)findViewById(R.id.btn_connection);
		btnConnection.setOnClickListener(listener);
		
		btnNewDevice=(RelativeLayout)findViewById(R.id.btn_new_device);
		btnNewDevice.setOnClickListener(listener);
		
		btnBroadcast=(RelativeLayout)findViewById(R.id.btn_broadcast);
		btnBroadcast.setOnClickListener(listener);
		
		btnFeedback=(RelativeLayout)findViewById(R.id.btn_feedback);
		btnFeedback.setOnClickListener(listener);
		
		btnVersion=(RelativeLayout)findViewById(R.id.btn_version);
		btnVersion.setOnClickListener(listener);
		
		btnShop=(RelativeLayout)findViewById(R.id.btn_shop);
		btnShop.setOnClickListener(listener);
		
		btnLanguage=(RelativeLayout)findViewById(R.id.btn_language);
		btnLanguage.setOnClickListener(listener);
	}
	
	
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}



	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
				
			case R.id.btn_connection:
				startActivity(new Intent(SettingActivity.this, ConnectionActivity.class));
				break;

			case R.id.btn_new_device:
				startActivity(new Intent(SettingActivity.this, NewDeviceActivity.class));
				break;
				
			case R.id.btn_broadcast:
				startActivity(new Intent(SettingActivity.this, BroadcastActivity.class));
				break;
				
			case R.id.btn_feedback:
				startActivity(new Intent(SettingActivity.this, FeedbackActivity.class));
				break;
				
			case R.id.btn_version:
				View dialog=getLayoutInflater().inflate(R.layout.dialog_message, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_message)).setText(getString(R.string.current_version_is_the_latest));
				new AlertDialog.Builder(SettingActivity.this)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
				break;
				
			case R.id.btn_shop: 
				startActivity(new Intent(SettingActivity.this, ShopActivity.class));				
				break;
				
			case R.id.btn_language:
				startActivity(new Intent(SettingActivity.this, ChangeLanguageActivity.class));
				break;
				
			}
		}
		
	}
}
