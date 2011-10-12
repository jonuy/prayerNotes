package com.jdroyd.prayerNotes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PNAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if( context != null ) {
			 	Context ctx = context.getApplicationContext();
				NotificationManager nm = (NotificationManager)
					ctx.getSystemService(Context.NOTIFICATION_SERVICE);
				
				if( nm != null ) {
					int icon = R.drawable.img_gallery;
					CharSequence tickerText = ctx.getResources().
						getText(R.string.ticker_title).toString();
					
					// With API 11, should use Notification.Builder instead
					Notification notification = new Notification(icon, 
							tickerText, 
							System.currentTimeMillis());
					
					Bundle bundle = intent.getExtras();
					Long position = bundle.getLong(PNDbAdapter.PNKEY_ROWID);
					
					//TODO: get "content_text" statically defined somewhere
					CharSequence contentText = bundle.getCharSequence("content_text");
					CharSequence contentTitle = tickerText;
					
					Log.v("ALARM", "Recv position: "+position);
					Intent notificationIntent = new Intent(ctx, PNEditNote.class);
					notificationIntent.putExtra(PNDbAdapter.PNKEY_ROWID, position);
					PendingIntent contentIntent = PendingIntent.getActivity(
							ctx, 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

					notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
					
					nm.notify(1, notification);
				}
				else
					Log.v("ERROR", "Failed to create NotificationManager");
			}
			else
				Log.v("ERROR", "Alarm received with a null context");
		}
		catch( Exception e ) {
			Log.v("ERROR", "Error received in PNAlarmReceiver: "+e);
		}
	}

}
