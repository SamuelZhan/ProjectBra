package com.tokool.bra.activity;

import com.tokool.bra.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ChangeLanguageActivity extends Activity {
	
	private ImageView btnBack;
	private RadioGroup radioGroupLanguage;
	private Button btnEnsure;
	private String currentLanguage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_language);
		
		currentLanguage=getSharedPreferences("language", Context.MODE_PRIVATE).getString("language", "zh");	
		if(currentLanguage.equals("en")){
			((RadioButton)findViewById(R.id.btn_english)).setChecked(true);
		}else if(currentLanguage.equals("zh")){
			((RadioButton)findViewById(R.id.btn_chinese)).setChecked(true);
		}else if(currentLanguage.equals("auto")){
			((RadioButton)findViewById(R.id.btn_auto)).setChecked(true);
		}
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		radioGroupLanguage=(RadioGroup)findViewById(R.id.radiogroup_language);
		
		btnEnsure=(Button)findViewById(R.id.btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor=getSharedPreferences("language", Context.MODE_PRIVATE).edit();
				String language=null;
				int id=radioGroupLanguage.getCheckedRadioButtonId();
				if(id==R.id.btn_chinese){
					language="zh";
				}else if(id==R.id.btn_english){
					language="en";
				}else if(id==R.id.btn_auto){
					language="auto";
				}
				if(currentLanguage.equals(language)){
					finish();
				}else{
					editor.putString("language", language).commit();
					Intent intent=new Intent(ChangeLanguageActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			}
		});
		
	}

}
