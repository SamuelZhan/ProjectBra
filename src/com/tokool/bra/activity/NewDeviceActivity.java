package com.tokool.bra.activity;

import com.tokool.bra.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NewDeviceActivity extends Activity {
	
	private ImageView btnBack;
	private RelativeLayout btnLeftDevice, btnRightDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_new_device);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnLeftDevice=(RelativeLayout)findViewById(R.id.btn_left_device);
		btnLeftDevice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				View dialog=getLayoutInflater().inflate(R.layout.dialog_message, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_message)).setText(getString(R.string.device_version_is_the_latest));
				new AlertDialog.Builder(NewDeviceActivity.this)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
			}
		});
		
		btnRightDevice=(RelativeLayout)findViewById(R.id.btn_right_device);
		btnRightDevice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				View dialog=getLayoutInflater().inflate(R.layout.dialog_message, null);
				((TextView)dialog.findViewById(R.id.tv_dialog_message)).setText(getString(R.string.device_version_is_the_latest));
				new AlertDialog.Builder(NewDeviceActivity.this)
				.setView(dialog)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
			}
		});
	}

}
