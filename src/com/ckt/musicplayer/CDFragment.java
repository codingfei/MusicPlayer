package com.ckt.musicplayer;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;

public class CDFragment extends Fragment {
	public ImageView imageView = null;
	View view = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.cd_fragment, container, false);	
		imageView = (ImageView)view.findViewById(R.id.CD_img);
		return view;
	}

	public boolean isScreenOriatationPortrait() {
		 return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		 }
}
