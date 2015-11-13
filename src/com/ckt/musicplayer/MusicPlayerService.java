package com.ckt.musicplayer;

import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;
import com.ckt.musicplayer.R;
import com.ckt.utils.Mp3FileUtil;

public class MusicPlayerService extends Service {

	private MediaPlayer mPlayer = new MediaPlayer();

	private boolean musicIsPrepared = false;
	
	private Mp3Info mp3Current = null;
	
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();

//	OnPlayBroadcastReciver onPlayBroadcastReciver;
	// ����ģʽ
	public static final int PLAYORDER_ORDER = 1;
	public static final int PLAYORDER_LOOP = 2;
	public static final int PLAYORDER_RANDOM = 3;
	public static final int PLAYORDER = PLAYORDER_ORDER;

	private List<Mp3Info> mp3InfoList;
	
	public class MusicPlayerBinder extends Binder {

		// ��ȡ���ֲ�����
		public MediaPlayer getMusicPlayer() {
			return mPlayer;
		}
		//��ȡ�б�
		public List<Mp3Info> getMusicList() {
			return mp3InfoList;
		}

		public void playPreviousMusic(int playOrder){
			//�����ڵ�ǰ����ģʽ��
			if (playOrder == 0) {
				playOrder = PLAYORDER;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//��ȡ��ǰģʽ����һ�׸����Ϣ
				Mp3Info mp3Info =getPreviousMusicByOrder();
				if (mp3Info == null) {
					//��һ�׸�--��ͣ����
					Toast.makeText(MusicPlayerService.this, 
							"This is the First Song!", Toast.LENGTH_SHORT).show();
					LogUtil.v("MusicPlayerService", "onNext NULL");
					
				}else{
					musicIsPrepared =false;
					LogUtil.v("MusicPlayerService", "path:"+mp3Info.getPath());
					playOrPauseMusic(mPlayer, mp3Info);
					LogUtil.v("MusicPlayerService", "onNext Not NULL");
					
				}
				break;
			case PLAYORDER_LOOP:
				
				break;
			case PLAYORDER_RANDOM:
				
				break;
			default:
				break;
			}
		}
		//��һ��
		public void playNextMusic(int playOrder){
			//�����ڵ�ǰ����ģʽ��
			if (playOrder == 0) {
				playOrder = PLAYORDER;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//��ȡ��ǰģʽ����һ�׸����Ϣ
				Mp3Info mp3Info =getNextMusicByOrder();
				if (mp3Info == null) {
					//���һ�׸�--��ͣ����
					Toast.makeText(MusicPlayerService.this, 
							"This is the Last Song!", Toast.LENGTH_SHORT).show();
					LogUtil.v("MusicPlayerService", "onNext NULL");
					
				}else{
					musicIsPrepared =false;
					LogUtil.v("MusicPlayerService", "path:"+mp3Info.getPath());
					playOrPauseMusic(mPlayer, mp3Info);
					LogUtil.v("MusicPlayerService", "onNext Not NULL");
					
				}
				break;
			case PLAYORDER_LOOP:
				
				break;
			case PLAYORDER_RANDOM:
				
				break;
			default:
				break;
			}
		}
		// ����/��ͣ����
		public void playOrPauseMusic(MediaPlayer musicPlayer, Mp3Info mp3){
	
			//�ж����׸��Ƿ��Ѿ����ع���
			if (!musicIsPrepared) {
				try {
					musicPlayer.reset();
					musicPlayer.setDataSource(mp3.getPath());
					musicPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.v("MusicPlayerService", "load Music Error");
				} 		
				mp3Current = mp3;
				musicIsPrepared = true;	
				LogUtil.v("MusicPlayerService", "onStatusChange");
			}
			//����������ͣ�벥��
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
				LogUtil.v("MusicPlayerService", "onPause");
			}else {
				musicPlayer.start();
				LogUtil.v("MusicPlayerService", "onStart");
			}
			LogUtil.v("MusicPlayerService", ""+mp3InfoList.size()+"");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onBind");
		return mBinder;
	}

	public Mp3Info getPreviousMusicByOrder() {
		// ��ȡ��ǰ������λ��
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		LogUtil.v("MusicPlayerService", "currentInde:" + currentIndex + "");
		if (currentIndex > 0) {
			currentIndex--;
			LogUtil.v("MusicPlayerService", "currentInde:" + currentIndex + "");
			return mp3InfoList.get(currentIndex);
		} else {
			return null;
		}
	}

	public Mp3Info getNextMusicByOrder() {
		//��ȡ��ǰ������λ��
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		LogUtil.v("MusicPlayerService", "currentInde:"+currentIndex+"");
		if (currentIndex<mp3InfoList.size()-1) {
			currentIndex++;
			LogUtil.v("MusicPlayerService", "currentInde:"+currentIndex+"");
			return mp3InfoList.get(currentIndex);
		}else {
			return null;
		}	
	}

	public Mp3Info getCurrentMusic(int PLAYORDER) {
		if (!mp3InfoList.isEmpty()) {
			return mp3InfoList.get(0);
		}else {
			return null;
		}
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
		// Notification:���޸�
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Test", System.currentTimeMillis()) {
		};
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "�ṷ����", "Hello �ṷ!",
				pendingIntent);
		startForeground(1, notification);

//		//ע��"���Ű�ť"�㲥
//		IntentFilter onPlayFilter = new IntentFilter();
//		onPlayFilter.addAction("android.intent.action.PLAY");
//		onPlayBroadcastReciver = new OnPlayBroadcastReciver();
//		onPlayFilter.setPriority(500);
//		registerReceiver(onPlayBroadcastReciver, onPlayFilter);
		
		LogUtil.v("MusicPlayerService", "onCreate");
		mp3InfoList = Mp3FileUtil.getMp3InfoList(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v("MusicPlayerService", "onStartCommand");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		unregisterReceiver(onPlayBroadcastReciver);
		mPlayer.stop();
		mPlayer.release();
		LogUtil.v("MusicPlayerService", "onDestory");
	}
	
//	class OnPlayBroadcastReciver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//		}
//	}
}


