package com.ckt.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class HeadsetPlugReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.hasExtra("state")){  
            if(intent.getIntExtra("state", 0)==0){  
                Toast.makeText(context, "headset not connected", Toast.LENGTH_LONG).show();  
            }  
            else if(intent.getIntExtra("state", 0)==1){  
                Toast.makeText(context, "headset  connected", Toast.LENGTH_LONG).show();  
            }
        }  
	}

}
