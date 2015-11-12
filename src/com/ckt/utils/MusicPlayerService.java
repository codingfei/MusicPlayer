package com.ckt.utils;

import com.ckt.modle.LogUtil;
import com.ckt.musicplayer.MainActivity;
import com.ckt.musicplayer.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicPlayerService extends Service {

	
	private MediaPlayer mPlayer = new MediaPlayer();
	
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();
	public class MusicPlayerBinder extends Binder{
		public MediaPlayer getMusicPlayer(){
			return mPlayer;
		}
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onBind");
		return mBinder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onReBind");
		super.onRebind(intent);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		//Notification:´ýÐÞ¸Ä
		Notification notification = new Notification(
				R.id.CD_img,"Test",System.currentTimeMillis()){};
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "¿á¹·ÒôÀÖ", "Hello ¿á¹·!", pendingIntent);
		startForeground(1, notification);
		
		LogUtil.v("MusicPlayerService", "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v("MusicPlayerService", "onStartCommand");
	
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		LogUtil.v("MusicPlayerService", "onDestory");
		super.onDestroy();
	}
}
