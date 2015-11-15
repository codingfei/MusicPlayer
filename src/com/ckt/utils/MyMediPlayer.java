package com.ckt.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

public class MyMediPlayer extends MediaPlayer{
	public static final int TIME = 1000; //声音渐变的总时间
	public static final int DELAY = 50; //声音渐变的间隔时间
	private Handler handler;
	private Runnable runnable;
	private boolean isPlaying;
	
	AudioManager mAudioManager;
	int maxVoice;
	int curVoice;
	float volume; //实际的播放音量
	float dv; //每次改变的音量
	
	int flag; //标志---1--pause  2---stop
	
	
	public MyMediPlayer(Context context) {
		
		isPlaying = false;
		handler = new Handler();
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		maxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		curVoice = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
		
		volume = 0;
		setVolume(volume/maxVoice, volume/maxVoice);
		
		runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				curVoice = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
//				setVolume(volume/maxVoice, volume/maxVoice);
				setVolume(volume/curVoice, volume/curVoice);
				volume += dv;
				if(volume<0) {
					volume = 0;
					if(flag == 1) {
						MyMediPlayer.super.pause();
					}else if(flag == 2) {
						MyMediPlayer.super.stop();
					}
				}else if(volume>curVoice) {
					volume = curVoice;
				}else {
					handler.postDelayed(runnable, DELAY);
				}
			}
		};
	}
	public boolean isPlaying() {
		return this.isPlaying;
	}
	
	@Override
	public void start() throws IllegalStateException {
		// TODO Auto-generated method stub
		super.start();
		isPlaying = true;
		curVoice = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
		//音量改变次数 = TIME/DELAY;
		dv =(float)curVoice/(TIME/DELAY);
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(runnable, DELAY);
	}
	
	@Override
	public void pause() throws IllegalStateException {
		// TODO Auto-generated method stub
		isPlaying = false;
		curVoice = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
		//音量改变次数 = TIME/DELAY;
		dv =(float)curVoice/(TIME/DELAY);
		dv *= -1;//负数表示 降音
		flag = 1;
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(runnable, DELAY);
	}
	
	@Override
	public void stop() throws IllegalStateException {
		// TODO Auto-generated method stub
		isPlaying = false;
		curVoice = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
		//音量改变次数 = TIME/DELAY;
		dv =(float)curVoice/(TIME/DELAY);
		dv *= -1;//负数表示 降音
		flag = 2;
		handler.removeCallbacksAndMessages(null);
		handler.postDelayed(runnable, DELAY);
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		isPlaying = false;
		super.reset();
	}
	
	@Override
	public void release() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		super.release();
	}
}
