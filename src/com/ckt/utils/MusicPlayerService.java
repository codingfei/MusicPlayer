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
	
	private ArrayList<Mp3Info> musicList;  //�����б�
	private MediaPlayer mPlayer = new MediaPlayer();
	private int playerState = 0; //0----->ֹͣ״̬  1---->����״̬  2----->��ͣ״̬
	
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
		//Notification:���޸�
		/*Notification notification = new Notification(
				R.id.CD_img,"Test",System.currentTimeMillis()){};
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "�ṷ����", "Hello �ṷ!", pendingIntent);
		startForeground(1, notification);*/
		
		//��ȡ�����б�:
		//��ȡ�����б�---->��ҪȨ��:<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		//��ʵ����Ӧ�ÿ����̵߳�
		this.musicList = Mp3FileUtil.getMp3InfoList(this);
		System.out.println("���ָ���:"+this.musicList.size());
		
		//ע��֪ͨ������¼��Ĺ㲥������
		IntentFilter filter = new IntentFilter(NotificationUtils.Broadcast_INTENT_ACTION);
		registerReceiver(new NotificationClickReciver(), filter);
		
		//��ʾ֪ͨ��
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
		NotificationUtils.close(getApplicationContext());  //�ر�֪ͨ��
		super.onDestroy();
	}
	
	
	/**֪ͨ������¼��Ĺ㲥������
	 * @author JonsonMarxy
	 *
	 */
	class NotificationClickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int action = intent.getIntExtra(NotificationUtils.Broadcast_INTENT_EXTRA_DATA, -1);
			LogUtil.v("MusicPlayerService", "֪ͨ�������:"+action);
			switch (action) {
			case NotificationUtils.NOTIFICATION_LAST:  //��һ��
				
				break;
			case NotificationUtils.NOTIFICATION_PLAY_OR_PAUSE:  //��ͣ���߲���
				
				break;
			case NotificationUtils.NOTIFICATION_NEXT:  //��һ��
				
				break;
			case NotificationUtils.NOTIFICATION_CANEL:  //�����֪ͨ������Ĳ��(X)
				
				break;
			default:
				break;
			}
		}
		
	}
}
