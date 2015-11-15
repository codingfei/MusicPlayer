package com.ckt.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LuncherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.luncher);
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
			    Intent intent = new Intent(LuncherActivity.this,MainActivity.class);
			    startActivity(intent);
			    LuncherActivity.this.finish();
			}

			}, 1000);
	}
}
