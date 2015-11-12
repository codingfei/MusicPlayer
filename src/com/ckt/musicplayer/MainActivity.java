package com.ckt.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener{

	private ImageButton list_but = null;
	private ImageButton play_but =null;
	private ImageView cd_view = null;
	private ImageView center_view =null;
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		cd_view = (ImageView)findViewById(R.id.CD_img);
		center_view = (ImageView)findViewById(R.id.music_center);
		list_but = (ImageButton)findViewById(R.id.list_btn);
		play_but = (ImageButton)findViewById(R.id.play_btn);
		list_but.setOnClickListener(this);
		play_but.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.list_btn:
				Intent intent=new Intent(MainActivity.this,ShowSongActivity.class); 
				startActivity(intent);
				break;
			case R.id.play_btn:
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
}
