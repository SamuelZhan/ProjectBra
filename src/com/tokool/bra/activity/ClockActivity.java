package com.tokool.bra.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.tokool.bra.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ClockActivity extends Activity {
	
	private ImageView btnBack, btnDelete;
	private Button btnAdd;
	private ListView lvClocks;
	private ArrayList<HashMap<String, Object>> clocks;
	private MyBaseAdapter adapter;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_clock);
		
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnAdd=(Button)findViewById(R.id.add_clock);
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(ClockActivity.this, ClockSettingActivity.class), 1000);
			}
		});
		
		btnDelete=(ImageView)findViewById(R.id.btn_delete);
		btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				View dialogContent=getLayoutInflater().inflate(R.layout.dialog_delete, null);
				final AlertDialog dialog=new AlertDialog.Builder(ClockActivity.this).setView(dialogContent).create();
				dialog.show();
				
				Button btnCancel=(Button)dialogContent.findViewById(R.id.btn_cancel);
				btnCancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				
				Button btnOK=(Button)dialogContent.findViewById(R.id.btn_ok);
				btnOK.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);						
						AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
						for(int i=0; i<clocks.size(); i++){							
							for(int j=0; j<7; j++){
								PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, i*7+j, clockIntent, 0);
								am.cancel(pi);
							}	
						}
						clocks.clear();
						try {
							String clocksString=list2String(clocks);
							getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", clocksString).commit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
						dialog.cancel();
					}
				});
				
				
			}
		});
		
		clocks=new ArrayList<HashMap<String,Object>>();
		
		String clocksString=getSharedPreferences("clocks", Context.MODE_PRIVATE).getString("clocks", null);
		if(clocksString!=null){
			try {
				clocks.addAll((ArrayList<HashMap<String,Object>>)string2List(clocksString));
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		lvClocks=(ListView)findViewById(R.id.clocks_list);
		lvClocks.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(ClockActivity.this, ClockSettingActivity.class);
				intent.putExtra("isSet", true);
				intent.putExtra("day", (boolean[])clocks.get(position).get("day"));
				intent.putExtra("hour", (Integer)clocks.get(position).get("hour"));
				intent.putExtra("minute", (Integer)clocks.get(position).get("minute"));
				intent.putExtra("mode", (Integer)clocks.get(position).get("mode"));
				startActivityForResult(intent, position);
			}
		});
		
		lvClocks.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
				// TODO Auto-generated method stub
				View dialogContent=getLayoutInflater().inflate(R.layout.dialog_delete, null);
				TextView tvTip=(TextView)dialogContent.findViewById(R.id.tv_tip);
				tvTip.setText(getString(R.string.delete_this_reminder));
				final AlertDialog dialog=new AlertDialog.Builder(ClockActivity.this).setView(dialogContent).create();
				dialog.show();
				Button btnCancel=(Button)dialogContent.findViewById(R.id.btn_cancel);
				btnCancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				
				Button btnOK=(Button)dialogContent.findViewById(R.id.btn_ok);
				btnOK.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						
						Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);						
						AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
						for(int i=0; i<7; i++){
							PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, position*7+i, clockIntent, 0);
							am.cancel(pi);
						}
						
						clocks.remove(position);
						try {
							String clocksString=list2String(clocks);
							getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", clocksString).commit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
						dialog.cancel();
					}
				});
				return true;
			}
		});
		
		adapter=new MyBaseAdapter();
		lvClocks.setAdapter(adapter);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode==1){
			//1000为新增闹钟
			if(requestCode==1000){
				HashMap<String, Object> clock=new HashMap<String, Object>();
				int hour=data.getIntExtra("hour", new Date().getHours());
				int minute=data.getIntExtra("minute", new Date().getMinutes());
				int mode=data.getIntExtra("mode", 0);
				boolean[] day=data.getBooleanArrayExtra("day");
				clock.put("hour", hour);
				clock.put("minute", minute);
				clock.put("mode", mode);
				clock.put("day", day);
				clock.put("status", true);				
				
				Calendar calendar=Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);
				AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
				if(mode==0){
					//oneshot模式下，判断是否小于当前时间，若是则将calendar推到明天，反之则直接设置calendar的时间
					if(calendar.getTimeInMillis()<System.currentTimeMillis()){
						calendar.add(Calendar.DATE, 1);						
					}
					PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, clocks.size()*7, clockIntent, 0);	
					am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
				}else{
					//非oneshot模式下，循环设置7天的闹钟，即一周为周期的闹钟，每个闹钟每天定时重复触发
					//即一个闹钟占了AlarmManager中的7个请求码
					//官方日历参数表示：星期日到星期六=1—7；
					//day[]参数表示：星期一到星期日=0—6；故需减2，小于0置6；
					int today=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
					if(today<0) today=6;
					
					//若果当前设置时间比现在时间少，则把今天的闹钟推到下一周设置，避免立即触发闹钟
					if(calendar.getTimeInMillis()<System.currentTimeMillis()){
						today++;
						if(today>6){
							today=0;
						}
						calendar.add(Calendar.DATE, 1);
					}
					
					//先遍历今天起后面的星期
					for(int i=today; i<day.length; i++){
						if(day[i]){
							PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, clocks.size()*7+i, clockIntent, 0);					
							am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
						}					
						calendar.add(Calendar.DATE, 1);
					}
					//将日历推到下周星期一,遍历今天前面的星期
					for(int i=0; i<today; i++){
						if(day[i]){
							PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, clocks.size()*7+i, clockIntent, 0);					
							am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
						}
						calendar.add(Calendar.DATE, 1);
					}
				}
							
				
				clocks.add(clock);
				try {
					String clocksString=list2String(clocks);
					getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", clocksString).commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.notifyDataSetChanged();
			}else{
				HashMap<String, Object> clock=clocks.get(requestCode);
				int hour=data.getIntExtra("hour", new Date().getHours());
				int minute=data.getIntExtra("minute", new Date().getMinutes());
				int mode=data.getIntExtra("mode", 0);
				boolean[] day=data.getBooleanArrayExtra("day");
				clock.put("hour", hour);
				clock.put("minute", minute);
				clock.put("mode", mode);
				clock.put("day", day);
				clock.put("status", true);
				
				Calendar calendar=Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);
				AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
				if(mode==0){
					//oneshot模式下，判断是否小于当前时间，若是则将calendar推到明天，反之则直接设置calendar的时间					
					if(calendar.getTimeInMillis()<System.currentTimeMillis()){	
						calendar.add(Calendar.DATE, 1);						
					}
					//由于是修改闹钟，需考虑mode从其他模式到oneshot的情况，即要清除其他6个闹钟
					for(int i=0; i<day.length; i++){
						PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, requestCode*7+i, clockIntent, 0);
						if(i==0){								
							am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
							continue;
						}							
						am.cancel(pi);
					}
				}else{
					int today=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
					if(today<0) today=6;
					
					//若果当前设置时间比现在时间少，则把今天的闹钟推到下一周设置，避免立即触发闹钟
					if(calendar.getTimeInMillis()<System.currentTimeMillis()){
						today++;
						if(today>6){
							today=0;
						}
						calendar.add(Calendar.DATE, 1);
					}
					
					//先遍历今天起后面的星期
					for(int i=today; i<day.length; i++){
						PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, requestCode*7+i, clockIntent, 0);	
						if(day[i]){											
							am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
						}else{
							am.cancel(pi);
						}
						calendar.add(Calendar.DATE, 1);
					}
					
					//将日历推到下周星期一,遍历今天前面的星期
					for(int i=0; i<today; i++){
						PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, requestCode*7+i, clockIntent, 0);
						if(day[i]){												
							am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
						}else{
							am.cancel(pi);
						}
						calendar.add(Calendar.DATE, 1);
					}
				}
				
				
				try {
					String clocksString=list2String(clocks);
					getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", clocksString).commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.notifyDataSetChanged();
			}
			
		}
	}
	
	private class MyBaseAdapter extends BaseAdapter{
		
		private class ViewHolder{
			private TextView tvTime;
			private TextView tvDay;
			private CheckBox cbToggle;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return clocks.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return clocks.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){
				holder=new ViewHolder();
				convertView=getLayoutInflater().inflate(R.layout.list_item_clock, null);
				holder.tvTime=(TextView)convertView.findViewById(R.id.tv_time);
				holder.tvDay=(TextView)convertView.findViewById(R.id.tv_day);
				holder.cbToggle=(CheckBox)convertView.findViewById(R.id.cb_toggle);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			final HashMap<String, Object> clock=clocks.get(position);
			DecimalFormat df=new DecimalFormat("00");
			holder.tvTime.setText(df.format(clock.get("hour"))+":"+df.format(clock.get("minute")));
			int mode=(Integer) clock.get("mode");
			if(mode==0){
				holder.tvDay.setText(getString(R.string.oneshot));
			}else if(mode==1){
				holder.tvDay.setText(getString(R.string.everyday));
			}else if(mode==2){
				holder.tvDay.setText(getString(R.string.weekday));
			}else{				
				boolean[] day=(boolean[]) clock.get("day");
				holder.tvDay.setText((day[0]?getString(R.string.Monday)+"  ":"")+(day[1]?getString(R.string.Tuesday)+"  ":"")
						+(day[2]?getString(R.string.Wednesday)+"  ":"")+(day[3]?getString(R.string.Thursday)+"  ":"")
						+(day[4]?getString(R.string.Friday)+"  ":"")+(day[5]?getString(R.string.Saturday)+"  ":"")
						+(day[6]?getString(R.string.Sunday)+"  ":""));
			}
			holder.cbToggle.setChecked((Boolean) clock.get("status"));
			holder.cbToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					// TODO Auto-generated method stub
					
					if(isChecked){
						HashMap<String, Object> clock=clocks.get(position);
						int hour=((Integer) clock.get("hour")).intValue();
						int minute=((Integer) clock.get("minute")).intValue();
						int mode=((Integer) clock.get("mode")).intValue();
						boolean[] day=(boolean[]) clock.get("day");
						
						Calendar calendar=Calendar.getInstance();
						calendar.set(Calendar.HOUR_OF_DAY, hour);
						calendar.set(Calendar.MINUTE, minute);
						calendar.set(Calendar.SECOND, 0);
						Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);
						AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
						if(mode==0){
							//oneshot模式下，判断是否小于当前时间，若是则将calendar推到明天，反之则直接设置calendar的时间					
							if(calendar.getTimeInMillis()<System.currentTimeMillis()){	
								calendar.add(Calendar.DATE, 1);						
							}
							//由于是修改闹钟，需考虑mode从其他模式到oneshot的情况，即要清除其他6个闹钟
							for(int i=0; i<day.length; i++){
								PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, position*7+i, clockIntent, 0);
								if(i==0){								
									am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
									continue;
								}							
								am.cancel(pi);
							}
						}else{
							int today=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
							if(today<0) today=6;
							
							//若果当前设置时间比现在时间少，则把今天的闹钟推到下一周设置，避免立即触发闹钟
							if(calendar.getTimeInMillis()<System.currentTimeMillis()){
								today++;
								if(today>6){
									today=0;
								}
								calendar.add(Calendar.DATE, 1);
							}
							
							//先遍历今天起后面的星期
							for(int i=today; i<day.length; i++){
								PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, position*7+i, clockIntent, 0);	
								if(day[i]){											
									am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
								}else{
									am.cancel(pi);
								}
								calendar.add(Calendar.DATE, 1);
							}
							
							//将日历推到下周星期一,遍历今天前面的星期
							for(int i=0; i<today; i++){
								PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, position*7+i, clockIntent, 0);
								if(day[i]){												
									am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000*7L, pi);						
								}else{
									am.cancel(pi);
								}
								calendar.add(Calendar.DATE, 1);
							}
						}
					}else{
						Intent clockIntent=new Intent(ClockActivity.this, AlarmActivity.class);						
						AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
						for(int i=0; i<7; i++){
							PendingIntent pi=PendingIntent.getActivity(ClockActivity.this, position*7+i, clockIntent, 0);
							am.cancel(pi);
						}							
					}
										
					clock.put("status", isChecked);
					try {
						String clocksString=list2String(clocks);
						getSharedPreferences("clocks", Context.MODE_PRIVATE).edit().putString("clocks", clocksString).commit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			return convertView;
		}
		
	}
	
	//list转String
	private String list2String(List<?> list) throws IOException{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(baos);
		oos.writeObject(list);
		String s=new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		oos.close();
		return s;	
	}
	
	//String转list
	private List<?> string2List(String s) throws StreamCorruptedException, IOException, ClassNotFoundException{
		byte[] bytes=Base64.decode(s.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
		ObjectInputStream ois=new ObjectInputStream(bais);
		List<?> list=(List<?>) ois.readObject();
		ois.close();
		return list;
	}

}
