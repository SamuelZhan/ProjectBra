package com.tokool.bra.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.tokool.bra.R;
import com.tokool.bra.service.BleService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicActivity extends Activity {
	
	private ImageView btnBack, ivArrow, ivSound, ivTrumpet;
	private TextView tvNumOfSongs;
	private CheckBox btnPlayOrStop;
	private LinearLayout btnHealthyRhythm, btnLocal;
	private SeekBar sbSound;
	private ListView lvSongs;
	private ArrayList<HashMap<String, Object>> songs;
	private MyBaseAdapter adapter;
	private AudioManager audioManager;
	private MediaPlayer mp;
	private Visualizer visualizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);	
		
		songs=new ArrayList<HashMap<String,Object>>();
		loadSongs();

		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnHealthyRhythm=(LinearLayout)findViewById(R.id.btn_healthy_music);
		btnHealthyRhythm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				ivTrumpet.setVisibility(View.VISIBLE);
				
				for(HashMap<String, Object> song : songs){
					song.put("isSelected", false);
				}
				adapter.notifyDataSetChanged();
				
				loadDefaultSong();
				visualizer.setEnabled(true);
				mp.start();
				btnPlayOrStop.setChecked(true);
				
			}
		});
		
		btnLocal=(LinearLayout)findViewById(R.id.btn_local_music);
		btnLocal.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		ivArrow=(ImageView)findViewById(R.id.iv_arrow_right);
		if(songs.size()>0){
			ivArrow.setRotationX(0.5f);
			ivArrow.setRotationY(0.5f);
			ivArrow.setRotation(90);
		}
		
		ivTrumpet=(ImageView)findViewById(R.id.iv_trumpet);
		
		
		ivSound=(ImageView)findViewById(R.id.iv_sound);
		
		audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);	
		sbSound=(SeekBar)findViewById(R.id.seekbar_sound);
		sbSound.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));	
		sbSound.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		sbSound.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar sb) {
				// TODO Auto-generated method stub
				if(sbSound.getProgress()==0){
					ivSound.setImageResource(R.drawable.music_sound_silent);
				}else{
					ivSound.setImageResource(R.drawable.music_sound_normal);
				}
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sb.getProgress(), AudioManager.FLAG_PLAY_SOUND);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				
			}
		});
		
		btnPlayOrStop=(CheckBox)findViewById(R.id.btn_play_or_stop);
		btnPlayOrStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					if(mp!=null){
						mp.start();
					}	
					if(visualizer!=null){
						visualizer.setEnabled(true);
					}
					
				}else{		
					if(mp!=null && mp.isPlaying()){
						mp.pause();
					}					
					if(visualizer!=null){
						visualizer.setEnabled(false);
					}
					BleService.isMassaging=false;
					sendBroadcast(new Intent().setAction("stop_massage"));
				}
			}
		});
		
		tvNumOfSongs=(TextView)findViewById(R.id.tv_numOfSongs);
		tvNumOfSongs.setText(songs.size()+getString(R.string.songs));
		
		lvSongs=(ListView)findViewById(R.id.lv_songs);
		adapter=new MyBaseAdapter();
		lvSongs.setAdapter(adapter);
		lvSongs.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				
				//在列表里用喇叭标记为选中
				for(HashMap<String, Object> song : songs){
					song.put("isSelected", false);
				}
				songs.get(position).put("isSelected", true);
				adapter.notifyDataSetChanged();
				
				ivTrumpet.setVisibility(View.INVISIBLE);
				
				if(mp!=null){
					mp.stop();
					mp.release();
					mp=null;
				}
				
				if(mp!=null){
					mp.release();
					mp=null;
				}
				if(visualizer!=null){
					visualizer.setEnabled(false);
				}				
				
				String path=(String) songs.get(position).get("path");				
				File f=new File(path);
				Uri uri=Uri.fromFile(f);				
				mp=MediaPlayer.create(MusicActivity.this, uri);
				mp.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer arg0) {
						// TODO Auto-generated method stub
						visualizer.setEnabled(false);
						btnPlayOrStop.setChecked(false);

					}
				});
				visualizer=new Visualizer(mp.getAudioSessionId());
				visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
				visualizer.setDataCaptureListener(new OnDataCaptureListener() {
					
					@Override
					public void onWaveFormDataCapture(Visualizer arg0, byte[] waveform, int samplingRate) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onFftDataCapture(Visualizer arg0, byte[] fft, int samplingRate) {
						// TODO Auto-generated method stub
						byte[] model = new byte[fft.length / 2 + 1];  
						
						StringBuffer sb=new StringBuffer();
						model[0] = (byte) Math.abs(fft[0]);  
						sb.append(Byte.toString(model[0])+"-");
						for (int i = 2, j = 1; j < model.length-1; j++){  
							model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
							i += 2;  
							sb.append(Byte.toString(model[j])+"-");
						} 
						Log.d("zz", sb.toString());
						int strength=(int)(model[2]/180f*100);
						if(strength==0){
							sendBroadcast(new Intent().setAction("stop_massage"));
						}else{
							Intent intent=new Intent("music_massage");
							intent.putExtra("strength", strength);
							sendBroadcast(intent);
						}						
					}
				}, Visualizer.getMaxCaptureRate()/4, false, true);
				visualizer.setEnabled(true);
				mp.start();
				btnPlayOrStop.setChecked(true);
			}
		});
		
		loadDefaultSong();
		
	}
	
	//加载本地曲目
	private void loadSongs(){
		Cursor c=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);		
		while(c.moveToNext()){
			HashMap<String, Object> song=new HashMap<String, Object>();			
			song.put("path", c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			song.put("title", c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
			song.put("album", c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			song.put("artist", c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			song.put("isSelected", false);
			songs.add(song);
		}
		c.close();
	}
	
	//加载默认曲目
	private void loadDefaultSong(){
		if(mp!=null){
			mp.release();
			mp=null;
		}
		mp=MediaPlayer.create(MusicActivity.this, R.raw.demo);
		mp.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				btnPlayOrStop.setChecked(false);
			}
		});
		visualizer=new Visualizer(mp.getAudioSessionId());
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
		visualizer.setDataCaptureListener(new OnDataCaptureListener() {
			
			@Override
			public void onWaveFormDataCapture(Visualizer arg0, byte[] waveform, int samplingRate) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFftDataCapture(Visualizer arg0, byte[] fft, int samplingRate) {
				// TODO Auto-generated method stub
				byte[] model = new byte[fft.length / 2 + 1];  
				
				StringBuffer sb=new StringBuffer();
				model[0] = (byte) Math.abs(fft[0]);  
				sb.append(Byte.toString(model[0])+"-");
				for (int i = 2, j = 1; j < model.length-1; j++){  
					model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
					i += 2;  
					sb.append(Byte.toString(model[j])+"-");
				}  
				Log.d("zz", sb.toString()); 
				int strength=(int)(model[2]/180f*100);
				if(strength==0){
					sendBroadcast(new Intent().setAction("stop_massage"));
				}else{
					Intent intent=new Intent("music_massage");
					intent.putExtra("strength", strength);
					sendBroadcast(intent);
				}						
			}
		}, Visualizer.getMaxCaptureRate()/4, false, true);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		BleService.isMassaging=false;
		sendBroadcast(new Intent().setAction("stop_massage"));
		if(mp!=null){
			mp.release();
			mp=null;
		}
		if(visualizer!=null){
			visualizer.setEnabled(false);
		}
		super.onDestroy();
	}

	private class MyBaseAdapter extends BaseAdapter{
		
		private class ViewHolder{
			TextView tvTitle, tvInfo;
			ImageView ivTrumpet;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return songs.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return songs.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView!=null){
				holder=(ViewHolder) convertView.getTag();
			}else{
				holder=new ViewHolder();
				convertView=getLayoutInflater().inflate(R.layout.list_item_songs, null);
				holder.tvTitle=(TextView)convertView.findViewById(R.id.tv_song_title);
				holder.tvInfo=(TextView)convertView.findViewById(R.id.tv_song_info);
				holder.ivTrumpet=(ImageView)convertView.findViewById(R.id.iv_trumpet);
				convertView.setTag(holder);
			}
			String title = (String)songs.get(position).get("title");
			holder.tvTitle.setText(title);
			
			String infoAlbum=(String)songs.get(position).get("album");
			if(infoAlbum==null){
				infoAlbum="未知专辑";
			}
			String infoArtist=(String)songs.get(position).get("artist");
			if(infoArtist==null){
				infoArtist="未知艺术家";
			}			
			holder.tvInfo.setText(infoAlbum+"--"+infoArtist);
			
			if((Boolean)songs.get(position).get("isSelected")){
				holder.ivTrumpet.setVisibility(View.VISIBLE);
			}else{
				holder.ivTrumpet.setVisibility(View.INVISIBLE);
			}
			
			return convertView;
		}
		
	}

}
