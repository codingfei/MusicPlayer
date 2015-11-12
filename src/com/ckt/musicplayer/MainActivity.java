package com.ckt.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ckt.modle.LogUtil;
import com.ckt.utils.MusicPlayerService;

public class MainActivity extends Activity implements View.OnClickListener,ServiceConnection{
//列表按钮
	private ImageButton list_but = null;
//	播放按钮
	private ImageButton play_but =null;
//	cd与中间镂空控件
	private ImageView cd_view = null;
	private ImageView center_view =null;
	private MediaPlayer mPlayer;
	
	private MusicPlayerService.MusicPlayerBinder mBinder;
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
		center_view = (ImageView)findViewById(R.id.music_center);
		list_but = (ImageButton)findViewById(R.id.list_btn);
		play_but = (ImageButton)findViewById(R.id.play_btn);
		list_but.setOnClickListener(this);
		play_but.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
//		打开歌曲列表
			case R.id.list_btn:
				Intent intent=new Intent(MainActivity.this,ShowSongActivity.class); 
				startActivity(intent);
				break;

			case R.id.play_btn:
//				播放CD旋转动画
				 Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.wangfei);
				 cd_view.setBackground(new BitmapDrawable(getCircleBitmap(this, bit, 200)));
				 Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.my_rotate);
					LinearInterpolator lir = new LinearInterpolator();
					anim.setInterpolator(lir);
					cd_view.startAnimation(anim);
					Animation anim_1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.my_rotate_2);
					LinearInterpolator lir_1 = new LinearInterpolator();
					anim.setInterpolator(lir_1);
					center_view.startAnimation(anim_1);
//					播放音乐
//					String path="/data/data/com.ckt.anothermusicplayer/music.mp3";
					//File file =new File(path, "music.mp3");
					try {
//						mPlayer.setDataSource(path);
						mPlayer = MediaPlayer.create(this, R.raw.test);
						mPlayer.prepare();				
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					if (mPlayer != null) {
						if (mPlayer.isPlaying()) {
							mPlayer.pause();
							LogUtil.v("MainActivity", "pause");
						}else {
							mPlayer.start();
							LogUtil.v("MainActivity", "start");
						}
					}else {
						LogUtil.v("MainActivity", "MusicPlayerIsNull");
					}
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
		LogUtil.v("MainActivity", "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		LogUtil.v("MainActivity", "onServiceDisconnected");
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
}
