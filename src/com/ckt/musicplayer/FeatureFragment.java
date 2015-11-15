package com.ckt.musicplayer;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ckt.modle.Mp3Info;

public class FeatureFragment extends Fragment{
	public TextView songName = null;
	public TextView cdName = null;
	public TextView singerName = null;
	private ImageButton imageButton = null;
	public Mp3Info mp3Info = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.feature_fragment, container, false);
		songName = (TextView)view.findViewById(R.id.music_name);
		cdName = (TextView)view.findViewById(R.id.music_CD);
		singerName = (TextView)view.findViewById(R.id.music_singer);
		imageButton = (ImageButton)view.findViewById(R.id.menu_btn);
		imageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(getActivity()).setTitle("Music").setMessage("你确定要退出这牛逼哄哄的软件-.-").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						// 关闭MainActivity
						getActivity().finish();
						// 终止服务
						Intent stopServiceIntent = new Intent(getActivity(),
								MusicPlayerService.class);
						getActivity().stopService(stopServiceIntent);
					}
				}).setNegativeButton("NO", null).show();
			}
		});
		return view;
	}
}
