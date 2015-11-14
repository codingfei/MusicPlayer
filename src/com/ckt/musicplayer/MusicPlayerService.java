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

public class MusicPlayerService extends Service {

	private MediaPlayer mPlayer = new MediaPlayer();

	private boolean musicIsPrepared = false;
	
	private Mp3Info mp3Current = null;
	private int currentPosition; //播放进度
	
//	private ArrayList<Mp3Info> musicList;  //音乐列表
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();
	
	private Handler matchHandler;
	
//	OnPlayBroadcastReciver onPlayBroadcastReciver;
	// 播放模式
	public static final int PLAYORDER_ORDER = 1;
	public static final int PLAYORDER_LOOP = 2;
	public static final int PLAYORDER_RANDOM = 3;
	public static final int PLAYORDER = PLAYORDER_ORDER;

	private List<Mp3Info> mp3InfoList;
	private int playerState = 0;
	private Handler handler = new Handler();
	public class MusicPlayerBinder extends Binder {

		// 获取音乐播放器
		public MediaPlayer getMusicPlayer() {
			return mPlayer;
		}
		public int getPlayerState() {
			return playerState;
		}
		public void setPlayerState(int state) {
			playerState = state;
		}
		//获取列表
		public List<Mp3Info> getMusicList() {
			return mp3InfoList;
		}
		public Mp3Info getCurrentMusic() {
			return mp3Current;
		}
		public void playPreviousMusic(int playOrder){
			//首先在当前播放模式下
			if (playOrder == 0) {
				playOrder = PLAYORDER;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//获取当前模式上一首歌的信息
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
			default:
				break;
			}
		}
		//下一首
		public void playNextMusic(int playOrder){
			//首先在当前播放模式下
			if (playOrder == 0) {
				playOrder = PLAYORDER;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				//获取当前模式下下一首歌的信息
				mp3Current=getNextMusicByOrder();
				if(mPlayer.isPlaying())
				{
					playOrPauseMusic(mPlayer, mp3Current,true);
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
		// 播放/暂停歌曲
		public void playOrPauseMusic(MediaPlayer musicPlayer, Mp3Info mp3,boolean isChange){
	
			//判断这首歌是否已经加载过了
			if (!musicIsPrepared || isChange == true) {
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
			//设置音乐暂停与播放
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
				LogUtil.v("MusicPlayerService", "onPause");
			}else {
				musicPlayer.start();
				LogUtil.v("MusicPlayerService", "onStart");
			}
			LogUtil.v("MusicPlayerService", ""+mp3InfoList.size()+"");
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
		// 获取当前歌曲的位置
		int currentIndex =0;
//		if(mp3Current != null)
		currentIndex = mp3InfoList.indexOf(mp3Current);
		Log.i("message", currentIndex + "++++++++currentIndex");
		Log.i("message", mp3Current + "++++++++mp3Current");
		if (currentIndex > 0) {
			currentIndex = (currentIndex-1) >0 ?currentIndex-1 : 0;
			Log.i("message", currentIndex + "-------currentIndex");
			return mp3InfoList.get(currentIndex);
		} else {
			return mp3InfoList.get(0);
		}
	}

	public Mp3Info getNextMusicByOrder() {
		//获取当前歌曲的位置
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		if (currentIndex<mp3InfoList.size()) {
			currentIndex  = (currentIndex +1) < mp3InfoList.size() ?  (currentIndex +1): mp3InfoList.size()-1;
			Log.i("message", currentIndex + "-------currentIndex");
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
		// Notification:待修改
		mPlayer = new MyMediPlayer(getApplicationContext());
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Test", System.currentTimeMillis()) {
		};
		//注册通知栏点击事件的广播接收器
		this.mp3InfoList = Mp3FileUtil.getMp3InfoList(this);
		IntentFilter filter = new IntentFilter(NotificationUtils.Broadcast_INTENT_ACTION);
		registerReceiver(new NotificationClickReciver(), filter);
				
				//显示通知栏
		NotificationUtils.show(getApplicationContext(), false, this.mp3InfoList.get(0));
	
		mp3InfoList = Mp3FileUtil.getMp3InfoList(getApplicationContext());
		readConf();//读取当前播放的音乐,以及播放进度,等等
		//把当前歌曲加载到mediaPlayer中--->activity会读取
		try {
			mPlayer.reset();
			mPlayer.setDataSource(mp3Current.getPath());
			mPlayer.prepare();
			mPlayer.seekTo(currentPosition);
			musicIsPrepared = true;
		} catch (Exception e) {
			musicIsPrepared = false;
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
		LogUtil.v("MusicPlayerService", "onDestory");
	}
	
	class NotificationClickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int action = intent.getIntExtra(NotificationUtils.Broadcast_INTENT_EXTRA_DATA, -1);
//			Log.v("MusicPlayerService", "通知栏点击了:"+action);
//			System.out.println("NotificationClickReciver---->");
			switch (action) {
			case NotificationUtils.NOTIFICATION_LAST:  //上一首
				mBinder.playPreviousMusic(MusicPlayerService.PLAYORDER_ORDER);
				if(!mPlayer.isPlaying() && mp3Current != null)
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}
				break;
			case NotificationUtils.NOTIFICATION_PLAY_OR_PAUSE:  //暂停或者播放
				Mp3Info mp3 = (Mp3Info) mBinder.getMusicList().get(0);
				mBinder.playOrPauseMusic(mPlayer, mp3,false);
				if(!mPlayer.isPlaying())
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}		
				break;
			case NotificationUtils.NOTIFICATION_NEXT:  //下一首
				mBinder.playNextMusic(MusicPlayerService.PLAYORDER_ORDER);
				if(!mPlayer.isPlaying() && mp3Current != null)
				{
					NotificationUtils.show(getApplicationContext(),false,mp3Current);
				}else
				{
					NotificationUtils.show(getApplicationContext(),true,mp3Current);
				}
				break;
			case NotificationUtils.NOTIFICATION_CANEL:  //点击了通知栏上面的叉叉(X)
				break;
			default:
				break;
			}
			Message msg = new Message();
			msg.what = 1;
			msg.obj = mp3InfoList.indexOf(mp3Current);
			matchHandler.sendMessage(msg);
			System.out.println(msg.obj +"++++++++++++++");
		}
	}
	/**
	 * 保存配置到文件
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
	 * 读取配置
	 */
	public void readConf() {
		LogUtil.v("MusicPlayerService", "service readConf()");
		SharedPreferences mySharedPreferences= getSharedPreferences("fiveMusic", 
				Activity.MODE_PRIVATE);
		int nowIndex = mySharedPreferences.getInt("nowIndex", 0); //没有读取到,默认是0
		mp3Current = mp3InfoList.get(nowIndex);
		currentPosition = mySharedPreferences.getInt("currentPosition", 20000);
	}
}


