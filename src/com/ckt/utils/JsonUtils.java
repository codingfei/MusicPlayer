package com.ckt.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ckt.modle.Mp3Info;


public class JsonUtils {

	//将歌曲列表转化为json字符串
	public static String changeListToJsonObj(List<Mp3Info> list) {
		
		JSONArray array = new JSONArray();
		for(int i=0; i<list.size(); i++) {
			Mp3Info mp3Info = list.get(i);
			JSONObject obj = new JSONObject();
			try {
				obj.put("song_id", mp3Info.getSong_id());
				obj.put("album_id", mp3Info.getAlbum_id());
				obj.put("album_art", mp3Info.getAlbum_art());
				obj.put("title", mp3Info.getName());
				obj.put("artistName", mp3Info.getArtistName());
				obj.put("size", mp3Info.getSize());
				obj.put("during", mp3Info.getDuring());
				obj.put("path", mp3Info.getPath());
				array.put(i, obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return array.toString();
		
	}

	//解析json为歌曲列表
	public static ArrayList<Mp3Info> resolveJsonToList(String json) {
		
		ArrayList<Mp3Info> list = new ArrayList<Mp3Info>();
		if("".equals(json))	return list;
		try {
			JSONArray array = new JSONArray(json);
			for(int i=0; i<array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				list.add(new Mp3Info(
						obj.getLong("song_id"),
						obj.getLong("album_id"),
						obj.getString("album_art"),
						obj.getString("title"),
						obj.getString("artistName"),
						obj.getDouble("size"),
						obj.getLong("during"),
						obj.getString("path")
				));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
}
