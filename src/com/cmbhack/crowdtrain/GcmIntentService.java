package com.cmbhack.crowdtrain;

import java.util.Date;
import java.util.Set;

import com.cmbhack.crowdtrain.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService { 
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}
 
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (true) {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
			String messageType = gcm.getMessageType(intent);

			if (!extras.isEmpty()) {
				if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
						.equals(messageType)) {
					sendNotification(GCMData.parse(extras));
				}
			}
			GcmBroadcastReceiver.completeWakefulIntent(intent);
		}
	}
 
	private void sendNotification(GCMData data) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("data", data.getDataBundle());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,0);
		int notificationId = Integer.parseInt(data.getTrainID())
				+ (new Date().getSeconds());
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(data.getName())
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(data
								.getName()))
				.setContentText(
						"Train "
								+ (data.getType().equalsIgnoreCase("delayed") ? "Delayed "
										+ (data.getDelayTime() != 0 ? data
												.getDelayTime() + "mins" : "")
										: "Cancelled"));

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(notificationId, mBuilder.build());
	}
}