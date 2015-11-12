package com.ckt.musicplayer;


import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.Activity;

import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.modle.Mp3Info;
import com.ckt.utils.HeadsetPlugReceiver;
import com.ckt.utils.Mp3FileUtil;
import com.ckt.utils.SensorUtils;

public class ShowSongActivity extends Activity implements OnItemClickListener{

	private ArrayList<Mp3Info> musicList;  //音乐列表
	
	private ListView listView;
	private MyListViewAdapter adapter;
	
	private SensorBroadcastReciver sensorBroadcastReciver;
	private HeadsetPlugReceiver headsetPlugReceiver; 
	private MediaPlayer mp =null;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.music_listview);

		//查找组件
		listView = (ListView) findViewById(R.id.listView_music);
		
		//读取音乐列表---->需要权限:<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
		//其实这里应该开个线程的
		this.musicList = Mp3FileUtil.getMp3InfoList(this);
		System.out.println("音乐个数:"+this.musicList.size());
		
		//绑定listview显示的数据,及设置item点击的监听
		adapter = new MyListViewAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		//动态注册 
		//接收摇一摇广播的接收器---->动态是因为程序退出后不想继续接收这个广播
		sensorBroadcastReciver = new SensorBroadcastReciver();
		registerReceiver(sensorBroadcastReciver, new IntentFilter(SensorUtils.BROADCAST_ACTION));
		registerHeadsetPlugReceiver();
		//开启摇一摇功能:
		SensorUtils.setAccelerateSensor(this);
	}
	
	//listview item点击的响应事件
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		Mp3Info tempMp3Info = musicList.get(position);
		Toast.makeText(this, "你想要播放:"+tempMp3Info.getName()+"?\r\n\r\nno way!!!", Toast.LENGTH_SHORT).show();

		registerHeadsetPlugReceiver();  

	}
	
	  private void registerHeadsetPlugReceiver(){  
	        headsetPlugReceiver  = new HeadsetPlugReceiver ();  
	        IntentFilter  filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);  
	        filter.addAction("android.intent.action.HEADSET_PLUG");
	        registerReceiver(headsetPlugReceiver, filter);  
	    }  


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//取消掉动态注册的广播接收器,不然会报错的哦
		unregisterReceiver(sensorBroadcastReciver);
		unregisterReceiver(headsetPlugReceiver);
		//取消摇一摇功能
		SensorUtils.cancelSensor();
		super.onDestroy();
	}
	
	/**显示歌曲的列表的适配器
	 * @author JonsonMarxy
	 *
	 */
	class MyListViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return musicList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return musicList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		/**用来保存view中的组件,下次要用时就不用再findViewById了
		 * @author JonsonMarxy
		 *
		 */
		class ViewHolder {
			TextView txt_musicName;  //歌曲名 
 			TextView txt_artistName; //歌手名
			TextView txt_during;	 //歌曲时长
		}
		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(view == null) {
				view = getLayoutInflater().inflate(R.layout.music_listview_list_item, null);
				
				//找组建
				holder = new ViewHolder();
				holder.txt_musicName = (TextView) view.findViewById(R.id.txt_mp3_name);
				holder.txt_artistName = (TextView) view.findViewById(R.id.txt_aritist);
				holder.txt_during = (TextView) view.findViewById(R.id.txt_during);
				view.setTag(holder);
			}
			holder = (ViewHolder) view.getTag();
			Mp3Info tempMp3 = musicList.get(position);
			holder.txt_musicName.setText(tempMp3.getName());
			holder.txt_artistName.setText(tempMp3.getArtistName());
			holder.txt_during.setText(Mp3FileUtil.getDuringString(tempMp3.getDuring()));
			return view;
		}
	}
	
	/**接收摇一摇广播的  接收器
	 * @author JonsonMarxy
	 *
	 */
	class SensorBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//接收到了摇一摇广播,在这里做要做的事----->下一首
			Toast.makeText(ShowSongActivity.this, "摇一摇!!!1", Toast.LENGTH_SHORT).show();
		}
		
	}

	private void unregisterReceiver(){  
	    this.unregisterReceiver(headsetPlugReceiver);  
	}  

}
