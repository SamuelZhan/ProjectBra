package com.tokool.bra.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tokool.bra.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.simonvt.numberpicker.NumberPicker;

public class MineActivity extends Activity {
	
	private ImageView btnBack, btnImage;
	private TextView btnNickname, tvAge, tvHeight, tvWeight, tvStepLength, tvTargetSteps;
	private RelativeLayout btnAge, btnHeight, btnWeight, btnStepLength, btnTargetSteps;
	private MyOnClickListener listener;
	private PopupWindow popup;
	
	private int age, height, weight, stepLength, targetSteps;
	private String nickname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mine);
		
		listener=new MyOnClickListener();
		
		age=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("age", 24);
		height=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("height", 165);
		weight=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("weight", 65);
		stepLength=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("stepLength", 60);
		targetSteps=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
		nickname=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("nickname", "Miss Bras");
		
		popup=new PopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		popup.setFocusable(true);
		popup.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				WindowManager.LayoutParams params=getWindow().getAttributes();
				params.alpha=1f;
				getWindow().setAttributes(params);
			}
		});
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnImage=(ImageView)findViewById(R.id.btn_image);
		try {
			btnImage.setImageBitmap(BitmapFactory.decodeStream(openFileInput("headImage")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			btnImage.setImageResource(R.drawable.mine_head_image);
		}
		
		btnImage.setOnClickListener(listener);
		
		btnNickname=(TextView)findViewById(R.id.btn_nickname);
		btnNickname.setText(nickname);
		btnNickname.setOnClickListener(listener);
		
		btnAge=(RelativeLayout)findViewById(R.id.btn_age);
		btnAge.setOnClickListener(listener);
		
		btnHeight=(RelativeLayout)findViewById(R.id.btn_height);
		btnHeight.setOnClickListener(listener);
		
		btnWeight=(RelativeLayout)findViewById(R.id.btn_weight);			
		btnWeight.setOnClickListener(listener);
		
		btnStepLength=(RelativeLayout)findViewById(R.id.btn_stepLength);
		btnStepLength.setOnClickListener(listener);
		
		btnTargetSteps=(RelativeLayout)findViewById(R.id.btn_targetSteps);
		btnTargetSteps.setOnClickListener(listener);
		
		tvAge=(TextView)findViewById(R.id.tv_age);
		tvAge.setText(age+"");
		
		tvHeight=(TextView)findViewById(R.id.tv_height);
		tvHeight.setText(height+"cm");
		
		tvWeight=(TextView)findViewById(R.id.tv_weight);
		tvWeight.setText(weight+"kg");
		
		tvStepLength=(TextView)findViewById(R.id.tv_stepLength);
		tvStepLength.setText(stepLength+"cm");
		
		tvTargetSteps=(TextView)findViewById(R.id.tv_targetSteps);
		tvTargetSteps.setText(targetSteps+" "+getString(R.string.step));
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(popup!=null){
			popup.dismiss();
		}
	}

	private class MyOnClickListener implements OnClickListener{

		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
				
			case R.id.btn_image:			
				WindowManager.LayoutParams params=getWindow().getAttributes();
				params.alpha=0.7f;
				getWindow().setAttributes(params);
				View view=getLayoutInflater().inflate(R.layout.popup_image_selection, null);
				Button btnAlbum=(Button)view.findViewById(R.id.btn_album);
				btnAlbum.setOnClickListener(this);
				Button btnCamera=(Button)view.findViewById(R.id.btn_camera);
				btnCamera.setOnClickListener(this);
				Button btnCancel=(Button)view.findViewById(R.id.btn_cancel);
				btnCancel.setOnClickListener(this);
				popup.setContentView(view);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_nickname:
				View dialogContent=getLayoutInflater().inflate(R.layout.dialog_input_nickname, null);
				final ImageView btnRemove=(ImageView)dialogContent.findViewById(R.id.btn_remove);				
				final EditText etNickname=(EditText)dialogContent.findViewById(R.id.et_nickname);
				btnRemove.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						etNickname.setText("");
					}
				});
				etNickname.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
						// TODO Auto-generated method stub
						if(etNickname.getText().toString().length()>0){
							btnRemove.setVisibility(View.VISIBLE);
						}else{
							btnRemove.setVisibility(View.INVISIBLE);
						}
					}
					
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterTextChanged(Editable arg0) {
						// TODO Auto-generated method stub
						
					}
				});
				final AlertDialog dialog=new AlertDialog.Builder(MineActivity.this, R.style.MyDialogTheme).setView(dialogContent).create();
				dialog.show();
				dialog.getWindow().setLayout((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics()),
						(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
				
				((Button)dialogContent.findViewById(R.id.btn_cancel)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});	
				((Button)dialogContent.findViewById(R.id.btn_ok)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						nickname=etNickname.getText().toString();
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putString("nickname", nickname).commit();
						dialog.dismiss();
						btnNickname.setText(nickname);
					}
				});
				break;
				
			case R.id.btn_age:
				WindowManager.LayoutParams params1=getWindow().getAttributes();
				params1.alpha=0.7f;
				getWindow().setAttributes(params1);
				View view1=getLayoutInflater().inflate(R.layout.popup_age_height_weight_selection, null);
				((TextView)view1.findViewById(R.id.tv_popup_title)).setText(getString(R.string.age_selection));
				final NumberPicker npAge=(NumberPicker)view1.findViewById(R.id.number_picker);
				npAge.setMinValue(5);
				npAge.setMaxValue(70);
				npAge.setValue(age);
				npAge.setLable(" "+getString(R.string.age));
				((TextView)view1.findViewById(R.id.btn_cancel)).setOnClickListener(this);				
				((TextView)view1.findViewById(R.id.btn_ensure)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						age=npAge.getValue();
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("age", age).commit();
						tvAge.setText(age+"");
						popup.dismiss();
					}
				});
				popup.setContentView(view1);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_height:
				WindowManager.LayoutParams params2=getWindow().getAttributes();
				params2.alpha=0.7f;
				getWindow().setAttributes(params2);
				View view2=getLayoutInflater().inflate(R.layout.popup_age_height_weight_selection, null);
				((TextView)view2.findViewById(R.id.tv_popup_title)).setText(getString(R.string.weight_selection));
				final NumberPicker npHeight=(NumberPicker)view2.findViewById(R.id.number_picker);
				npHeight.setMinValue(50);
				npHeight.setMaxValue(300);
				npHeight.setValue(height);
				npHeight.setLable(" cm");
				((TextView)view2.findViewById(R.id.btn_cancel)).setOnClickListener(this);				
				((TextView)view2.findViewById(R.id.btn_ensure)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						height=npHeight.getValue();
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("height", height).commit();
						tvHeight.setText(height+"cm");
						popup.dismiss();
					}
				});
				popup.setContentView(view2);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_weight:
				WindowManager.LayoutParams params3=getWindow().getAttributes();
				params3.alpha=0.7f;
				getWindow().setAttributes(params3);
				View view3=getLayoutInflater().inflate(R.layout.popup_age_height_weight_selection, null);
				((TextView)view3.findViewById(R.id.tv_popup_title)).setText(getString(R.string.weight_selection));
				final NumberPicker npWeight=(NumberPicker)view3.findViewById(R.id.number_picker);
				npWeight.setMinValue(20);
				npWeight.setMaxValue(100);
				npWeight.setValue(weight);
				npWeight.setLable(" kg");
				((TextView)view3.findViewById(R.id.btn_cancel)).setOnClickListener(this);				
				((TextView)view3.findViewById(R.id.btn_ensure)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						weight=npWeight.getValue();
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("weight", weight).commit();
						tvWeight.setText(weight+"kg");
						popup.dismiss();
					}
				});
				popup.setContentView(view3);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_stepLength:
				WindowManager.LayoutParams params4=getWindow().getAttributes();
				params4.alpha=0.7f;
				getWindow().setAttributes(params4);
				View view4=getLayoutInflater().inflate(R.layout.popup_age_height_weight_selection, null);
				((TextView)view4.findViewById(R.id.tv_popup_title)).setText(getString(R.string.step_length_selection));
				final NumberPicker npStepLength=(NumberPicker)view4.findViewById(R.id.number_picker);
				npStepLength.setMinValue(20);
				npStepLength.setMaxValue(150);
				npStepLength.setValue(stepLength);
				npStepLength.setLable(" cm");
				((TextView)view4.findViewById(R.id.btn_cancel)).setOnClickListener(this);				
				((TextView)view4.findViewById(R.id.btn_ensure)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						stepLength=npStepLength.getValue();
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("stepLength", stepLength).commit();
						tvStepLength.setText(stepLength+"cm");
						popup.dismiss();
					}
				});
				popup.setContentView(view4);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_targetSteps:
				WindowManager.LayoutParams params5=getWindow().getAttributes();
				params5.alpha=0.7f;
				getWindow().setAttributes(params5);
				View view5=getLayoutInflater().inflate(R.layout.popup_age_height_weight_selection, null);
				((TextView)view5.findViewById(R.id.tv_popup_title)).setText(getString(R.string.target_selection));
				final NumberPicker npTargetSteps=(NumberPicker)view5.findViewById(R.id.number_picker);				
				String[] valuesString=new String[30];
				int temp=1000;
				for(int i=0; i<valuesString.length; i++){
					valuesString[i]=Integer.toString(temp);
					temp+=1000;
				}
				npTargetSteps.setDisplayedValues(valuesString);
				npTargetSteps.setMinValue(0);
				npTargetSteps.setMaxValue(29);
				npTargetSteps.setValue(targetSteps/1000-1);
				npTargetSteps.setLable(" "+getString(R.string.step));
				((TextView)view5.findViewById(R.id.btn_cancel)).setOnClickListener(this);				
				((TextView)view5.findViewById(R.id.btn_ensure)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						targetSteps=(npTargetSteps.getValue()+1)*1000;
						getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putInt("targetSteps", targetSteps).commit();
						tvTargetSteps.setText(targetSteps+" "+getString(R.string.step));
						popup.dismiss();
					}
				});
				popup.setContentView(view5);						
				popup.setBackgroundDrawable(new BitmapDrawable());
				popup.setAnimationStyle(R.style.popupwindow_display_anim);
				popup.showAtLocation(findViewById(R.id.mine_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
				
			case R.id.btn_cancel:
				if(popup.isShowing()){
					popup.dismiss();
				}
				break;
				
			case R.id.btn_album:
				Intent intentAlbum=new Intent(Intent.ACTION_PICK);
				intentAlbum.setType("image/*");
				startActivityForResult(intentAlbum, 1);
				popup.dismiss();
				break;
				
			case R.id.btn_camera:
				Intent intentCamera=new Intent("android.media.action.IMAGE_CAPTURE");
				startActivityForResult(intentCamera, 2);
				popup.dismiss();
				break;

			}
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				return;
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);
			
			startActivityForResult(intent, 3);
		}
		if(requestCode==2 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				Bundle bundle=data.getExtras();
				if(bundle!=null){
					Log.d("zz", "bundle 返回");
					Bitmap bitmap=(Bitmap)bundle.get("data");
					File f=new File(Environment.getExternalStorageDirectory()+"/bra", "clipImage.png");
					if(f.exists()){
						f.delete();
					}
					try{
						FileOutputStream fos=new FileOutputStream(f);
						bitmap.compress(CompressFormat.PNG, 80, fos);
						fos.flush();
						fos.close();
					}catch(Exception e){						
						Log.d("zz", e.toString());
						return;
					}
					uri=Uri.fromFile(f);
				}
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);				
			startActivityForResult(intent, 3);
				
			
		}
		if(requestCode==3 && resultCode==RESULT_OK){
			final Bitmap bitmap=data.getParcelableExtra("data");
			btnImage.setImageBitmap(bitmap);
			
			try {
				ByteArrayOutputStream baos=new ByteArrayOutputStream();						
				bitmap.compress(CompressFormat.PNG, 10, baos);
				FileOutputStream fos=openFileOutput("headImage", MODE_PRIVATE);			
				fos.write(baos.toByteArray());
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}								
			
		}
		
	}
	
	

}
