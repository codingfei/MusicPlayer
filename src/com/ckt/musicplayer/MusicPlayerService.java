package com.ckt.musicplayer;

import java.util.List;

import android.app.Activity;
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

import com.ckt.modle.Mp3Info;
import com.ckt.utils.Mp3FileUtil;
import com.ckt.utils.MyMediPlayer;
import com.ckt.utils.NotificationUtils;

public class MusicPlayerService extends Service implements OnCompletionListener {

	private MediaPlayer mPlayer = new MediaPlayer();

	// private boolean musicIsPrepared = false;

	private Mp3Info mp3Current = null;
	private int currentPosition; // 播放进度

	// private ArrayList<Mp3Info> musicList; //音乐列表
	private MusicPlayerBinder mBinder = new MusicPlayerBinder();

	private Handler matchHandler;
	
	private NotificationClickReciver notificationClickReciver;

	// OnPlayBroadcastReciver onPlayBroadcastReciver;
	// 播放模式
	public static final int PLAYORDER_ORDER = 1;// 顺序播放
	public static final int PLAYORDER_LOOP = 2;// 循环播放
	public static final int PLAYORDER_RANDOM = 3;// 随机播放
	public static final int PLAYORDER_SINGLE = 4;// 单曲播放
	public int playOrder = PLAYORDER_SINGLE;// 默认的播放模式

	private List<Mp3Info> mp3InfoList;
	private int playerState = 0;

	public int getPlayOrder() {
		return playOrder;
	}

	public void setPlayOrder(int playOrder) {
		this.playOrder = playOrder;
	}

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

		// 获取列表
		public List<Mp3Info> getMusicList() {
			return mp3InfoList;
		}

		public Mp3Info getCurrentMusic() {
			return mp3Current;
		}

		public void setCurrentMusic(Mp3Info mp3Info) {
			mp3Current = mp3Info;
		}

		// 获取播放模式列表
		public int[] getMusicPlayOrders() {
			int[] playOrderList = new int[4];
			playOrderList[0] = MusicPlayerService.PLAYORDER_ORDER;
			playOrderList[1] = MusicPlayerService.PLAYORDER_LOOP;
			playOrderList[2] = MusicPlayerService.PLAYORDER_RANDOM;
			playOrderList[3] = MusicPlayerService.PLAYORDER_SINGLE;
			return playOrderList;
		}

		// 获取当前播放模式
		public int getCurrentMusicPlayOrder() {
			return getPlayOrder();
		}

		// 设置播放模式
		public void setMusicPlayOrder(int playOrder) {
			setPlayOrder(playOrder);
		}

		public void playPreviousMusic(int playOrder) {
			if(mp3InfoList.size() == 0)return;
			// 首先在当前播放模式下
			if (playOrder == 0) {
				playOrder = getPlayOrder();
				;
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				// 获取当前模式上一首歌的信息
				mp3Current = getPreviousMusicByOrder();
				playOrPauseMusic(mPlayer, mp3Current, true);

				break;
			case PLAYORDER_LOOP:

				break;
			case PLAYORDER_RANDOM:

				break;
			case PLAYORDER_SINGLE:
				playOrPauseMusic(mPlayer, mp3Current, true);
				break;
			default:
				break;
			}
		}

		// 下一首
		public void playNextMusic(int playOrder) {
			if(mp3InfoList.size() == 0)return;
			// 首先在当前播放模式下
			if (playOrder == 0) {
				playOrder = getPlayOrder();
			}
			switch (playOrder) {
			case PLAYORDER_ORDER:
				// 获取当前模式下下一首歌的信息
				mp3Current = getNextMusicByOrder();

				playOrPauseMusic(mPlayer, mp3Current, true);

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
		public void playOrPauseMusic(MediaPlayer musicPlayer, Mp3Info mp3,
				boolean isChange) {
			if(mp3InfoList.size() == 0)return;
			// 判断这首歌是否已经加载过了
			if (isChange) {
				Log.i("message", "isChange++++++++++++++");
				try {
					musicPlayer.reset();
					musicPlayer.setDataSource(mp3.getPath());
					musicPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				}
				mp3Current = mp3;
			}
			// 设置音乐暂停与播放
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
			} else {
				musicPlayer.start();
			}
			//更新通知栏:
			NotificationUtils.show(getApplicationContext(), mPlayer.isPlaying(), mp3Current);
		}

		public void setMatchHandler(Handler handler) {
			matchHandler = handler;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public Mp3Info getPreviousMusicByOrder() {
		// 获取当前歌曲的位置
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		currentIndex = mp3InfoList.indexOf(mp3Current);
		if (currentIndex > 0) {
			currentIndex = (currentIndex - 1) > 0 ? currentIndex - 1 : 0;
			mBinder.setCurrentMusic(mp3InfoList.get(currentIndex));
			return mp3InfoList.get(currentIndex);
		} else {
			return mp3InfoList.get(0);
		}
	}

	public Mp3Info getNextMusicByOrder() {
		// 获取当前歌曲的位置
		int currentIndex = mp3InfoList.indexOf(mp3Current);
		if (currentIndex < mp3InfoList.size()) {
			currentIndex = (currentIndex + 1) < mp3InfoList.size() ? (currentIndex + 1)
					: mp3InfoList.size() - 1;
			mBinder.setCurrentMusic(mp3InfoList.get(currentIndex));
			return mp3InfoList.get(currentIndex);
		} else {
			return null;
		}
	}

	public Mp3Info getCurrentMusic(int PLAYORDER) {
		if (!mp3InfoList.isEmpty()) {
			return mp3InfoList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		System.out.println("service onCreate!!!!");
		super.onCreate();
		// Notification:待修改
		mPlayer = new MyMediPlayer(getApplicationContext());
		playOrder = PLAYORDER_ORDER;
		mPlayer.setOnCompletionListener(this);
		// 注册通知栏点击事件的广播接收器
		this.mp3InfoList = Mp3FileUtil.getMp3InfoList(this);
		IntentFilter filter = new IntentFilter(
				NotificationUtils.Broadcast_INTENT_ACTION);
		notificationClickReciver = new NotificationClickReciver();
		registerReceiver(notificationClickReciver, filter);

		mp3InfoList = Mp3FileUtil.getMp3InfoList(getApplicationContext());
		if(mp3InfoList.size() == 0)	return; //一首歌都没有
		readConf();// 读取当前播放的音乐,以及播放进度,等等
		// 把当前歌曲加载到mediaPlayer中--->activity会读取
		try {
			mPlayer.reset();
			mPlayer.setDataSource(mp3Current.getPath());
			mPlayer.prepare();
			mPlayer.seekTo(currentPosition);
		} catch (Exception e) {}
		//当service被其他安全软件杀掉的时候,会自动调用onCreate()方法哦
		// 显示通知栏
//		NotificationUtils.show(getApplicationContext(), false, mp3Current);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		saveConf();
		mPlayer.stop();
		mPlayer.release();
		unregisterReceiver(notificationClickReciver);
		NotificationUtils.close(getApplicationContext());
	}

	class NotificationClickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			if(mp3InfoList.size() == 0)return;
			int action = intent.getIntExtra(
					NotificationUtils.Broadcast_INTENT_EXTRA_DATA, -1);
			System.out.println("NotificationClickReciver-->onRecevice:"+action);
			switch (action) {
			case NotificationUtils.NOTIFICATION_LAST: // 上一首
				mBinder.playPreviousMusic(MusicPlayerService.PLAYORDER_ORDER);
				break;
			case NotificationUtils.NOTIFICATION_PLAY_OR_PAUSE: // 暂停或者播放
				mBinder.playOrPauseMusic(mPlayer, mp3Current, false);
				break;
			case NotificationUtils.NOTIFICATION_NEXT: // 下一首
				mBinder.playNextMusic(MusicPlayerService.PLAYORDER_ORDER);
				break;
			case NotificationUtils.NOTIFICATION_CANEL: // 点击了通知栏上面的叉叉(X)
				break;
			default:
				break;
			}
			//通知Activity更新UI
			matchHandler.sendEmptyMessage(1);
		}
	}

	/**
	 * 保存配置到文件
	 */
	public void saveConf() {
		SharedPreferences mySharedPreferences = getSharedPreferences(
				"fiveMusic", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		try {
			int nowIndex = mp3InfoList.indexOf(mp3Current);
			if (nowIndex >= 0) {
				editor.putInt("nowIndex", nowIndex);
				editor.putInt("currentPosition", mPlayer.getCurrentPosition());
			}
		} catch (Exception e) {
		}
		editor.commit();
	}

	/**
	 * 读取配置
	 */
	public void readConf() {
		SharedPreferences mySharedPreferences = getSharedPreferences(
				"fiveMusic", Activity.MODE_PRIVATE);
		int nowIndex = mySharedPreferences.getInt("nowIndex", 0); // 没有读取到,默认是0
		mp3Current = mp3InfoList.get(nowIndex);
		// currentPosition = mySharedPreferences.getInt("currentPosition",
		// 20000);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// 首先判断歌单是否为空
		if (!mp3InfoList.isEmpty()) {
			mBinder.playNextMusic(getPlayOrder());
			matchHandler.sendEmptyMessage(1);
		}
	}
}
