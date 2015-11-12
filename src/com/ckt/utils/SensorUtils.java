package com.ckt.utils;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * 设置加速传感器的工具类
 * 
 * @author JonsonMarxy
 * 
 */
public class SensorUtils {
	public static final int SHAKED = 100;
	private static Sensor accelerateSensor;
	private static SensorManager sensorManager;
	private static MySensorListener listener;
	private static Context context;
	
	public static final String BROADCAST_ACTION = "yaoyiyao"; //监听到摇一摇时,发送的广播的action
	
	/**
	 * 取消传感器的监听
	 */
	public static void cancelSensor() {
		try {
			sensorManager.unregisterListener(listener);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * 设置 加速传感器及其监听
	 * 
	 * @param context
	 * @param handler
	 */
	public static boolean setAccelerateSensor(Context context) {
		SensorUtils.context = context;
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 获取加速传感器
		if (accelerateSensor == null)
			return false;
		listener = new MySensorListener();
		return sensorManager.registerListener(listener,
				accelerateSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	
	static class MySensorListener implements SensorEventListener {
		private boolean isAllowShake = true;
		/**设置触发间隔,防止要一次触发多次
		 * @author JonsonMarxy
		 *
		 */
		class MyThread extends Thread {
			public void run() {
				isAllowShake = false;
				try {
					sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isAllowShake = true;
			};
		};

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
	
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			int sensorType = event.sensor.getType();
			// values[0]:X轴，values[1]：Y轴，values[2]：Z轴
			float[] values = event.values;

			float x = values[0];
			float y = values[1];
			float z = values[2];
			
			if(isAllowShake == false) return;
			
			if (sensorType == Sensor.TYPE_ACCELEROMETER) {
				int value = 15;// 摇一摇阀值,不同手机能达到的最大值不同,如某品牌手机只能达到20
				if (x >= value || x <= -value || y >= value || y <= -value
						|| z >= value || z <= -value) {
					System.out.println("监测到摇一摇了!!!!");
					
					// 发送广播,通知service换歌
					Intent intent = new Intent(BROADCAST_ACTION);
					context.sendBroadcast(intent);
					//设置摇动换歌的间隔,防止要一下换多首歌
					try {
						new MyThread().start();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
