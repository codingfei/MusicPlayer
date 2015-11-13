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
import com.ckt.utils.JsonUtils;
import com.ckt.utils.Mp3FileUtil;
import com.ckt.utils.SensorUtils;

public class ShowSongActivity extends Activity implements OnItemClickListener{

	private ArrayList<Mp3Info> musicList;  //�����б�
	
	private ListView listView;
	private MyListViewAdapter adapter;
	
	private SensorBroadcastReciver sensorBroadcastReciver;
	private HeadsetPlugReceiver headsetPlugReceiver; 
	private MediaPlayer mp =null;

	public static final int RESULT_CODE = 10086;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.music_listview);

		//�������
		listView = (ListView) findViewById(R.id.listView_music);
		
		//��ȡ�����б�
		Intent it = getIntent();
		if(it!=null) {
			musicList = JsonUtils.resolveJsonToList(it.getStringExtra("list"));
		}else {
			musicList = new ArrayList<Mp3Info>();
		}
		

		System.out.println("���ָ���:"+this.musicList.size());
		
		//��listview��ʾ������,������item����ļ���
		adapter = new MyListViewAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		//��̬ע�� 
		//����ҡһҡ�㲥�Ľ�����---->��̬����Ϊ�����˳����������������㲥
		sensorBroadcastReciver = new SensorBroadcastReciver();
		registerReceiver(sensorBroadcastReciver, new IntentFilter(SensorUtils.BROADCAST_ACTION));
		registerHeadsetPlugReceiver();
		//����ҡһҡ����:
		SensorUtils.setAccelerateSensor(this);
	}
	
	//listview item�������Ӧ�¼�
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		Mp3Info tempMp3Info = musicList.get(position);
		Toast.makeText(this, "����Ҫ����:"+tempMp3Info.getName()+"?\r\n\r\nno way!!!"+position, Toast.LENGTH_SHORT).show();
		
		registerHeadsetPlugReceiver();  //����ʲô?????
		
		Intent data = new Intent();
		data.putExtra("position", position);
		setResult(RESULT_CODE, data);  //��MainActivity���onActivityResult()�������
		finish();
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
		//ȡ������̬ע��Ĺ㲥������,��Ȼ�ᱨ���Ŷ
		unregisterReceiver(sensorBroadcastReciver);
		unregisterReceiver(headsetPlugReceiver);
		//ȡ��ҡһҡ����
		SensorUtils.cancelSensor();
		super.onDestroy();
	}
	
	/**��ʾ�������б��������
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
		/**��������view�е����,�´�Ҫ��ʱ�Ͳ�����findViewById��
		 * @author JonsonMarxy
		 *
		 */
		class ViewHolder {
			TextView txt_musicName;  //������ 
 			TextView txt_artistName; //������
			TextView txt_during;	 //����ʱ��
		}
		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(view == null) {
				view = getLayoutInflater().inflate(R.layout.music_listview_list_item, null);
				
				//���齨
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
	
	/**����ҡһҡ�㲥��  ������
	 * @author JonsonMarxy
	 *
	 */
	class SensorBroadcastReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//���յ���ҡһҡ�㲥,��������Ҫ������----->��һ��
			Toast.makeText(ShowSongActivity.this, "ҡһҡ!!!1", Toast.LENGTH_SHORT).show();
		}
		
	}

	private void unregisterReceiver(){  
	    this.unregisterReceiver(headsetPlugReceiver);  
	}  

}
