package com.ckt.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.modle.Mp3Info;

public class FeatureFragment extends Fragment {
	public TextView songName = null;
	public TextView cdName = null;
	public TextView singerName = null;
	public Mp3Info mp3Info = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.feature_fragment, container, false);
		songName = (TextView)view.findViewById(R.id.music_name);
		cdName = (TextView)view.findViewById(R.id.music_CD);
		singerName = (TextView)view.findViewById(R.id.music_singer);
		return view;
	}
}
