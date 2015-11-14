package com.ckt.utils;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * ���ü��ٴ������Ĺ�����
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
	
	public static final String BROADCAST_ACTION = "yaoyiyao"; //������ҡһҡʱ,���͵Ĺ㲥��action
	
	/**
	 * ȡ���������ļ���
	 */
	public static void cancelSensor() {
		try {
			sensorManager.unregisterListener(listener);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * ���� ���ٴ������������
	 * 
	 * @param context
	 * @param handler
	 */
	public static boolean setAccelerateSensor(Context context) {
		SensorUtils.context = context;
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // ��ȡ���ٴ�����
		if (accelerateSensor == null)
			return false;
		listener = new MySensorListener();
		return sensorManager.registerListener(listener,
				accelerateSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	
	static class MySensorListener implements SensorEventListener {
		private boolean isAllowShake = true;
		/**���ô������,��ֹҪһ�δ������
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
			// values[0]:X�ᣬvalues[1]��Y�ᣬvalues[2]��Z��
			float[] values = event.values;

			float x = values[0];
			float y = values[1];
			float z = values[2];
			
			if(isAllowShake == false) return;
			
			if (sensorType == Sensor.TYPE_ACCELEROMETER) {
				int value = 15;// ҡһҡ��ֵ,��ͬ�ֻ��ܴﵽ�����ֵ��ͬ,��ĳƷ���ֻ�ֻ�ܴﵽ20
				if (x >= value || x <= -value || y >= value || y <= -value
						|| z >= value || z <= -value) {
					System.out.println("��⵽ҡһҡ��!!!!");
					
					// ���͹㲥,֪ͨservice����
					Intent intent = new Intent(BROADCAST_ACTION);
					context.sendBroadcast(intent);
					//����ҡ������ļ��,��ֹҪһ�»����׸�
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
