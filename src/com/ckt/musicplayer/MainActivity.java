package com.ckt.musicplayer;

import java.util.ArrayList;

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
import android.media.AudioManager;
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
import android.widget.ProgressBar;

import com.ckt.modle.LogUtil;
import com.ckt.modle.Mp3Info;
import com.ckt.ui.CustomProgressBar;
import com.ckt.utils.JsonUtils;
import com.ckt.utils.MusicPlayerService;

public class MainActivity extends Activity implements View.OnClickListener,ServiceConnection{
//�б�ť
	private ImageButton list_but = null;
//	���Ű�ť
	private ImageButton play_but =null;
//	cd���м��οտؼ�
	private ImageView cd_view = null;
	private ImageView center_view =null;
	private MediaPlayer mPlayer;
//	����������ʾ�ؼ�
	private CustomProgressBar circle_progress = null;
	private ProgressBar sound_progress = null;
	private MusicPlayerService.MusicPlayerBinder mBinder;  
    private MediaPlayer mediaPlayer = null;// ������       
    private AudioManager audioMgr = null; // Audio�����������˿�������
    private ArrayList<Mp3Info> musicList; // �����б�
    private Mp3Info currentSong;
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

//		sound_progress = (ProgressBar)findViewById(R.id.sound_progress);
		center_view = (ImageView)findViewById(R.id.music_center);
		list_but = (ImageButton)findViewById(R.id.list_btn);
		play_but = (ImageButton)findViewById(R.id.play_btn);
		list_but.setOnClickListener(this);
		play_but.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ShowSongActivity.RESULT_CODE) { // �����˵���ĸ�����index
			int index = data.getIntExtra("position", -1);
			currentSong = musicList.get(0);
			if (index >= 0) {// ����������ص���Ӧ---->���ŵ�index�׸�
				LogUtil.v("MusicPlayerService", "�û��������:" + index);
				currentSong = musicList.get(index);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
//		�򿪸����б�
			case R.id.list_btn:
				Intent intent = new Intent(MainActivity.this,
				ShowSongActivity.class);
				intent.putExtra("list", JsonUtils.changeListToJsonObj(musicList)); //����Activity���Ѹ����б���ȥ
				startActivityForResult(intent, 1111);  //����Ҫ��startActivityForResult
				break;


			case R.id.play_btn:
//				����CD��ת����
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
//					��������
//					String path="/data/data/com.ckt.anothermusicplayer/music.mp3";
					//File file =new File(path, "music.mp3");
//					try {
////						mPlayer.setDataSource(path);
//						mPlayer = MediaPlayer.create(this, R.raw.test);
//						mPlayer.prepare();				
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} 
//					if (mPlayer != null) {
//						if (mPlayer.isPlaying()) {
//							mPlayer.pause();
//							LogUtil.v("MainActivity", "pause");
//						}else {
//							mPlayer.start();
//							circle_progress.start();
//							LogUtil.v("MainActivity", "start");
//						}
//					}else {
//						LogUtil.v("MainActivity", "MusicPlayerIsNull");
//					}
					try {
						int state = mBinder.getPlayerState();
						if (state == 0) { // ֹͣ ----> ����
							mPlayer.reset();
							mPlayer.setDataSource(musicList.get(0).getPath());
							mPlayer.prepare();
							mPlayer.start();
//							circle_progress.setMax((int)currentSong.getDuring());
							circle_progress.start();
							state = 1;
						} else if (state == 1) { // ����--->��ͣ
							mPlayer.pause();
							circle_progress.stop();
							state = 2;
						} else { // ��ͣ---->����
							mPlayer.start();
							circle_progress.start();
							state = 1;
						}
						mBinder.setPlayerState(state);
					} catch (Exception e) {
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
		this.musicList = mBinder.getMusicList();
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
