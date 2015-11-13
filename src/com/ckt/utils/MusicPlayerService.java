package com.ckt.utils;

import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;

/**
 * @author admin
 *
 */
public class MusicPlayerService extends Service {
	
	private ArrayList<Mp3Info> musicList;  //音乐列表
	private MediaPlayer mPlayer = new MediaPlayer();
	private int playerState = 0; //0----->停止状态  1---->播放状态  2----->暂停状态
	
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();
	public class MusicPlayerBinder extends Binder{
		public MediaPlayer getMusicPlayer(){
			return mPlayer;
		}
		public ArrayList<Mp3Info> getMusicList() {
			return musicList;
		}
		public int getPlayerState() {
			return playerState;
		}
		public void setPlayerState(int state) {
			playerState = state;
		}
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onBind");
		System.out.println("MusicPlayerService--->onBind");
		return mBinder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onReBind");
		System.out.println("MusicPlayerService--->onReBind");
		super.onRebind(intent);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		//Notification:待修改
		/*Notification notification = new Notification(
				R.id.CD_img,"Test",System.currentTimeMillis()){};
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "酷狗音乐", "Hello 酷狗!", pendingIntent);
		startForeground(1, notification);*/
		
		//读取音乐列表:
		//读取音乐列表---->需要权限:<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		//其实这里应该开个线程的
		this.musicList = Mp3FileUtil.getMp3InfoList(this);
		System.out.println("音乐个数:"+this.musicList.size());
		
		//注册通知栏点击事件的广播接收器
		IntentFilter filter = new IntentFilter(NotificationUtils.Broadcast_INTENT_ACTION);
		registerReceiver(new NotificationClickReciver(), filter);
		
		//显示通知栏
		NotificationUtils.show(getApplicationContext(), false, this.musicList.get(0));
		
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
		NotificationUtils.close(getApplicationContext());  //关闭通知栏
		super.onDestroy();
	}
	
	
	/**通知栏点击事件的广播接收器
	 * @author JonsonMarxy
	 *
	 */
	class NotificationClickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int action = intent.getIntExtra(NotificationUtils.Broadcast_INTENT_EXTRA_DATA, -1);
			LogUtil.v("MusicPlayerService", "通知栏点击了:"+action);
			switch (action) {
			case NotificationUtils.NOTIFICATION_LAST:  //上一首
				
				break;
			case NotificationUtils.NOTIFICATION_PLAY_OR_PAUSE:  //暂停或者播放
				
				break;
			case NotificationUtils.NOTIFICATION_NEXT:  //下一首
				
				break;
			case NotificationUtils.NOTIFICATION_CANEL:  //点击了通知栏上面的叉叉(X)
				
				break;
			default:
				break;
			}
		}
		
	}
}
