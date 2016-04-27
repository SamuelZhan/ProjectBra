package com.tokool.bra.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.tokool.bra.R;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BleService extends Service {
	
	private UUID serviceUUID=UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	private UUID characteristicWriteUUID=UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	private UUID characteristicReadUUID=UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	private UUID descriptorUUID=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private BluetoothGatt btGattLeft, btGattRight;
	private BluetoothGattCharacteristic characteristicLeft, characteristicRight;
	
	private Handler handler;
	private MyBroadcastReceiver receiver;
	private Timer timer;
	
	public static boolean isLeftConnected, isRightConnected;
	public static String leftAddress, rightAddress;
	private boolean isDiscovering=false;
	
	private final int LEFT=1;
	private final int BOTH=2;
	private final int RIGHT=3;
	//电量
	public static int electricityLeft, electricityRight;
	
	//按摩
	public static int mode=6;
	public static int which=2;
	public static int time=10;
	public static int strength=30;
	public static boolean isMassaging;
	private boolean isLeftMassaging;
	private boolean isRightMassaging;
	
	//计步
	public static int steps;
	public static float calorie;
	public static int distance;
	private StringBuffer dataString;
	private String dateString;
	private boolean isInitSteps=false;
	private int lastStepsBLE;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		//设置一个第二天的闹钟，使APP运行期间跨过0点时能重置数据，避免叠加
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, 1);
		Intent intent=new Intent("time_to_24_hour");
		PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000L, pendingIntent);
		
		SharedPreferences preferences=getSharedPreferences("stepInfo", Context.MODE_PRIVATE);
		steps=preferences.getInt("steps", 0);
		calorie=preferences.getFloat("calorie", 0f);
		distance=preferences.getInt("distance", 0);
		dateString=preferences.getString("date", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		
		//这里判断是否到了第二天，若到了第二天，重置todaySteps和time,并把昨天的步数录入本地数据库
		String newDateString=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		if(!newDateString.equals(dataString)){
			steps=0;
			calorie=0f;
			distance=0;
			dateString=newDateString;
			
		}
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("request_connect");
		filter.addAction("request_disconnect");
		filter.addAction("start_massage");
		filter.addAction("music_massage");
		filter.addAction("stop_massage");
		filter.addAction("stop_left_massage");
		filter.addAction("stop_right_massage");
		filter.addAction("query_massage_status");
		filter.addAction("start_count");
		filter.addAction("get_history_steps");
		filter.addAction("alarm_time_up");
		filter.addAction("synchronize_time");
		filter.addAction("query_electricity");
		filter.addAction("time_to_24_hour");
		registerReceiver(receiver, filter);
		
		handler=new Handler();
		
		timer=new Timer();		
		timer.schedule(new ConnectionDetector(), 0, 10000);
//		timer.schedule(new MassagingStatus(), 0, 5000);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(btGattLeft!=null){
			
			btGattLeft.disconnect();
			btGattLeft.close();
			btGattLeft=null;
		}
		
		if(btGattRight!=null){
			btGattRight.disconnect();
			btGattRight.close();
			btGattRight=null;
		}
		isLeftConnected=false;
		isRightConnected=false;
		
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
		
		unregisterReceiver(receiver);
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		leftAddress=getSharedPreferences("connection", Context.MODE_PRIVATE).getString("left_address", "unbound");
		rightAddress=getSharedPreferences("connection", Context.MODE_PRIVATE).getString("right_address", "unbound");
		if(!leftAddress.equals("unbound")){
			Intent intent1=new Intent();
			intent1.setAction("request_connect");
			intent1.putExtra("address", leftAddress);
			sendBroadcast(intent1);
		}
		if(!rightAddress.equals("unbound")){
			Intent intent1=new Intent();
			intent1.setAction("request_connect");
			intent1.putExtra("address", rightAddress);
			sendBroadcast(intent1);
		}
		return super.onStartCommand(intent, flags, startId);
		
		
	}
	
	
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();			
			if(action.equals("request_connect")){
					final BluetoothDevice device=((BluetoothManager)getSystemService(BLUETOOTH_SERVICE))
							.getAdapter().getRemoteDevice(intent.getStringExtra("address"));
					
					if(device.getName()!=null){
						if(device.getName().endsWith("L")){
							if(btGattLeft!=null){
								btGattLeft.disconnect();
								btGattLeft.close();
								btGattLeft=null;
							}
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									btGattLeft=device.connectGatt(BleService.this, true, mBluetoothGattCallback);
								}
							}, 1000);
						}else if(device.getName().endsWith("R")){
							if(btGattRight!=null){
								btGattRight.disconnect();
								btGattRight.close();
								btGattRight=null;
							}
							//断开和重连之间必须得有时间间隔，不然会出现连接后又秒断的现象
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									btGattRight=device.connectGatt(BleService.this, true, mBluetoothGattCallback);
								}
							}, 1000);
							
						}
						
					}									
				
			}
			if(action.equals("request_disconnect")){
				if(intent.getStringExtra("which").equals("L")){
					if(btGattLeft!=null){
						btGattLeft.disconnect();
						btGattLeft.close();
						btGattLeft=null;
					}
					isLeftConnected=false;
					electricityLeft=0;
					leftAddress="unbound";
					getSharedPreferences("connection", Context.MODE_PRIVATE).edit().putString("left_address", leftAddress).commit();
					sendBroadcast(new Intent("device_disconnected"));
				}else{
					if(btGattRight!=null){
						btGattRight.disconnect();
						btGattRight.close();
						btGattRight=null;
					}
					isRightConnected=false;
					electricityRight=0;
					rightAddress="unbound";
					getSharedPreferences("connection", Context.MODE_PRIVATE).edit().putString("right_address", rightAddress).commit();
					sendBroadcast(new Intent("device_disconnected"));
				}
			}
			if(action.equals("query_electricity")){			
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x13;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x00;
				writeCommand(bytes, BOTH);
				
			}
			if(action.equals("start_massage")){	
				isLeftMassaging=true;
				isRightMassaging=true;
				if(mode==6){
					if(time<5 && time!=0){
						byte[] bytes=new byte[6+time];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x62;
						bytes[2]=(byte)0xc0;
						bytes[3]=(byte)(2+time);
						bytes[4]=(byte)0x01;
						bytes[5]=(byte)strength;
						for(int i=0; i<time; i++){
							bytes[6+i]=(byte)(i+1);
						}
						writeCommand(bytes, which);
					}else{
						byte[] bytes=new byte[11];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x62;
						bytes[2]=(byte)0xc0;
						bytes[3]=(byte)0x07;
						bytes[4]=(byte)(time/5);
						bytes[5]=(byte)strength;
						bytes[6]=(byte)0x01;
						bytes[7]=(byte)0x02;
						bytes[8]=(byte)0x03;
						bytes[9]=(byte)0x04;
						bytes[10]=(byte)0x05;
						writeCommand(bytes, which);
					}					
					
					if(which==1){
						isRightMassaging=false;
						byte[] stopBytes=new byte[4];
						stopBytes[0]=(byte)0x00;
						stopBytes[1]=(byte)0x62;
						stopBytes[2]=(byte)0x01;
						stopBytes[3]=(byte)0xc0;
						writeCommand(stopBytes, RIGHT);
					}else if(which==3){
						isLeftMassaging=false;
						byte[] stopBytes=new byte[4];
						stopBytes[0]=(byte)0x00;
						stopBytes[1]=(byte)0x62;
						stopBytes[2]=(byte)0x01;
						stopBytes[3]=(byte)0xc0;
						writeCommand(stopBytes, LEFT);
					}
				}else{
					byte[] bytes=new byte[7];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x62;
					bytes[2]=(byte)0xc0;
					bytes[3]=(byte)0x03;
					bytes[4]=(byte)time;
					bytes[5]=(byte)strength;
					bytes[6]=(byte)mode;
					writeCommand(bytes, which);
					if(which==1){
						isRightMassaging=false;
						byte[] stopBytes=new byte[4];
						stopBytes[0]=(byte)0x00;
						stopBytes[1]=(byte)0x62;
						stopBytes[2]=(byte)0x01;
						stopBytes[3]=(byte)0xc0;
						writeCommand(stopBytes, RIGHT);
					}else if(which==3){
						isLeftMassaging=false;
						byte[] stopBytes=new byte[4];
						stopBytes[0]=(byte)0x00;
						stopBytes[1]=(byte)0x62;
						stopBytes[2]=(byte)0x01;
						stopBytes[3]=(byte)0xc0;
						writeCommand(stopBytes, LEFT);
					}
				}											
			}
			if(action.equals("stop_massage")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x62;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0xc0;
				writeCommand(bytes, BOTH);
			}
			if(action.equals("stop_left_massage")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x62;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0xc0;
				writeCommand(bytes, LEFT);
				isLeftMassaging=false;
				isMassaging=isLeftMassaging|isRightMassaging;
			}
			if(action.equals("stop_right_massage")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x62;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0xc0;
				writeCommand(bytes, RIGHT);
				isRightMassaging=false;
				isMassaging=isLeftMassaging|isRightMassaging;
			}
			if(action.equals("music_massage")){
				int strength=intent.getIntExtra("strength", 0);
				byte[] bytes=new byte[7];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x62;
				bytes[2]=(byte)0xc0;
				bytes[3]=(byte)0x03;
				bytes[4]=(byte)0x01;
				bytes[5]=(byte)strength;
				bytes[6]=(byte)0x01;
				writeCommand(bytes, BOTH);
			}
			if(action.equals("query_massage_status")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x61;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x00;				
				writeCommand(bytes, BOTH);			
			}
			//获取实时计步
			if(action.equals("start_count")){										
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x51;
				bytes[2]=(byte)0x01;	
				bytes[3]=(byte)0x00;
				writeCommand(bytes, BOTH);				
			}
			//获取历史计步
			if(action.equals("get_history_steps")){
				if(dataString==null){
					dataString=new StringBuffer();
				}else{
					dataString.delete(0, dataString.length());
				}
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x52;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes, BOTH);
			}
			//闹钟到时间触发震动
			if(action.equals("alarm_time_up")){
				byte[] bytes=new byte[7];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x62;
				bytes[2]=(byte)0xc0;
				bytes[3]=(byte)0x03;
				bytes[4]=(byte)0x01;
				bytes[5]=(byte)0x32;
				bytes[6]=(byte)0x01;
				writeCommand(bytes, BOTH);
			}
			//将手机时间同步到设备上
			if(action.equals("synchronize_time")){
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				long timeBefore=0;
				try {
					timeBefore=format.parse("2000-1-1").getTime();	//1970-1-1到2000-1-1的毫秒数				
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int time=(int)((new Date().getTime()-timeBefore)/1000); //2000-1-1到现在的秒数
				byte[] bytes=new byte[8];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x21;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x04;
				bytes[4]=(byte)((time>>24)&0xff);
				bytes[5]=(byte)((time>>16)&0xff);
				bytes[6]=(byte)((time>>8)&0xff);
				bytes[7]=(byte)(time&0xff);
				writeCommand(bytes, BOTH);
													
			}
			if(action.equals("time_to_24_hour")){
				steps=0;
				calorie=0f;
				distance=0;
				dateString=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			}
		}
		
	}
	
	//往设备写命令
	private void writeCommand(byte[] bytes, int flag){
		if(flag==LEFT){
			if(btGattLeft!=null && characteristicLeft!=null){							
				characteristicLeft.setValue(bytes);					
				btGattLeft.writeCharacteristic(characteristicLeft);
			}
		}
		if(flag==RIGHT){
			if(btGattRight!=null && characteristicRight!=null){
				characteristicRight.setValue(bytes);					
				btGattRight.writeCharacteristic(characteristicRight);
			}
		}
		if(flag==BOTH){
			if(btGattLeft!=null && characteristicLeft!=null){							
				characteristicLeft.setValue(bytes);					
				btGattLeft.writeCharacteristic(characteristicLeft);
			}
			if(btGattRight!=null && characteristicRight!=null){
				characteristicRight.setValue(bytes);					
				btGattRight.writeCharacteristic(characteristicRight);
			}
		}

	}
	
	private BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback(){

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			// TODO Auto-generated method stub
			if(newState==BluetoothGatt.STATE_CONNECTED){
				
				String name=gatt.getDevice().getName();
				String address=gatt.getDevice().getAddress();
				Intent intent=new Intent();
				intent.setAction("device_connected");
				intent.putExtra("name", name);
				intent.putExtra("address", address);
				sendBroadcast(intent);
				
				if(name.endsWith("L")){
					isLeftConnected=true;
					leftAddress=address;
					getSharedPreferences("connection", Context.MODE_PRIVATE).edit().putString("left_address", address).commit();

				}else{
					isRightConnected=true;
					rightAddress=address;
					isInitSteps=false;
					getSharedPreferences("connection", Context.MODE_PRIVATE).edit().putString("right_address", address).commit();					
					
				}
//				gatt.discoverServices();
				handler.post(new Discovering(gatt));
				
			}else if(newState==BluetoothGatt.STATE_DISCONNECTED){
				String name=gatt.getDevice().getName();
				if(name.endsWith("L")){
					isLeftConnected=false;
					electricityLeft=0;
					leftAddress="unbound";
				}else{
					isRightConnected=false;
					electricityRight=0;
					rightAddress="unbound";
					isInitSteps=false;
				}
				sendBroadcast(new Intent("device_disconnected"));			
				
			}
		}
		
		

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			
			BluetoothGattService gattService=gatt.getService(serviceUUID);
			if(gatt.getDevice().getName().endsWith("L")){
				characteristicLeft=gattService.getCharacteristic(characteristicWriteUUID);
				gatt.setCharacteristicNotification(characteristicLeft, true);
			}else{
				characteristicRight=gattService.getCharacteristic(characteristicWriteUUID);
				gatt.setCharacteristicNotification(characteristicRight, true);
			}			
			BluetoothGattCharacteristic gattCharacteristicRead=gattService.getCharacteristic(characteristicReadUUID);
			gatt.setCharacteristicNotification(gattCharacteristicRead, true);
			BluetoothGattDescriptor gattDescriptor=gattCharacteristicRead.getDescriptor(descriptorUUID);
			gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			gatt.writeDescriptor(gattDescriptor);
			
			isDiscovering=false;
			
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					sendBroadcast(new Intent("synchronize_time"));
					sendBroadcast(new Intent("start_count"));
					sendBroadcast(new Intent("query_massage_status"));
					sendBroadcast(new Intent("query_electricity"));
				}
			}, 500);
		}



		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			byte[] bytes=characteristic.getValue();
			String response=bytes2HexString(bytes);
			if(response.substring(0, 8).equals("b0510204")){
				
				SharedPreferences preferences=getSharedPreferences("userInfo", Context.MODE_PRIVATE);
				int stepLength=preferences.getInt("stepLength", 65);
				int weight=preferences.getInt("weight", 65);
				
				if(isInitSteps){				
					lastStepsBLE=Integer.valueOf(response.substring(8), 16);
					//若硬件上的步数大于APP，则表示硬件没断电，取其新增步数；反之，硬件断电，把期间的数据全加
					if(lastStepsBLE<steps){
						distance+=stepLength*lastStepsBLE/100;
						steps+=lastStepsBLE;						
						calorie+=weight*(stepLength*lastStepsBLE/100)/1000f*1.036f;
					}else{
						distance+=stepLength*(lastStepsBLE-steps)/100;
						steps+=lastStepsBLE-steps;						
						calorie+=weight*(stepLength*(lastStepsBLE-steps)/100)/1000f*1.036f;
					}
					SharedPreferences.Editor editor=preferences.edit();
					editor.putInt("steps", steps);
					editor.putFloat("calorie", calorie);
					editor.putInt("distance", distance);
					editor.putString("date", dateString);
					editor.commit();
					isInitSteps=true;
					return;
				}
								
				int currentStepsBLE=Integer.valueOf(response.substring(8), 16);
				int deltaSteps=currentStepsBLE-lastStepsBLE;
				if(deltaSteps<0){
					return;
				}else{
					steps+=deltaSteps;
					distance+=stepLength*(deltaSteps)/100;
					calorie+=weight*(stepLength*(deltaSteps)/100)/1000f*1.036f;
					SharedPreferences.Editor editor=preferences.edit();
					editor.putInt("steps", steps);
					editor.putFloat("calorie", calorie);
					editor.putInt("distance", distance);
					editor.putString("date", dateString);
					editor.commit();
					lastStepsBLE=currentStepsBLE;
					
					sendBroadcast(new Intent("update_steps"));
				}
				
			}
			//按摩工作状态
			if(response.substring(0, 4).equals("b061")){	
				if(response.equals("b0610000")){					
					
				}else if(response.length()==18){
					isMassaging=true;
					mode=Integer.valueOf(response.substring(16, 18), 16)-1;
					time=Integer.valueOf(response.substring(10, 12), 16);
					strength=Integer.valueOf(response.substring(8, 10), 16);
				}else if(response.length()>18){
					isMassaging=true;
					mode=6;
					time=Integer.valueOf(response.substring(10, 12), 16);
					strength=Integer.valueOf(response.substring(8, 10), 16);
				}
				
			}
			//右按摩停止
			if(response.equals("b0614000")){
				isRightMassaging=false;
				isMassaging=isLeftMassaging||isRightMassaging; 
				sendBroadcast(new Intent("update_massage_status"));
			}
			//左按摩停止
			if(response.equals("b0618000")){
				isLeftMassaging=false;
				isMassaging=isLeftMassaging||isRightMassaging; 
				sendBroadcast(new Intent("update_massage_status"));
			}
			//获取计步历史数据	
			if(response.substring(0, 4).equals("b052")){
				Log.d("zz", "history:"+response);
				dataString.append(response.substring(8));									
			}
			//计步历史数据传输完毕（同步成功）
			if(response.substring(0, 8).equals("b0530200")){
				Log.d("zz", "同步完毕:"+response);
				String[] historyData=new String[dataString.length()/48];
				for(int i=0; i<dataString.length()/48; i++){
					historyData[i]=dataString.substring(i*48, (i+1)*48);
				}
				Intent intent=new Intent("history_steps");
				intent.putExtra("history_steps", historyData);
				
				sendBroadcast(intent);
				
			}
			//电量
			if(response.substring(0, 8).equals("b0130101")){
				if(gatt.getDevice().getName().endsWith("L")){
					electricityLeft=Integer.parseInt(response.substring(8, 10), 16);
				}else{
					electricityRight=Integer.parseInt(response.substring(8, 10), 16);
				}
				sendBroadcast(new Intent().setAction("update_electricity"));
			}
		}
		
		
	};

	
	//byte转16进制字符串
	public String bytes2HexString(byte[] bytes){
		String hexString="";
		String temp="";
		for(int i=0; i<bytes.length; i++){
			temp=(Integer.toHexString(bytes[i] & 0xFF));
			if(temp.length() == 1){
				hexString=hexString+"0"+temp;
			}else{
				hexString=hexString+temp;
			}
		}	
		return hexString.toLowerCase();
	}
	
	private class Discovering implements Runnable{
		
		private BluetoothGatt gatt;
		private int count;
		
		public Discovering(BluetoothGatt gatt) {
			// TODO Auto-generated constructor stub
			this.gatt=gatt;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if(!isDiscovering){
				gatt.discoverServices();
				isDiscovering=true;
			}else{
				handler.postDelayed(this, 100);
				count++;
				if(count==10){
					count=0;
					isDiscovering=false;
				}
			}
		}
		
	}
	
	//用于检测蓝牙的实时连接状态，防止异常断开又没有触发onConnectionStateChange回调造成UI没有及时更新的情况
	public class ConnectionDetector extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sendBroadcast(new Intent("query_electricity"));
			
			BluetoothManager btManager=(BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			if(btGattLeft!=null){
				BluetoothDevice device=btGattLeft.getDevice();
				if(device!=null){
					int state=btManager.getConnectionState(device, BluetoothGatt.GATT);
					if(state==BluetoothGatt.STATE_DISCONNECTED){
						isLeftConnected=false;
						electricityLeft=0;
						leftAddress="unbound";
						sendBroadcast(new Intent("device_disconnected"));
						btGattLeft.disconnect();
					}
				}else{
					isLeftConnected=false;
					electricityLeft=0;
					leftAddress="unbound";
					sendBroadcast(new Intent("device_disconnected"));
					btGattLeft.disconnect();
				}
			}else{
				isLeftConnected=false;
				electricityLeft=0;
				leftAddress="unbound";
				sendBroadcast(new Intent("device_disconnected"));
			}
			if(btGattRight!=null){								
				BluetoothDevice device=btGattRight.getDevice();
				if(device!=null){
					int state=btManager.getConnectionState(device, BluetoothGatt.GATT);
					if(state==BluetoothGatt.STATE_DISCONNECTED){
						isRightConnected=false;
						electricityRight=0;
						rightAddress="unbound";
						sendBroadcast(new Intent("device_disconnected"));
						btGattRight.disconnect();
					}
				}else{
					isRightConnected=false;
					electricityRight=0;
					rightAddress="unbound";
					sendBroadcast(new Intent("device_disconnected"));
					btGattRight.disconnect();
				}
			}else{
				isRightConnected=false;
				electricityRight=0;
				rightAddress="unbound";
				sendBroadcast(new Intent("device_disconnected"));
			}
			
		}
		
	}
	
	//每分钟检测按摩的状态
	public class MassagingStatus extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sendBroadcast(new Intent("query_massage_status"));
		}
		
	}
	
	//电量检测
	public class ElectricityQuery extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startActivity(new Intent());
		}
		
	}
	
	
}
