package com.tokool.bra.activity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.tokool.bra.R;
import com.tokool.bra.customview.RiseNumberTextView;
import com.tokool.bra.customview.RoundProgressBar;
import com.tokool.bra.database.DatabaseHelper;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StepActivity extends Activity {
	
	private ImageView btnBack, btnPrevious, btnNext;
	private TextView tvDateShow, tvSteps2, tvSteps3, tvPercent, tvCalorie, tvDistance, tvTargetSteps, tvMaxSteps;
	private RiseNumberTextView tvSteps1;
	private RoundProgressBar progress;
	private RelativeLayout chartLayout;
	private MyBroadcastReceiver receiver;
	private long currentTime;
	private long showTime;
	private String dateNowString;
	private String dateShowString;
	
	private SQLiteDatabase db;
	
	private int weight;
	private int stepLength;
	private int targetSteps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_step);

		weight=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("weight", 65);
		stepLength=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("stepLength", 50);
		targetSteps=getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
		
		DatabaseHelper dbHelper=new DatabaseHelper(this, "steps");		
		db=dbHelper.getWritableDatabase();
		
		currentTime=System.currentTimeMillis();
		showTime=currentTime;
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		dateNowString=dateFormat.format(Calendar.getInstance().getTime());
		dateShowString=dateNowString;		
		
		Cursor c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateNowString});
		if(!c.moveToNext()){
			db.execSQL("INSERT INTO steps VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{0, 10000, 
					0, 0f, null, dateNowString});
		}
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("update_steps");
		filter.addAction("history_steps");
		filter.addAction("time_to_24_hour");
		registerReceiver(receiver, filter);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnPrevious=(ImageView)findViewById(R.id.btn_previous);
		btnPrevious.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showTime-=24*3600*1000L;
				changeDate();
			}
		});
		
		btnNext=(ImageView)findViewById(R.id.btn_next);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showTime+=24*3600*1000L;
				changeDate();
			}
		});
		
		tvDateShow=(TextView)findViewById(R.id.tv_dateShow);
		tvDateShow.setText(dateNowString);
		
		tvSteps1=(RiseNumberTextView)findViewById(R.id.tv_steps_1);
		tvSteps1.setNumber(BleService.steps);
		
		tvSteps2=(TextView)findViewById(R.id.tv_steps_2);
		tvSteps2.setText(""+BleService.steps);
		
		tvSteps3=(TextView)findViewById(R.id.tv_steps_3);
		tvSteps3.setText(getString(R.string.total_steps)+" "+BleService.steps+" "+getString(R.string.step));
		
		tvTargetSteps=(TextView)findViewById(R.id.tv_targetSteps);
		tvTargetSteps.setText(getString(R.string.target)+" "+targetSteps+" "+getString(R.string.step));
		
		tvMaxSteps=(TextView)findViewById(R.id.tv_maxSteps);
		
		DecimalFormat df=new DecimalFormat("0.0");
		tvPercent=(TextView)findViewById(R.id.tv_percent);
		if((float)BleService.steps/targetSteps*100>100){
			tvPercent.setText("100.0%");
		}else{
			tvPercent.setText(df.format((float)BleService.steps/targetSteps*100)+"%");
		}		
		
		tvCalorie=(TextView)findViewById(R.id.tv_calorie);
		tvCalorie.setText(df.format(BleService.calorie)+"k");
		
		tvDistance=(TextView)findViewById(R.id.tv_distance);
		df=new DecimalFormat("0.00");
		tvDistance.setText(df.format(BleService.distance/1000f)); 
	
		progress=(RoundProgressBar)findViewById(R.id.progress_bar);
		progress.setProgress(((float)BleService.steps/targetSteps)*100);
		
		chartLayout=(RelativeLayout)findViewById(R.id.chart_layout);
		c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateNowString});
		if(c.moveToNext()){				
			String stepString=c.getString(c.getColumnIndex("stepString"));
			chartLayout.addView(getGraphicalView(stepString), 0);				
		}
		
		sendBroadcast(new Intent("get_history_steps"));
	}	
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		db.execSQL("UPDATE steps SET totalSteps=?, targetSteps=?, calorie=?, distance=? WHERE date=?",
				new Object[]{BleService.steps, targetSteps, BleService.calorie, 
						BleService.distance, dateNowString});
		db.close();
		unregisterReceiver(receiver);
	}

	//更新不同日期的界面
	private void changeDate(){
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		dateShowString=dateFormat.format(new Date(showTime));
		tvDateShow.setText(dateShowString);
		if(dateShowString.equals(dateNowString)){
			DecimalFormat df=new DecimalFormat("0.0");
			tvSteps1.setNumber(BleService.steps);
			tvSteps2.setText(""+BleService.steps);
			tvSteps3.setText(getString(R.string.total_steps)+" "+BleService.steps+" "+getString(R.string.step));
			tvCalorie.setText(df.format(BleService.calorie)+"k");
			tvTargetSteps.setText(getString(R.string.target)+" "+targetSteps+" "+getString(R.string.step));
			if((float)BleService.steps/targetSteps*100>100){
				tvPercent.setText("100.0%");
			}else{
				tvPercent.setText(df.format((float)BleService.steps/targetSteps*100)+"%");
			}	
			df=new DecimalFormat("0.00");
			tvDistance.setText(df.format(BleService.distance/1000f));
			progress.setProgress(((float)BleService.steps/targetSteps)*100);
			Cursor c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateNowString});
			if(c.moveToNext()){				
				String stepString=c.getString(c.getColumnIndex("stepString"));
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(stepString), 0);				
			}
		}else{
			Cursor c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateShowString});
			if(c.moveToNext()){
				DecimalFormat df=new DecimalFormat("0.0");
				int steps=c.getInt(c.getColumnIndex("totalSteps"));
				int targetSteps=c.getInt(c.getColumnIndex("targetSteps"));
				float calorie=c.getFloat(c.getColumnIndex("calorie"));
				int distance=c.getInt(c.getColumnIndex("distance"));
				String stepString=c.getString(c.getColumnIndex("stepString"));
				tvSteps1.setNumber(steps);
				tvSteps2.setText(""+steps);
				tvSteps3.setText(getString(R.string.total_steps)+" "+steps+" "+getString(R.string.step));
				tvCalorie.setText(df.format(calorie)+"k");
				tvTargetSteps.setText(getString(R.string.target)+" "+targetSteps+" "+getString(R.string.step));
				if((float)steps/targetSteps*100>100){
					tvPercent.setText("100.0%");
				}else{
					tvPercent.setText(df.format((float)steps/targetSteps*100)+"%");
				}	
				df=new DecimalFormat("0.00");
				tvDistance.setText(df.format(distance/1000f));
				progress.setProgress(((float)steps/targetSteps)*100);				
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(stepString), 0);
				
			}else{
				Toast.makeText(StepActivity.this, getString(R.string.no_data), Toast.LENGTH_SHORT).show();
				tvSteps1.setNumber(0);
				tvSteps2.setText("0");
				tvSteps3.setText(getString(R.string.total_steps)+"0"+getString(R.string.step));
				tvCalorie.setText("0.0");
				tvTargetSteps.setText(getString(R.string.target)+10000+getString(R.string.step));
				tvPercent.setText("0.0%");				
				tvDistance.setText("0.00");
				progress.setProgress(0);
				chartLayout.removeViewAt(0);
				chartLayout.addView(getGraphicalView(null), 0);
			}	
		}
		
	}


	//获取折线图
	private GraphicalView getGraphicalView(String data){
		
		//X轴标签
		String[] labels=new String[]{"0","2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24"};
		//数据		

		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
		XYSeries series=new XYSeries("步数");
				
		//数据, 只能递加添加数据，递减添加数据会报错，原因不知，应该是源码bug
		int maxHeight=0;
		if(data==null){
			for(int i=0; i<labels.length; i++){
				series.add(i, 0);
			}	
		}else{
			series.add(0, 0);
			for(int i=0; i<data.length()/4; i++){
				int x=Integer.valueOf(data.substring(i*4, (i+1)*4), 16);
				if(x>maxHeight){
					maxHeight=x;
				}
				series.add(i+1, x);
			}
		}
		
		tvMaxSteps.setText(getString(R.string.max_steps)+maxHeight+getString(R.string.step));
		
		dataset.addSeries(series);
		
		//轴渲染器
		XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
		//X、Y的最大最小值
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(maxHeight+0.25f*maxHeight);//加上0.25倍的空间以显示数字，避免出界
		renderer.setXAxisMin(0);
		renderer.setXAxisMax(12);
		//不显示标示
		renderer.setShowLegend(false);
		//设置边距，参数上，左，下，右
		renderer.setMargins(new int[]{dp2px(35), dp2px(20), dp2px(5), dp2px(20)});
		//设置边距颜色为透明
		renderer.setMarginsColor(0x00ffffff);
		//设置XY轴是否可以延伸
		renderer.setPanEnabled(false, false);
		//设置点的大小
		renderer.setPointSize(dp2px(2));
		//设置XY轴颜色
		renderer.setAxesColor(0x0055DFC0);
		//设置折线图是否可以伸缩
		renderer.setZoomEnabled(false, false);
		//设置自定义X轴标签
		for(int i=0; i<labels.length; i++){
			renderer.addXTextLabel(i, labels[i]);
		}
		//标签文字大小
		renderer.setXLabelsColor(0xff55DFC0);
		renderer.setLabelsTextSize(sp2px(12));
		renderer.setXLabels(0);
		renderer.setYLabels(0);
		
		//折线渲染器
		XYSeriesRenderer r=new XYSeriesRenderer();
		//折线颜色
		r.setColor(0xff55DFC0);
		//折线宽度
		r.setLineWidth(2f);			
		//设置点的风格
		r.setPointStyle(PointStyle.CIRCLE);
		//设置实心
		r.setFillPoints(true);
		//显示值
		r.setDisplayChartValues(true);
		//防止值缺失
		r.setDisplayChartValuesDistance(dp2px(10));
		//值的文本大小
		r.setChartValuesTextSize(sp2px(12));
		//值与点的距离
		r.setChartValuesSpacing(dp2px(8));
		
		renderer.addSeriesRenderer(r);
		
		return ChartFactory.getLineChartView(this, dataset, renderer);
	}
	
	private int dp2px(int dp){
		
		int px=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
		return px;
		
	}
	
	private int sp2px(int sp){
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_steps")){
				if(dateNowString.equals(dateShowString)){
					DecimalFormat df=new DecimalFormat("0.0");	
					tvSteps1.setText(""+BleService.steps);
					tvSteps2.setText(""+BleService.steps);
					tvSteps3.setText(getString(R.string.total_steps)+BleService.steps+getString(R.string.step));
					progress.setProgress(((float)BleService.steps/targetSteps)*100);
					tvCalorie.setText(df.format(BleService.calorie)+"k");
					if((float)BleService.steps/targetSteps*100>100){
						tvPercent.setText("100.0%");
					}else{
						tvPercent.setText(df.format((float)BleService.steps/targetSteps*100)+"%");
					}	
					df=new DecimalFormat("0.00");
					tvDistance.setText(df.format(BleService.distance/1000f)); 
				}
				
			}
			if(action.equals("history_steps")){
				String[] historyData=intent.getStringArrayExtra("history_steps");
				long time=System.currentTimeMillis();
				for(int i=historyData.length-1; i>=0; i--){
					if(i==historyData.length-1){
						chartLayout.removeViewAt(0);
						chartLayout.addView(getGraphicalView(historyData[i]), 0);
					}
					int totalSteps=0;
					for(int j=0; j<historyData[i].length()/4; j++){
						totalSteps+=Integer.valueOf(historyData[i].substring(j*4, (j+1)*4), 16);
					}
					Date date=new Date(time-(historyData.length-i-1)*24*3600*1000L);
					SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
					String dateString=dateFormat.format(date);
					Cursor c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateString});
					if(c.moveToNext()){
						db.execSQL("UPDATE steps SET stepString=? WHERE date=?", new Object[]{historyData[i], dateString});
					}else{
						db.execSQL("INSERT INTO steps VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{totalSteps, targetSteps, 
								weight*(stepLength*totalSteps/100)/1000f*1.036f, totalSteps*stepLength/100, historyData[i], dateString});
					}
					
				}
			}
			if(action.equals("time_to_24_hour")){
				currentTime=System.currentTimeMillis();
				SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				dateNowString=dateFormat.format(Calendar.getInstance().getTime());
				Cursor c=db.rawQuery("SELECT * FROM steps WHERE date=?", new String[]{dateNowString});
				if(!c.moveToNext()){
					db.execSQL("INSERT INTO steps VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{0, 10000, 
							0, 0f, null, dateNowString});
				}
				
			}
		}
		
	}

}
