package com.cmbhack.crowdtrain;

import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent; 
import android.util.Log;

public class GcmBroadcastReceiver extends android.support.v4.content.WakefulBroadcastReceiver {
	@Override
    public void onReceive(Context context, Intent intent) {
		Log.d("GCM","GCM recieved"); 
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName()); 
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
