package com.tokool.bra.activity;

import java.util.ArrayList;
import com.tokool.bra.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class GuideActivity extends Activity {
	
	private ViewPager pager;
	private RadioGroup radioGroup;
	private ImageView btnEnter;
	private ArrayList<View> views;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		boolean isFirstUse=getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).getBoolean("isFirstUse", true);
		if(!isFirstUse){
			startActivity(new Intent(GuideActivity.this, FlashActivity.class));
			finish();
		}
		
		setContentView(R.layout.activity_guide);
		
		radioGroup=(RadioGroup)findViewById(R.id.radiogroup);
		((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
		
		btnEnter=(ImageView)findViewById(R.id.btn_enter);
		btnEnter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(GuideActivity.this, FlashActivity.class));
				finish();
				getSharedPreferences("isFirstUse", Context.MODE_PRIVATE).edit().putBoolean("isFirstUse", false).commit();
			}
		});
		
		views=new ArrayList<View>();		
		ImageView view1=new ImageView(this);
		view1.setBackgroundResource(R.drawable.guide1);
		views.add(view1);
		ImageView view2=new ImageView(this);
		view2.setBackgroundResource(R.drawable.guide2);
		views.add(view2);
		ImageView view3=new ImageView(this);
		view3.setBackgroundResource(R.drawable.guide3);
		views.add(view3);
				
		pager=(ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new MyPagerAdapter());
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				((RadioButton)radioGroup.getChildAt(position)).setChecked(true);
				if(position==2){
					btnEnter.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private class MyPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			((ViewPager)container).addView(views.get(position),0);
			return views.get(position);
		}						
	}
	
}
