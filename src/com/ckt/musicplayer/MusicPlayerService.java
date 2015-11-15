package com.ckt.musicplayer;

import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;
import com.ckt.utils.Mp3FileUtil;
import com.ckt.utils.MyMediPlayer;
import com.ckt.utils.NotificationUtils;

public class MusicPlayerService extends Service implements OnCompletionListener{

	private MediaPlayer mPlayer = new MediaPlayer();

//	private boolean musicIsPrepared = false;
	
	private Mp3Info mp3Current = null;
	private int currentPosition; //���Ž���
	
//	private ArrayList<Mp3Info> musicList;  //�����б�
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();
	
	private Handler matchHandler;
	
//	OnPlayBroadcastReciver onPlayBroadcastReciver;
	// ����ģʽ
	public static final int PLAYORDER_ORDER = 1;//˳�򲥷�
	public static final int PLAYORDER_LOOP = 2;//ѭ������
	public static final int PLAYORDER_RANDOM = 3;//�������
	public static final int PLAYORDER_SINGLE = 4;//��������
	public int playOrder = PLAYORDER_SINGLE;//Ĭ�ϵĲ���ģʽ


	private List<Mp3Info> mp3InfoList;
	private int playerState = 0;
	
	public int getPlayOrder() {
		return playOrder;
	}

	public void setPlayOrder(int playOrder) {
		this.playOrder = playOrder;
	}
	public class MusicPlayerBinder extends Binder {

		// ��ȡ���ֲ�����
		public MediaPlayer getMusicPlayer() {
			return mPlayer;
		}
		public int getPlayerState() {
			return playerState;
		}
		public void setPlayerState(int state) {
			playerState = state;
		}

		// ��ȡ�б�
		public List<Mp3Info> getMusicList() {
			return mp3InfoList;
		}

		public Mp3Info getCurrentMusic() {
			return mp3Current;
		}
		public void setCurrentMusic(Mp3Info mp3Info)
		{
			mp3Current = mp3Info;
		}
		// ��ȡ����ģʽ�б�
		public int[] getMusicPlayOrders() {
			int[] playOrderList = new int[4];
			playOrderList[0] = MusicPlayerService.PLAYORDER_ORDER;
			playOrderList[1] = MusicPlayerService.PLAYORDER_LOOP;
			playOrderList[2] = MusicPlayerService.PLAYORDER_RANDOM;
			playOrderList[3] = MusicPlayerService.PLAYORDER_SINGLE;
			return playOrderList;
		}
		//��ȡ��ǰ����ģʽ
		public int getCurrentMusicPlayOrder(){
			return getPlayOrder();
		}
		
		//���ò���ģʽ
		public void setMusicPlayOrder(int playOrder){
			setPlayOrder(playOrder);
		}
	
		public void playPreviousMusic(int playOrder){
			//�����ڵ�ǰ����ģʽ��
			if (playOrder == 0) {
				playOrder = getPlayOrder();;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//��ȡ��ǰģʽ��һ�׸����Ϣ
				mp3Current =getPreviousMusicByOrder();
				if(mPlayer.isPlaying())
				{
					playOrPauseMusic(mPlayer, mp3Current,true);
				}
				break;
			case PLAYORDER_LOOP:
				
				break;
			case PLAYORDER_RANDOM:
				
				break;
			case PLAYORDER_SINGLE:
//				musicIsPrepared = false;
				playOrPauseMusic(mPlayer,mp3Current,true);
				break;
			default:
				break;
			}
		}
		//��һ��
		public void playNextMusic(int playOrder){
			//�����ڵ�ǰ����ģʽ��
			if (playOrder == 0) {
				playOrder = getPlayOrder();
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//��ȡ��ǰģʽ����һ�׸����Ϣ
				mp3Current=getNextMusicByOrder();
				
				if(mPlayer.isPlaying())
				{
					playOrPauseMusic(mPlayer, mp3Current,true);
				}
				else
				{
					
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
		public void playOrPauseMusic(MediaPlayer musicPlayer, Mp3Info mp3,boolean isChange){
	
			//�ж����׸��Ƿ��Ѿ����ع���
			if (isChange) {
				try {
					musicPlayer.reset();
					musicPlayer.setDataSource(mp3.getPath());
					musicPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				} 		
				mp3Current = mp3;
//				musicIsPrepared = true;	
			}
			//����������ͣ�벥��
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
				Log.i("message","pause++++++++++++++++playOrPauseMusic");
			}else {
				Log.i("message","start++++++++++++++++playOrPauseMusic");
				musicPlayer.start();
			}
		}
		public void setMatchHandler(Handler handler) {
			matchHandler = handler;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.v("MusicPlayerService", "onBind");
		mp3Current = mp3InfoList.get(0);
		return mBinder;
	}

	public Mp3Info getPreviousMusicByOrder() {
		// ��ȡ��ǰ������λ��
		int currentIndex =0;
//		if(mp3Current != null)
		currentIndex = mp3InfoList.indexOf(mp3Current);
		Log.i("message", currentIndex + "++++++++currentIndex");
		Log.i("message", mp3Current + "++++++++mp3Current");
		if (currentIndex > 0) {
			currentIndex = (currentIndex-1) >0 ?currentIndex-1 : 0;
			return mp3InfoList.get(currentIndex);
		} else {
			return mp3InfoList.get(0);
		}
	}

	public Mp3Info getNextMusicByOrder() {
		//��ȡ��ǰ������λ��
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		if (currentIndex<mp3InfoList.size()) {
			currentIndex  = (currentIndex +1) < mp3InfoList.size() ?  (currentIndex +1): mp3InfoList.size()-1;
			System.out.println("currentIndex------------------++++++++++++++++"+currentIndex);
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
		mPlayer = new MyMediPlayer(getApplicationContext());
		playOrder = PLAYORDER_ORDER;
		mPlayer.setOnCompletionListener(this);
//		Notification notification = new Notification(R.drawable.ic_launcher,
//				"Test", System.currentTimeMillis()) {
//		};
		//ע��֪ͨ������¼��Ĺ㲥������
		this.mp3InfoList = Mp3FileUtil.getMp3InfoList(this);
		IntentFilter filter = new IntentFilter(NotificationUtils.Broadcast_INTENT_ACTION);
		registerReceiver(new NotificationClickReciver(), filter);
				
				//��ʾ֪ͨ��
		NotificationUtils.show(getApplicationContext(), false, this.mp3InfoList.get(0));
	
		mp3InfoList = Mp3FileUtil.getMp3InfoList(getApplicationContext());
		readConf();//��ȡ��ǰ���ŵ�����,�Լ����Ž���,�ȵ�
		//�ѵ�ǰ�������ص�mediaPlayer��--->activity���ȡ
		try {
			mPlayer.reset();
			mPlayer.setDataSource(mp3Current.getPath());
			mPlayer.prepare();
			mPlayer.seekTo(currentPosition);
//			musicIsPrepared = true;
		} catch (Exception e) {
//			musicIsPrepared = false;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.v("MusicPlayerService", "onStartCommand");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		saveConf();
		mPlayer.stop();
		mPlayer.release();
	}
	
	class NotificationClickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int action = intent.getIntExtra(NotificationUtils.Broadcast_INTENT_EXTRA_DATA, -1);
//			Log.v("MusicPlayerService", "֪ͨ�������:"+action);
//			System.out.println("NotificationClickReciver---->");
			switch (action) {
			case NotificationUtils.NOTIFICATION_LAST:  //��һ��
				mBinder.playPreviousMusic(MusicPlayerService.PLAYORDER_ORDER);
				if(!mPlayer.isPlaying() && mp3Current != null)
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}
				break;
			case NotificationUtils.NOTIFICATION_PLAY_OR_PAUSE:  //��ͣ���߲���
				Mp3Info mp3 = (Mp3Info) mBinder.getMusicList().get(0);
				mBinder.playOrPauseMusic(mPlayer, mp3,true);
				if(!mPlayer.isPlaying())
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}		
				break;
			case NotificationUtils.NOTIFICATION_NEXT:  //��һ��
				mBinder.playNextMusic(MusicPlayerService.PLAYORDER_ORDER);
				if(!mPlayer.isPlaying() && mp3Current != null)
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}
				break;
			case NotificationUtils.NOTIFICATION_CANEL:  //�����֪ͨ������Ĳ��(X)
				break;
			default:
				break;
			}
			Message msg = new Message();
			msg.what = 1;
			msg.obj = mp3InfoList.indexOf(mp3Current);
			matchHandler.sendMessage(msg);
		}
	}
	/**
	 * �������õ��ļ�
	 */
	public void saveConf() {
		LogUtil.v("MusicPlayerService", "service saveConf()");
		SharedPreferences mySharedPreferences= getSharedPreferences("fiveMusic", 
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		try {
			int nowIndex = mp3InfoList.indexOf(mp3Current);
			if(nowIndex >= 0) {
				editor.putInt("nowIndex", nowIndex);
				editor.putInt("currentPosition", mPlayer.getCurrentPosition());
				LogUtil.v("MusicPlayerService", "mPlayer.getCurrentPosition()"+mPlayer.getCurrentPosition());
			}
		} catch (Exception e) {}
		editor.commit();
	}
	
	/**
	 * ��ȡ����
	 */
	public void readConf() {
		LogUtil.v("MusicPlayerService", "service readConf()");
		SharedPreferences mySharedPreferences= getSharedPreferences("fiveMusic", 
				Activity.MODE_PRIVATE);
		int nowIndex = mySharedPreferences.getInt("nowIndex", 0); //û�ж�ȡ��,Ĭ����0
		mp3Current = mp3InfoList.get(nowIndex);
		currentPosition = mySharedPreferences.getInt("currentPosition", 20000);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		//�����жϸ赥�Ƿ�Ϊ��
		if (!mp3InfoList.isEmpty()) {
			mBinder.playNextMusic(getPlayOrder());
			Message msg = new Message();
			msg.what = 2;
			msg.obj = mp3InfoList.indexOf(mp3Current);
			matchHandler.sendMessage(msg);
		}
	}
}


