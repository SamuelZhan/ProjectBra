package com.tokool.bra.activity;

import com.tokool.bra.R;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AlarmActivity extends Activity {
		
	private Button btnEnsure;
	private Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		
		btnEnsure=(Button)findViewById(R.id.btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				vibrator.cancel();
				BleService.isMassaging=false;
				sendBroadcast(new Intent("stop_massage"));
				finish();
			}
		});
		
		vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(new long[]{200, 200}, 0);
		
		//若Bleservice不运行，则启动CallingRemindService连接设备并发送命令给硬件
		boolean isRunning=false;
		ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);		
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	         if ("com.tokool.bra.service.BleService".equals(service.service.getClassName())) {
	             isRunning=true;
	         }
	    }
		
		if(isRunning){
			BleService.isMassaging=true;
			sendBroadcast(new Intent("alarm_time_up"));
		}else{
			//若程序没启动的话，暂时不管
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

}
