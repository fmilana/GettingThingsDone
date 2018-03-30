package com.gettingthingsdone.federico.gettingthingsdone.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by feder on 26-Mar-18.
 */

public class TagsNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("RECEIVING TAG REMINDER!");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainFragmentActivity.class);
        notificationIntent.putExtra("fragmentToLaunch", intent.getStringExtra("list name"));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel tagsNotificationChannel = new NotificationChannel(MainFragmentActivity.TAGS_NOTIFICATIONS_CHANNEL,
                    context.getResources().getString(R.string.tags_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            tagsNotificationChannel.setLightColor(context.getColor(R.color.colorAccent));

            notificationManager.createNotificationChannel(tagsNotificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainFragmentActivity.TAGS_NOTIFICATIONS_CHANNEL).setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(context.getResources().getString(R.string.tags_notification_title))
                .setContentText(intent.getStringExtra("item name"))
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }
}
