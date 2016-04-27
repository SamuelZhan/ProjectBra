package com.tokool.bra.activity;

import com.tokool.bra.R;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MassageActivity extends Activity {

	private ImageView btnBack;
	private CheckBox btnPlayOrStop;
	private RadioGroup group1, group2, group3;
	private RadioButton btnLeft, btnBoth, btnRight, btn1, btn2, btn3, btn4, btn5, btn6;
	private SeekBar sbTime, sbStrength;
	private TextView tvTime, tvStrength;
	private MyOnCheckedChangeListener listener;
	private MyBroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_massage);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter("update_massage_status");
		registerReceiver(receiver, filter);
		
		listener=new MyOnCheckedChangeListener();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnPlayOrStop=(CheckBox)findViewById(R.id.cb_play_or_stop);
		btnPlayOrStop.setChecked(BleService.isMassaging);
		btnPlayOrStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					BleService.isMassaging=true;
					sendBroadcast(new Intent().setAction("start_massage"));
				}else{
					BleService.isMassaging=false;
					sendBroadcast(new Intent().setAction("stop_massage"));
				}
			}
		});
						
		btnLeft=(RadioButton)findViewById(R.id.radio_btn_left);
		btnBoth=(RadioButton)findViewById(R.id.radio_btn_both);
		btnRight=(RadioButton)findViewById(R.id.radio_btn_right);
		btn1=(RadioButton)findViewById(R.id.radio_btn_1);
		setBoundOfDrawable(btn1);
		btn2=(RadioButton)findViewById(R.id.radio_btn_2);
		setBoundOfDrawable(btn2);
		btn3=(RadioButton)findViewById(R.id.radio_btn_3);
		setBoundOfDrawable(btn3);
		btn4=(RadioButton)findViewById(R.id.radio_btn_4);
		setBoundOfDrawable(btn4);
		btn5=(RadioButton)findViewById(R.id.radio_btn_5);
		setBoundOfDrawable(btn5);
		btn6=(RadioButton)findViewById(R.id.radio_btn_6);
		setBoundOfDrawable(btn6);
		
		switch (BleService.which) {
		case 1:
			btnLeft.setChecked(true);
			break;

		case 2:
			btnBoth.setChecked(true);
			break;
			
		case 3:
			btnRight.setChecked(true);
			break;
		}
		
		switch (BleService.mode) {
		case 1:
			btn1.setChecked(true);
			break;

		case 2:
			btn2.setChecked(true);
			break;
			
		case 3:
			btn3.setChecked(true);
			break;
			
		case 4:
			btn4.setChecked(true);
			break;
			
		case 5:
			btn5.setChecked(true);
			break;
			
		case 6:
			btn6.setChecked(true);
			break;
		}
		
		group1=(RadioGroup)findViewById(R.id.radio_group_1);
		group1.setOnCheckedChangeListener(listener);
		
		group2=(RadioGroup)findViewById(R.id.radio_group_2);
		group2.setOnCheckedChangeListener(listener);
		
		group3=(RadioGroup)findViewById(R.id.radio_group_3);
		group3.setOnCheckedChangeListener(listener);
		
		tvTime=(TextView)findViewById(R.id.tv_time);
		
		sbTime=(SeekBar)findViewById(R.id.seekbar_time);
		sbTime.setProgress(BleService.time);
		sbTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
				BleService.time=seekBar.getProgress();
				if(BleService.isMassaging){
					sendBroadcast(new Intent().setAction("start_massage"));
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar sb, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				float seekBarWidth=sbTime.getWidth()-dp2px(50);
				float translation=seekBarWidth*((float)progress/sb.getMax());
				tvTime.setTranslationX(translation);
				tvTime.setText(progress+"min");
			}
		});
		
		ViewTreeObserver vto1=sbTime.getViewTreeObserver();
		vto1.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				sbTime.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				float seekBarWidth=sbTime.getWidth()-dp2px(50);
				float translationLength=seekBarWidth*((float)sbTime.getProgress()/sbTime.getMax());
				tvTime.setTranslationX(translationLength);
				tvTime.setText(sbTime.getProgress()+"min");
			}
		});	
		
		tvStrength=(TextView)findViewById(R.id.tv_strength);
		
		sbStrength=(SeekBar)findViewById(R.id.seekbar_strength);
		sbStrength.setProgress(BleService.strength);
		sbStrength.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if(seekBar.getProgress()==0){
					BleService.strength=1;
				}else{
					BleService.strength=seekBar.getProgress();
				}				
				if(BleService.isMassaging){
					sendBroadcast(new Intent().setAction("start_massage"));
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar sb, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				float seekBarWidth=sbStrength.getWidth()-dp2px(50);
				float translation=seekBarWidth*((float)progress/sb.getMax());
				tvStrength.setTranslationX(translation);				
				if(progress==0){
					tvStrength.setText("1");
				}else{
					tvStrength.setText(""+progress);
				}
				
			}
		});
		
		ViewTreeObserver vto2=sbStrength.getViewTreeObserver();
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				sbStrength.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				float seekBarWidth=sbStrength.getWidth()-dp2px(50);
				float translationLength=seekBarWidth*((float)sbStrength.getProgress()/sbStrength.getMax());
				tvStrength.setTranslationX(translationLength);
				if(sbStrength.getProgress()==0){
					tvStrength.setText("1");
				}else{
					tvStrength.setText(""+sbStrength.getProgress());
				}				
			}
		});	
		
	
	}
	
	private void setBoundOfDrawable(RadioButton btn){
		btn.getCompoundDrawables()[1].setBounds(dp2px(5), 0, dp2px(45), dp2px(40));
	}
	
	private class MyOnCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			
			switch (checkedId) {
			
			case R.id.radio_btn_left:
				BleService.which=1;
				if(BleService.isMassaging){
					sendBroadcast(new Intent().setAction("start_massage"));
				}
				break;
				
			case R.id.radio_btn_both:
				BleService.which=2;
				if(BleService.isMassaging){
					sendBroadcast(new Intent().setAction("start_massage"));
				}
				break;
				
			case R.id.radio_btn_right:
				BleService.which=3;
				if(BleService.isMassaging){
					sendBroadcast(new Intent().setAction("start_massage"));
				}
				break;
				
			case R.id.radio_btn_1:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					group3.clearCheck();	
					BleService.mode=1;
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;

			case R.id.radio_btn_2:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					BleService.mode=2;
					group3.clearCheck();
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;
				
			case R.id.radio_btn_3:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					BleService.mode=3;
					group3.clearCheck();
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;
				
			case R.id.radio_btn_4:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					BleService.mode=4;
					group2.clearCheck();
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;
				
			case R.id.radio_btn_5:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					BleService.mode=5;
					group2.clearCheck();
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;
				
			case R.id.radio_btn_6:
				if(((RadioButton)findViewById(checkedId)).isChecked()){
					BleService.mode=6;
					group2.clearCheck();
					if(BleService.isMassaging){
						sendBroadcast(new Intent().setAction("start_massage"));
					}
					
				}
				break;				
			
			}
			
		}
		
	}
	
	//dp转px
	private int dp2px(int dp){
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if(arg1.getAction().equals("update_massage_status")){
				btnPlayOrStop.setChecked(BleService.isMassaging);
				sbTime.setProgress(BleService.time);
			}
			
		}
		
	}

}
