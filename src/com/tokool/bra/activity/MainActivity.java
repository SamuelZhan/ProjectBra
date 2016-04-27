package com.tokool.bra.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.tokool.bra.R;
import com.tokool.bra.customview.LoadingDialog;
import com.tokool.bra.service.BleService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;

public class MainActivity extends Activity {

	private RelativeLayout btnMassage, btnSteps, btnClock, btnMusic, btnMine, btnSetting;
	private ImageView ivConnectionLeft, ivConnectionRight;
	private TextView tvElectricityLeft, tvElectricityRight;
	private Button btnRecognize;
	private RelativeLayout brasConnectionLayout;
	private MyOnClickListener listener;
	private MyBroadcastReceiver receiver;
	private long backFirstTime;
	private SpeechRecognizer recognizer;
	private LoadingDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		String language=getSharedPreferences("language", Context.MODE_PRIVATE).getString("language", "zh");
		changeLanguage(language);
		
		setContentView(R.layout.activity_main);
		
		dialog=new LoadingDialog(this, getString(R.string.recognizing));
		
		//初始化语音识别模块
		initRecognizer();

		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("device_connected");
		filter.addAction("device_disconnected");
		filter.addAction("request_disconnect");
		filter.addAction("update_electricity");
		registerReceiver(receiver, filter);
		
		listener=new MyOnClickListener();
		
		btnMassage=(RelativeLayout)findViewById(R.id.btn_massage);
		btnMassage.setOnClickListener(listener);
		
		btnSteps=(RelativeLayout)findViewById(R.id.btn_steps);
		btnSteps.setOnClickListener(listener);
		
		btnClock=(RelativeLayout)findViewById(R.id.btn_clock);
		btnClock.setOnClickListener(listener);
		
		btnMusic=(RelativeLayout)findViewById(R.id.btn_music);
		btnMusic.setOnClickListener(listener);
		
		btnMine=(RelativeLayout)findViewById(R.id.btn_mine);
		btnMine.setOnClickListener(listener);
		
		btnSetting=(RelativeLayout)findViewById(R.id.btn_setting);
		btnSetting.setOnClickListener(listener);
		
		brasConnectionLayout=(RelativeLayout)findViewById(R.id.bra_connection_layout);
		brasConnectionLayout.setOnClickListener(listener);
		
		btnRecognize=(Button)findViewById(R.id.btn_recognize);
		btnRecognize.setOnClickListener(listener);
		
		ivConnectionLeft=(ImageView)findViewById(R.id.iv_connection_left);
		if(BleService.isLeftConnected){
			ivConnectionLeft.setImageResource(R.drawable.bra_left_connected);
		}else{
			ivConnectionLeft.setImageResource(R.drawable.bra_left_disconnected);
		}
		
		ivConnectionRight=(ImageView)findViewById(R.id.iv_connection_right);
		if(BleService.isRightConnected){
			ivConnectionRight.setImageResource(R.drawable.bra_right_connected);
		}else{
			ivConnectionRight.setImageResource(R.drawable.bra_right_disconnected);
		}
						
		tvElectricityLeft=(TextView)findViewById(R.id.tv_electricity_left);				
		tvElectricityRight=(TextView)findViewById(R.id.tv_electricity_right);				
		checkedLowPower();		
		tvElectricityLeft.setText(BleService.electricityLeft+"%");
		tvElectricityRight.setText(BleService.electricityRight+"%");
	}
	
	private void changeLanguage(String language){
		Configuration config=getResources().getConfiguration();
		if(language.equals("en")){
			config.locale=Locale.ENGLISH;
		}else if(language.equals("zh")){
			config.locale=Locale.SIMPLIFIED_CHINESE;			
		}else if(language.equals("auto")){
			config.locale=Locale.getDefault();
		}
		getResources().updateConfiguration(config, getResources().getDisplayMetrics());
	}
	
	private void initRecognizer(){
		SpeechUtility.createUtility(this, SpeechConstant.APPID+"=56f361ec");
		recognizer=SpeechRecognizer.createRecognizer(this, null);
		recognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		recognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
		recognizer.setParameter(SpeechConstant.VAD_BOS, "4000"); 
		recognizer.setParameter(SpeechConstant.VAD_EOS, "1000");
		recognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8"); 
		recognizer.setParameter(SpeechConstant.ASR_PTT, "0");
		
		try {
			InputStream in=getAssets().open("userwords");
			byte[] buffer=new byte[in.available()];
			in.read(buffer, 0, in.available());
			String words=new String(buffer);
			recognizer.updateLexicon("userword", words, new LexiconListener() {
				
				@Override
				public void onLexiconUpdated(String arg0, SpeechError error) {
					// TODO Auto-generated method stub
				
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("zz", e.toString());
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
		unregisterReceiver(receiver);
		recognizer.cancel();
		recognizer.destroy();
	}
	
	private RecognizerListener recognizerListener=new RecognizerListener() {
		
		@Override
		public void onVolumeChanged(int arg0, byte[] arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onResult(RecognizerResult results, boolean arg1) {
			// TODO Auto-generated method stub
			String text =parseResult(results.getResultString());
			if(text.equals("开启按摩")||text.equals("start massage")){
				BleService.isMassaging=true;
				sendBroadcast(new Intent("start_massage"));
			}
			if(text.equals("停止按摩")||text.equals("stop massage")){
				BleService.isMassaging=false;
				sendBroadcast(new Intent("stop_massage"));
			}
			if(text.equals("揉")||text.equals("rub")){
				if(BleService.isMassaging){
					BleService.mode=1;
					sendBroadcast(new Intent("start_massage"));
				}				
			}
			if(text.equals("捏")||text.equals("pinch")){
				if(BleService.isMassaging){
					BleService.mode=2;
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("推")||text.equals("pel")){
				if(BleService.isMassaging){
					BleService.mode=3;
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("拍")||text.equals("pat")){
				if(BleService.isMassaging){
					BleService.mode=4;
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("按")||text.equals("press")){
				if(BleService.isMassaging){
					BleService.mode=5;
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("自动")||text.equals("auto")||text.equals("automatic")){
				if(BleService.isMassaging){
					BleService.mode=6;
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("用力一点")||text.equals("用力点")||text.equals("more strength")){
				if(BleService.isMassaging){
					BleService.strength+=10;
					if(BleService.strength>100){
						BleService.strength=100;
					}
					sendBroadcast(new Intent("start_massage"));
				}
			}
			if(text.equals("轻一点")){
				if(BleService.isMassaging){
					BleService.strength-=10;
					if(BleService.strength<=0){
						BleService.strength=1;
					}
					sendBroadcast(new Intent("start_massage"));
				}
			}
			dialog.dismiss();
		}
		
		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onError(SpeechError arg0) {
			// TODO Auto-generated method stub
			dialog.dismiss();
		}
		
		@Override
		public void onEndOfSpeech() {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onBeginOfSpeech() {
			// TODO Auto-generated method stub
			dialog.show();
		}
	};
	
	private String parseResult(String json) {
		StringBuffer sb = new StringBuffer();
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);

			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {
				// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				sb.append(obj.getString("w"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return sb.toString();
	}

	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.bra_connection_layout:
				startActivity(new Intent(MainActivity.this, ConnectionActivity.class));
				break;
			case R.id.btn_massage:
				startActivity(new Intent(MainActivity.this, MassageActivity.class));
				break;

			case R.id.btn_steps:
				startActivity(new Intent(MainActivity.this, StepActivity.class));
				break;
				
			case R.id.btn_clock:
				startActivity(new Intent(MainActivity.this, ClockActivity.class));
				break;
				
			case R.id.btn_music:
				startActivity(new Intent(MainActivity.this, MusicActivity.class));
				break;
				
			case R.id.btn_mine:
				startActivity(new Intent(MainActivity.this, MineActivity.class));
				break;
				
			case R.id.btn_setting:
				startActivity(new Intent(MainActivity.this, SettingActivity.class));
				break;
				
			case R.id.btn_recognize:
				recognizer.startListening(recognizerListener);				
				break;
			}
		}
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
						
		if(keyCode==KeyEvent.KEYCODE_BACK){
			long backSecondTime=System.currentTimeMillis();
			if(backSecondTime-backFirstTime>1500){
				Toast.makeText(this, getString(R.string.one_more_click_exit), Toast.LENGTH_SHORT).show();
				backFirstTime=backSecondTime;
				return true;
			}else{
				stopService(new Intent(this, BleService.class));
				finish();
			}
		}
		return false;
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("device_connected")){
				if(intent.getStringExtra("name").endsWith("L")){
					ivConnectionLeft.setImageResource(R.drawable.bra_left_connected);
				}else{
					ivConnectionRight.setImageResource(R.drawable.bra_right_connected);
				}
				
			}
			if(action.equals("device_disconnected")){
				checkedLowPower();
				if(!BleService.isLeftConnected){
					ivConnectionLeft.setImageResource(R.drawable.bra_left_disconnected);					
					tvElectricityLeft.setText(BleService.electricityLeft+"%");
				}
				if(!BleService.isRightConnected){
					ivConnectionRight.setImageResource(R.drawable.bra_right_disconnected);
					tvElectricityRight.setText(BleService.electricityRight+"%");
				}
			}
			if(action.equals("request_disconnect")){
				if(intent.getStringExtra("which").equals("L")){
					
					ivConnectionLeft.setImageResource(R.drawable.bra_left_disconnected);
					BleService.electricityLeft=0;
					checkedLowPower();
					tvElectricityLeft.setText(BleService.electricityLeft+"%");
				}else{
					ivConnectionRight.setImageResource(R.drawable.bra_right_disconnected);
					BleService.electricityRight=0;
					checkedLowPower();
					tvElectricityRight.setText(BleService.electricityRight+"%");
				}
			}
			if(action.equals("update_electricity")){
				checkedLowPower();
				tvElectricityLeft.setText(BleService.electricityLeft+"%");
				tvElectricityRight.setText(BleService.electricityRight+"%");
			}
		}
		
	}
	
	//检测是否低电量，更改UI字体颜色
	private void checkedLowPower(){
		if(BleService.electricityLeft<=20){
			tvElectricityLeft.setTextColor(Color.RED);
		}else{
			tvElectricityLeft.setTextColor(0xff339F3E);;
		}
		if(BleService.electricityRight<=20){
			tvElectricityRight.setTextColor(Color.RED);
		}else{
			tvElectricityRight.setTextColor(0xff339F3E);;
		}
	}
	
}
