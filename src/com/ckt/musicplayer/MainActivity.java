package com.ckt.musicplayer;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;
import com.ckt.ui.CustomProgressBar;
import com.ckt.utils.JsonUtils;

public class MainActivity extends Activity implements View.OnClickListener,ServiceConnection,OnSeekBarChangeListener{
//列表按钮
	private ImageButton list_but = null;
//	播放按钮
	private ImageButton play_but =null;
	
	private ImageButton next_but;

	private ImageButton pre_but;

	private ImageButton menu_but;
//	cd与中间镂空控件
	private ImageView cd_view = null;
	private ImageView center_view =null;
	private MediaPlayer mPlayer;
//	歌曲进度显示控件
	private CustomProgressBar circle_progress = null;
	private ProgressBar sound_progress = null;
	private MusicPlayerService.MusicPlayerBinder mBinder;  
    private MediaPlayer mediaPlayer = null;// 播放器
    
    private SeekBar seekbar_voice;
	private boolean isAllowChangeVoice=false;  //设置拖动seekbar时的互斥量
	private boolean isUserChangingVoice=false;  //表示当前是否是我们自己在改变音量--->这个时候就不要理会系统音量改变的广播了,不然会出现一些小问题
    private AudioManager mAudioManager = null; // Audio管理器，用了控制音量
    
    private ArrayList<Mp3Info> musicList; // 音乐列表
    private Mp3Info currentSong;
    private ValueAnimator valueAnimator=null;
    float value=0;
    private Handler handler = new Handler();
    private int state = 1;
    
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		Intent startServiceIntent = new Intent(this,MusicPlayerService.class);
		startService(startServiceIntent);
		Intent bindIntent = new Intent(this,MusicPlayerService.class);
		bindService(bindIntent,this, BIND_AUTO_CREATE);
		
		cd_view = (ImageView)findViewById(R.id.CD_img);
		circle_progress = (CustomProgressBar)findViewById(R.id.circle_pro);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//		sound_progress = (ProgressBar)findViewById(R.id.sound_progress);
		center_view = (ImageView)findViewById(R.id.music_center);
		//添加主界面布局控件的点击响应
		//---播放/暂停按钮---//
		play_but = (ImageButton)findViewById(R.id.play_btn);
		//---下一首歌按钮---//
		next_but = (ImageButton)findViewById(R.id.next_btn);
		next_but.setOnClickListener(this);
		//---上一首歌按钮---//
		pre_but = (ImageButton)findViewById(R.id.pre_btn);
		pre_but.setOnClickListener(this);
		// ---歌曲列表按钮---//
		list_but = (ImageButton)findViewById(R.id.list_btn);
		list_but.setOnClickListener(this);
		// ---关闭按钮---//
		menu_but = (ImageButton)findViewById(R.id.menu_btn);
		menu_but.setOnClickListener(this);
		seekbar_voice = (SeekBar) findViewById(R.id.voice_seekbar);
		play_but.setOnClickListener(this);
		Bitmap bit = BitmapFactory.decodeResource(getResources(),
				R.drawable.wangfei);
		cd_view.setBackground(new BitmapDrawable(getCircleBitmap(this, bit,
				200)));
		
		//初始化voice_seekbar:
				seekbar_voice.setOnSeekBarChangeListener(this);
				int maxV = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
				int currentV = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
				seekbar_voice.setMax(maxV*10); //为什么要放大十倍???---->因为max值一般是10多,太小了,拖动seekbar的时候明显感觉不爽
				seekbar_voice.setProgress(currentV*10);
				
				//注册监听--->监听系统音量的改变
				IntentFilter filter = new IntentFilter() ;
		        filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;
		        registerReceiver(new MyVolumeReceiver(), filter) ;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ShowSongActivity.RESULT_CODE) { // 返回了点击的歌曲的index
			int index = data.getIntExtra("position", -1);
			currentSong = musicList.get(0);
			if (index >= 0) {// 这里面做相关的响应---->播放第index首歌
				LogUtil.v("MusicPlayerService", "用户点击播放:" + index);
				currentSong = musicList.get(index);
			}
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// 打开歌曲列表
		case R.id.list_btn:
			Intent intent = new Intent(MainActivity.this,
					ShowSongActivity.class);
			intent.putExtra("list", JsonUtils.changeListToJsonObj(musicList)); //启动Activity并把歌曲列表传过去
			startActivityForResult(intent, 1111);  //这里要用startActivityForResult
			break;

		case R.id.play_btn:
			// 播放CD旋转动画
			
			/*Animation anim = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.my_rotate);
			LinearInterpolator lir = new LinearInterpolator();
			anim.setInterpolator(lir);
			cd_view.startAnimation(anim);
			Animation anim_1 = AnimationUtils.loadAnimation(MainActivity.this,
					R.anim.my_rotate_2);
			LinearInterpolator lir_1 = new LinearInterpolator();
			anim.setInterpolator(lir_1);
			center_view.startAnimation(anim_1);*/
			if(valueAnimator == null) {
				valueAnimator = ValueAnimator.ofFloat(360);
				LinearInterpolator lir = new LinearInterpolator();
				valueAnimator.setInterpolator(lir);
				valueAnimator.setRepeatCount(-1);
		        valueAnimator.setDuration(20000).addUpdateListener(new AnimatorUpdateListener() {
					
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						cd_view.setRotation((Float) animation.getAnimatedValue()+value);	
					}
				});
			}
	     
			Mp3Info mp3 = (Mp3Info) mBinder.getMusicList().toArray()[0];
			mBinder.playOrPauseMusic(mPlayer, mp3);
			circle_progress.start();
			// 播放音乐
			try {
				int state = mBinder.getPlayerState();
				if (state == 0) { 
					valueAnimator.start();
					state = 1;
					Log.d("test", state+"");
				} else if (state == 1) {
					valueAnimator.cancel();
					value = cd_view.getRotation();
					state = 2;
					Log.d("test", state+"");
				} else if(state == 2){ 
					valueAnimator.start();
					state = 1;
					Log.d("test", state+"");
				}
				mBinder.setPlayerState(state);
				Log.d("test", mBinder+"-----"+state+"");
			} catch (Exception e) {
			}
			
			break;
		case R.id.next_btn:
			mBinder.playNextMusic(MusicPlayerService.PLAYORDER_ORDER);
			break;
		case R.id.pre_btn:
			mBinder.playPreviousMusic(MusicPlayerService.PLAYORDER_ORDER);
			break;
		case R.id.menu_btn:
			//关闭MainActivity
			Toast.makeText(this, "关闭", Toast.LENGTH_SHORT).show();
			finish();
			//终止服务
			Intent stopServiceIntent = new Intent(this,MusicPlayerService.class);
			stopService(stopServiceIntent);
			
			break;
		default:
			break;
		}
	}

	public static Bitmap getCircleBitmap(Context context, Bitmap src, float radius) {  
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
    	return  (dpValue * scale + 0.5f);
    }
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mBinder = (MusicPlayerService.MusicPlayerBinder)service;
		mPlayer = mBinder.getMusicPlayer();
		musicList = (ArrayList<Mp3Info>) mBinder.getMusicList();
		LogUtil.v("MainActivity", "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		LogUtil.v("MainActivity", "onServiceDisconnected");
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(valueAnimator!=null) {
			valueAnimator.cancel();
			value = cd_view.getRotation();
		}
		super.onPause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(valueAnimator != null) {
			if(mPlayer.isPlaying()) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						valueAnimator.start();
					}
				}, 500);
			}
		}
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(this);
//		Intent stopServiceIntent = new Intent(this,MusicPlayerService.class);
//		stopService(stopServiceIntent);
		LogUtil.v("MainActivity", "onDestory");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:  //拦截音量+按键--->避免出现系统的音量改变对话框
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				     AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN: //拦截音量-按键--->避免出现系统的音量改变对话框
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
		if(!isAllowChangeVoice)return;  //还不允许改变系统音量
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				isAllowChangeVoice =false;
				mAudioManager.setStreamVolume(
						AudioManager.STREAM_MUSIC, 
						progress/10, 
						AudioManager.FLAG_PLAY_SOUND);
				/*mAudioManager.setStreamVolume(
						AudioManager.STREAM_MUSIC, 
						progress, 
						AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);*/
				
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
	class MyVolumeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
                int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ;// 当前的媒体音量
                if(!isUserChangingVoice) {
                	seekbar_voice.setProgress(currVolume*10);  //改变seekbar的位置
                }
            }
        }
    }
}
