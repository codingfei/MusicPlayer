package com.ckt.musicplayer;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;
import com.ckt.ui.CustomProgressBar;
import com.ckt.ui.CustomProgressBar.OnCircleProgressBarDragListener;
import com.ckt.utils.JsonUtils;
import com.ckt.utils.NotificationUtils;

public class MainActivity extends Activity implements View.OnClickListener,
		ServiceConnection, OnSeekBarChangeListener,
		OnCircleProgressBarDragListener {
	// 列表按钮
	private ImageButton list_but = null;
	// 播放按钮
	private ImageButton play_but = null;

	private ImageButton next_but;

	private ImageButton pre_but;

//	private ImageButton menu_but;
	// cd与中间镂空控件
	private ImageView cd_view = null;
	private ImageView center_view = null;
	private MediaPlayer mPlayer;
	// 歌曲进度显示控件
	private CustomProgressBar circle_progress = null;
	private ProgressBar sound_progress = null;
	private MusicPlayerService.MusicPlayerBinder mBinder;
	private MediaPlayer mediaPlayer = null;// 播放器

	private SeekBar seekbar_voice;
	private boolean isAllowChangeVoice = false; // 设置拖动seekbar时的互斥量
	private boolean isUserChangingVoice = false; // 表示当前是否是我们自己在改变音量--->这个时候就不要理会系统音量改变的广播了,不然会出现一些小问题
	private AudioManager mAudioManager = null; // Audio管理器，用了控制音量
	private MyVolumeReceiver myVolumeReceiver;
	private ArrayList<Mp3Info> musicList; // 音乐列表
	public int currentSong = 0;
	private ValueAnimator valueAnimator = null;
	float value = 0;
	private boolean isChange = false;
	private Handler handler = new Handler();
	private Handler matchHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 1:
				if (!mPlayer.isPlaying()) {
					play_but.setImageResource(R.drawable.widget_music_btn_play_normal);
				} else {
					play_but.setImageResource(R.drawable.widget_music_btn_pause_normal);
				}
				postMessagetoFeatureFragment((Integer) msg.obj);
				break;
			case 2:
				postMessagetoFeatureFragment((Integer) msg.obj);
				break;
			default:
				break;
			}
		};
	};
	private int state = 1;
	private FeatureFragment featureFragment = null;
	private CDFragment cdFragment = null;
	private Bitmap bit = null;

	private Runnable runnable_changeProgress; // 改变音乐进度条的线程
	public static final int RUNNABLE_DELAY = 100; // 改变进度条的间隔时间100ms

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(isScreenOriatationPortrait())
		{
			setContentView(R.layout.activity_main);
		}
		else
		{
			setContentView(R.layout.activity_main_land);
		}

		Intent startServiceIntent = new Intent(this, MusicPlayerService.class);
		startService(startServiceIntent);
		Intent bindIntent = new Intent(this, MusicPlayerService.class);
		bindService(bindIntent, this, BIND_AUTO_CREATE);
		cd_view = (ImageView) findViewById(R.id.CD_img);
		circle_progress = (CustomProgressBar) findViewById(R.id.circle_pro);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// sound_progress = (ProgressBar)findViewById(R.id.sound_progress);
		center_view = (ImageView) findViewById(R.id.music_center);
		// 添加主界面布局控件的点击响应
		// ---播放/暂停按钮---//
		play_but = (ImageButton) findViewById(R.id.play_btn);
		// ---下一首歌按钮---//
		next_but = (ImageButton) findViewById(R.id.next_btn);
		next_but.setOnClickListener(this);
		// ---上一首歌按钮---//
		pre_but = (ImageButton) findViewById(R.id.pre_btn);
		pre_but.setOnClickListener(this);
		// ---歌曲列表按钮---//
		list_but = (ImageButton) findViewById(R.id.list_btn);
		list_but.setOnClickListener(this);
		// ---关闭按钮---//
//		menu_but = (ImageButton) findViewById(R.id.menu_btn);
//		menu_but.setOnClickListener(this);
		seekbar_voice = (SeekBar) findViewById(R.id.voice_seekbar);
		play_but.setOnClickListener(this);
		bit = BitmapFactory.decodeResource(getResources(), R.drawable.wangfei);
		cd_view.setBackground(new BitmapDrawable(
				getCircleBitmap(this, bit, 200)));
		circle_progress.setOnDragListener(this);

		// 初始化voice_seekbar:
		seekbar_voice.setOnSeekBarChangeListener(this);
		int maxV = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentV = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		seekbar_voice.setMax(maxV * 10); // 为什么要放大十倍???---->因为max值一般是10多,太小了,拖动seekbar的时候明显感觉不爽
		seekbar_voice.setProgress(currentV * 10);

		// 注册监听--->监听系统音量的改变
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		myVolumeReceiver = new MyVolumeReceiver();
		registerReceiver(new MyVolumeReceiver(), filter);
		// 这个线程是用来更新进度条的哦
		runnable_changeProgress = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				circle_progress.setProgress(mPlayer.getCurrentPosition());
				handler.postDelayed(runnable_changeProgress, RUNNABLE_DELAY);
			}
		};

		// 加载旋转动画
		// startRoateCDView()-->开始动画
		// pauseRoateCDView()-->暂停动画
		valueAnimator = ValueAnimator.ofFloat(360);
		LinearInterpolator lir = new LinearInterpolator();
		valueAnimator.setInterpolator(lir);
		valueAnimator.setRepeatCount(-1);
		valueAnimator.setDuration(20000).addUpdateListener(
				new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						cd_view.setRotation((Float) animation
								.getAnimatedValue() + value);
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ShowSongActivity.RESULT_CODE) { // 返回了点击的歌曲的index
			int index = data.getIntExtra("position", -1);
			if (index >= 0) {// 这里面做相关的响应---->播放第index首歌
				postMessagetoFeatureFragment(index);
			}
			Mp3Info mp3 = (Mp3Info) mBinder.getMusicList().get(index);
			mBinder.setCurrentMusic(mp3);
			currentSong = musicList.indexOf(mp3);
			mBinder.playOrPauseMusic(mPlayer, mp3,true);
			play_but.setImageResource(R.drawable.widget_music_btn_pause_normal);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Mp3Info mp3 = null;
		switch (v.getId()) {
		// 打开歌曲列表
		case R.id.list_btn:
			Intent intent = new Intent(MainActivity.this,
					ShowSongActivity.class);
			intent.putExtra("list", JsonUtils.changeListToJsonObj(musicList)); // 启动Activity并把歌曲列表传过去
			startActivityForResult(intent, 1111); // 这里要用startActivityForResult
			break;

		case R.id.play_btn:
			mp3 =mBinder.getCurrentMusic();
			Log.i("message", "isChange---------" + isChange);
			mBinder.playOrPauseMusic(mPlayer, mp3,isChange);
			isChange = false;
			circle_progress.setMax((int) mp3.getDuring()); // 设置进度条的最大值
			startUpdatePlaingProgress(); // 开始更新进度条(handle里面更新哦)
			// 播放音乐
			if (mPlayer.isPlaying()) {
				startRoateCDView();
				startUpdatePlaingProgress();
				play_but.setImageResource(R.drawable.widget_music_btn_pause_normal);
			} else {
				pauseRoateCDView();
				stopUpdatePlayingProgress();
				play_but.setImageResource(R.drawable.widget_music_btn_play_normal);
			}
			postMessagetoFeatureFragment(currentSong);
			break;
		case R.id.next_btn:
			mp3 =mBinder.getCurrentMusic();
			mBinder.playNextMusic(MusicPlayerService.PLAYORDER_ORDER);
			int temp = currentSong;
			currentSong = (currentSong + 1) < musicList.size() ? (currentSong + 1)
					: musicList.size() - 1;
			postMessagetoFeatureFragment(currentSong);
			if(temp!=currentSong)
			isChange=true;
			break;
		case R.id.pre_btn:
			mp3 =mBinder.getCurrentMusic();
			mBinder.playPreviousMusic(MusicPlayerService.PLAYORDER_ORDER);
			int temp1 = currentSong;
			currentSong = currentSong - 1 >= 0 ? currentSong - 1 : 0;
			postMessagetoFeatureFragment(currentSong);
			if(temp1!=currentSong)
			isChange=true;
			break;
		default:
			break;
		}
	}

	/**
	 * 开始更新进度条
	 */
	public void startUpdatePlaingProgress() {
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(runnable_changeProgress, RUNNABLE_DELAY);// 开始更新进度条
	}

	/**
	 * 停止更新进度条
	 */
	public void stopUpdatePlayingProgress() {
		handler.removeCallbacksAndMessages(null);
	}

	/**
	 * 开始旋转cd图片
	 */
	public void startRoateCDView() {
		if (valueAnimator == null)
			return;
		valueAnimator.start();
	}

	/**
	 * 暂停旋转CD:
	 */
	public void pauseRoateCDView() {
		if (valueAnimator == null)
			return;
		valueAnimator.cancel();
		value = cd_view.getRotation(); // 记录当前旋转的位置,下次接着这里旋转
	}

	public static Bitmap getCircleBitmap(Context context, Bitmap src,
			float radius) {
		radius = dipTopx(context, radius);
		int w = src.getWidth();
		int h = src.getHeight();
		int canvasW = Math.round(radius * 2);
		Bitmap bitmap = Bitmap.createBitmap(canvasW, canvasW,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Path path = new Path();
		path.addCircle(radius, radius, radius, Path.Direction.CW);
		canvas.clipPath(path);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Rect srcRect = new Rect(0, 0, w, h);
		Rect dstRect = new Rect(0, 0, canvasW, canvasW);
		canvas.drawBitmap(src, srcRect, dstRect, paint);
		return bitmap;
	}

	public static float dipTopx(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (dpValue * scale + 0.5f);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mBinder = (MusicPlayerService.MusicPlayerBinder) service;
		mPlayer = mBinder.getMusicPlayer();
		musicList = (ArrayList<Mp3Info>) mBinder.getMusicList();
		try { // 这里的mp3可能为null,(没有一首歌的时候)
			Mp3Info mp3 = mBinder.getCurrentMusic(); // 获取当前播放的音乐
			circle_progress.setMax((int) mp3.getDuring());
			circle_progress.setProgress(mPlayer.getCurrentPosition());
			postMessagetoFeatureFragment(currentSong);
		} catch (Exception e) {
		}

		// 更新界面:
		if (mPlayer.isPlaying()) {

			startRoateCDView(); // 开始旋转cdview
			startUpdatePlaingProgress();
		}
		// Toast.makeText(this, musicList+"", Toast.LENGTH_LONG).show();
		// postMessagetoFeatureFragment(currentSong);
		mBinder.setMatchHandler(matchHandler);

	}

	public void postMessagetoFeatureFragment(int index) {
		if (index >= 0 && index < musicList.size()) {
			FragmentManager manager = getFragmentManager();
			featureFragment = (FeatureFragment) manager
					.findFragmentById(R.id.feature_fragment);
			cdFragment = (CDFragment) manager
					.findFragmentById(R.id.cd_fragment);
			bit = musicList.get(index).getBitmap(this);
			if (bit != null) {
				cdFragment.imageView.setBackground(new BitmapDrawable(
						getCircleBitmap(this, bit, 200)));
			}

			featureFragment.songName.setText(musicList.get(index).getName());
			Log.i("test", "postMessage" + musicList.get(index).getName());
			featureFragment.cdName.setText(musicList.get(index).getAlbum_art());
			featureFragment.singerName.setText(musicList.get(index)
					.getArtistName());
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		LogUtil.v("MainActivity", "onServiceDisconnected");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		pauseRoateCDView();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (valueAnimator != null) {
			try {
				if (mPlayer.isPlaying()) {
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							startRoateCDView();
						}
					}, 500);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(this);
		stopUpdatePlayingProgress();
		pauseRoateCDView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP: // 拦截音量+按键--->避免出现系统的音量改变对话框
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN: // 拦截音量-按键--->避免出现系统的音量改变对话框
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, final int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		if (!isAllowChangeVoice)
			return; // 还不允许改变系统音量
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isAllowChangeVoice = false;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						progress / 10, AudioManager.FLAG_PLAY_SOUND);
				isAllowChangeVoice = true;
			}
		});
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		isAllowChangeVoice = true;
		isUserChangingVoice = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		isUserChangingVoice = false;
	}

	class MyVolumeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 如果音量发生变化则更改seekbar的位置
			if (intent.getAction()
					.equals("android.media.VOLUME_CHANGED_ACTION")) {
				int currVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);

				// 当前的媒体音量
				if (!isUserChangingVoice) {
					seekbar_voice.setProgress(currVolume * 10); // 改变seekbar的位置
				}
			}
		}
	}

	@Override
	public void onDrag(int progress) {
		// TODO Auto-generated method stub
		mPlayer.seekTo(progress);
		LogUtil.v("MusicPlayerService", "public void onDrag(int progress)");
	}

	@Override
	public void onClick(int progress) {
		// TODO Auto-generated method stub
		mPlayer.seekTo(progress);
		LogUtil.v("MusicPlayerService", "public void onClick(int progress)");
	}
	
	public boolean isScreenOriatationPortrait() {
		 return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		 }
}
