package com.ckt.musicplayer;

import android.app.Activity;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	 HeadsetPlugReceiver headsetPlugReceiver; 
	 MediaPlayer mp =null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		registerHeadsetPlugReceiver();  
	}
	
	  private void registerHeadsetPlugReceiver(){  
	        headsetPlugReceiver  = new HeadsetPlugReceiver ();  
	        IntentFilter  filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);  
	        filter.addAction("android.intent.action.HEADSET_PLUG");
	        registerReceiver(headsetPlugReceiver, filter);  
	    }  

	  public void onClick(View v)
	  {	 mp = MediaPlayer.create(this, R.raw.test);
		  mp.start();
	  }
	@Override  
	protected void onDestroy() {  
	    // TODO Auto-generated method stub  
	    super.onDestroy();  
	    unregisterReceiver();  //×¢Ïú¼àÌý  
	}  
	private void unregisterReceiver(){  
	    this.unregisterReceiver(headsetPlugReceiver);  
	}  
}
