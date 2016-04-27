package com.tokool.bra.activity;

import java.util.Locale;

import com.tokool.bra.R;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;

public class FlashActivity extends Activity {
	
	private ImageView ivFlash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_flash);
		
		ivFlash=(ImageView)findViewById(R.id.iv_flash);
		String language=getSharedPreferences("language", Context.MODE_PRIVATE).getString("language", "zh");
		if(language.equals("en")){
			ivFlash.setImageResource(R.drawable.flash_english);
		}else if(language.equals("zh")){
			ivFlash.setImageResource(R.drawable.flash);
		}else{
			String local=Locale.getDefault().toString();
			if(local.equals("en_US") || local.equals("en_GB")){
				ivFlash.setImageResource(R.drawable.flash_english);
			}else{
				ivFlash.setImageResource(R.drawable.flash);
			}
		}
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent=new Intent(FlashActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 1200);
		
		startService(new Intent(this, BleService.class));
	}	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			//什么都不做
		}
		return true;
	}

}
