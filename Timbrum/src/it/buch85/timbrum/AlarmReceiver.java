package it.buch85.timbrum;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver{

	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(MainActivity.NOTIFY_END_OF_WORK.equals(intent.getAction())){
			//String title=intent.getStringExtra(name)
			Intent resultIntent = new Intent(context, MainActivity.class);
			// Because clicking the notification opens a new ("special") activity, there's
			// no need to create an artificial back stack.
			PendingIntent resultPendingIntent = PendingIntent.getActivity(context,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(context.getString(R.string.end_of_work_title))
			.setContentText(context.getString(R.string.end_of_work_message))
			.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        mNotificationManager.notify(1, mBuilder.build());
		}
		
	}

}
