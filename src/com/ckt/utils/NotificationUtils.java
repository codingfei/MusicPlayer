package com.ckt.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

import com.ckt.modle.Mp3Info;
import com.ckt.musicplayer.R;



@SuppressLint("NewApi") 
public class NotificationUtils {
	
	public static final int NOTIFICATION_LAST=901;
	public static final int NOTIFICATION_PLAY_OR_PAUSE=902;
	public static final int NOTIFICATION_NEXT=903;
	public static final int NOTIFICATION_CANEL=904;//点击了通知栏的  x
	
	private static Notificationreciver notificationreciver=null;
	
	public static final String Broadcast_INTENT_ACTION = "tongzhilanxiangying";
	public static final String Broadcast_INTENT_EXTRA_DATA = "data"; 
	public static NotificationManager mNotificationManager = null;
	
	/**显示通知栏
	 * @param context
	 * @param isPlay  当前是否在播放音乐
	 * @param mp3Info 当前播放的歌曲
	 */
	public static void show(Context context, boolean isPlay, Mp3Info mp3Info) {
		
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(notificationreciver==null)	notificationreciver = new Notificationreciver();
		
		//自定义界面   
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification_view); 
        //创建通知
       Notification notification = new Notification.Builder(context).setTicker("正在播放音乐").setContent(rv)
				.setSmallIcon(R.drawable.logo).setAutoCancel(true).build();
       
        notification.flags=Notification.FLAG_NO_CLEAR;
        //设置视图
		if(isPlay) 	
			rv.setImageViewResource(R.id.playbtn, R.drawable.notification_music_play);
		else		
			rv.setImageViewResource(R.id.playbtn, R.drawable.notification_music_pause);
		String info = mp3Info.getName() + "-" + mp3Info.getArtistName();
		String ss [] = info.split("-");
		try {
			rv.setTextViewText(R.id.textTitle, ss[0]);
			rv.setTextViewText(R.id.textArtist, ss[1]);
		} catch (Exception e) {
			// TODO: handle exception
			rv.setTextViewText(R.id.textTitle, mp3Info.getName());
			rv.setTextViewText(R.id.textArtist, mp3Info.getArtistName());
		}
        
		//注册通知栏按钮的点击监听
		Intent btnit = new Intent("asd");
		btnit.putExtra("Infor", 1);
		PendingIntent pt1 = PendingIntent.getBroadcast(context, 1, btnit,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.previousbtn, pt1);

		Intent btnit1 = new Intent("asd");
		btnit1.putExtra("Infor", 2);
		PendingIntent pt2 = PendingIntent.getBroadcast(context, 2, btnit1,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.playbtn, pt2);

		Intent btnit2 = new Intent("asd");
		btnit2.putExtra("Infor", 3);
		PendingIntent pt3 = PendingIntent.getBroadcast(context, 3, btnit2,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.nextbtn, pt3);
		
		
		//点击通知栏---->Activity
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.ckt.musicplayer", "com.ckt.musicplayer.MainActivity"));
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
					);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 4,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.lin_notification, pendingIntent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IntentFilter ifr = new IntentFilter("asd");
		context.registerReceiver(notificationreciver, ifr);

        //把定义的notification 传递给 notificationmanager 显示通知  
        mNotificationManager.notify(1008611, notification);
	}
	
	/**取消通知栏按钮的监听
	 * @param context
	 */
	public static void release(Context context) {
		if(notificationreciver!=null)
			context.unregisterReceiver(notificationreciver);
	}
	
	/**关闭通知栏
	 * @param context
	 */
	public static void close(Context context) {
		release(context);
		mNotificationManager.cancel(1008611);
		
	}
	 
	/**接收按钮的点击事件,然后向service发送一个新的广播   ---> 这个广播的接收器,我写在service里面了
	 * @author JonsonMarxy
	 *
	 */
	static class Notificationreciver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int s = intent.getIntExtra("Infor", 0);
			//向service发送广播
			Intent it = new Intent(Broadcast_INTENT_ACTION);
			switch (s) {  //下面用到的一些常量都是在本类开头定义的,可以自行修改
			case 1: //last
				it.putExtra(Broadcast_INTENT_EXTRA_DATA, NOTIFICATION_LAST);
				break;
			case 2: //play or pause
				it.putExtra(Broadcast_INTENT_EXTRA_DATA, NOTIFICATION_PLAY_OR_PAUSE);
				break;
			case 3:  //next
				it.putExtra(Broadcast_INTENT_EXTRA_DATA, NOTIFICATION_NEXT);
				break;
				
			default:
				break;
			}
			context.sendBroadcast(it);
		}
	}
}
