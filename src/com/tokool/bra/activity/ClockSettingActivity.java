package com.tokool.bra.activity;

import java.util.Date;

import com.tokool.bra.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;

public class ClockSettingActivity extends Activity {

	private ImageView btnBack;
	private NumberPicker npHour;
	private NumberPicker npMinute;
	private RadioButton btnOneShot, btnEveryday, btnWeekdays, btnCustom;
	private CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7;
	private LinearLayout cbLayout;
	private Button btnEnsure;
	private MyOnCheckedChangeListener listener;
	
	private boolean[] day;
	@SuppressWarnings("deprecation")
	private int hour=new Date().getHours();
	@SuppressWarnings("deprecation")
	private int minute=new Date().getMinutes();
	private int mode=0;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_clock_setting);
		
		day=new boolean[7];
		for(int i=0; i<day.length; i++){
			day[i]=false;
		}
		
		Intent intent=getIntent();
		boolean isSet=intent.getBooleanExtra("isSet", false);
		if(isSet){
			mode=intent.getIntExtra("mode", 0);
			hour=intent.getIntExtra("hour", new Date().getHours());
			minute=intent.getIntExtra("minute", new Date().getMinutes());
			day=intent.getBooleanArrayExtra("day");
		}
		
		listener=new MyOnCheckedChangeListener();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		npHour=(NumberPicker)findViewById(R.id.number_picker_hour);
		npHour.setMinValue(0);
		npHour.setMaxValue(23);	
		npHour.setValue(hour);
		npHour.setLable(getString(R.string.hour));
		npHour.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker arg0, int arg1, int current) {
				// TODO Auto-generated method stub
				hour=current;
			}
		});
		
		npMinute=(NumberPicker)findViewById(R.id.number_picker_minute);
		npMinute.setMinValue(0);
		npMinute.setMaxValue(59);	
		npMinute.setValue(minute);
		npMinute.setLable(getString(R.string.minute));
		npMinute.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker arg0, int last, int current) {
				// TODO Auto-generated method stub
				minute=current;
			}
		});	
		

		cbLayout=(LinearLayout)findViewById(R.id.cb_layout);
		
		btnOneShot=(RadioButton)findViewById(R.id.radio_btn_0);
		btnOneShot.setOnCheckedChangeListener(listener);
		
		btnEveryday=(RadioButton)findViewById(R.id.radio_btn_1);
		btnEveryday.setOnCheckedChangeListener(listener);
		
		btnWeekdays=(RadioButton)findViewById(R.id.radio_btn_2);
		btnWeekdays.setOnCheckedChangeListener(listener);
		
		btnCustom=(RadioButton)findViewById(R.id.radio_btn_3);
		btnCustom.setOnCheckedChangeListener(listener);
		
		switch (mode) {
		case 0:
			btnOneShot.setChecked(true);
			break;
			
		case 1:
			btnEveryday.setChecked(true);
			break;

		case 2:
			btnWeekdays.setChecked(true);
			break;
			
		case 3:
			btnCustom.setChecked(true);
			break;
		}
		
		
		cb1=(CheckBox)findViewById(R.id.cb1);
		cb1.setChecked(day[0]);
		cb2=(CheckBox)findViewById(R.id.cb2);	
		cb2.setChecked(day[1]);
		cb3=(CheckBox)findViewById(R.id.cb3);
		cb3.setChecked(day[2]);
		cb4=(CheckBox)findViewById(R.id.cb4);	
		cb4.setChecked(day[3]);
		cb5=(CheckBox)findViewById(R.id.cb5);	
		cb5.setChecked(day[4]);
		cb6=(CheckBox)findViewById(R.id.cb6);
		cb6.setChecked(day[5]);
		cb7=(CheckBox)findViewById(R.id.cb7);
		cb7.setChecked(day[6]);
		
		btnEnsure=(Button)findViewById(R.id.btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mode==0){
					for(int i=0; i<day.length; i++){
						day[i]=false;
					}
				}else if(mode==1){
					for(int i=0; i<day.length; i++){
						day[i]=true;
					}
				}else if(mode==2){
					for(int i=0; i<day.length-2; i++){
						day[i]=true;
					}
				}else{
					day[0]=cb1.isChecked();
					day[1]=cb2.isChecked();
					day[2]=cb3.isChecked();
					day[3]=cb4.isChecked();
					day[4]=cb5.isChecked();
					day[5]=cb6.isChecked();
					day[6]=cb7.isChecked();
				}
				Intent intent=new Intent();
				intent.putExtra("day", day);
				intent.putExtra("hour", hour);
				intent.putExtra("minute", minute);
				intent.putExtra("mode", mode);
				setResult(1, intent);
				finish();
			}
		});
	}
	
	private class MyOnCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
			// TODO Auto-generated method stub
			if(isChecked){
				switch (compoundButton.getId()) {
				case R.id.radio_btn_0:
					mode=0;
					cbLayout.setVisibility(View.INVISIBLE);
					break;
					
				case R.id.radio_btn_1:				
					mode=1;
					cbLayout.setVisibility(View.INVISIBLE);
					break;
	
				case R.id.radio_btn_2:
					mode=2;
					cbLayout.setVisibility(View.INVISIBLE);
					break;
					
				case R.id.radio_btn_3:
					mode=3;					
					cbLayout.setVisibility(View.VISIBLE);
					break;
				
				}
			}
		}
		
	}


}
