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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by feder on 20-Mar-18.
 */

public class InTrayReminderReceiver extends BroadcastReceiver {

    final DatabaseReference databaseReference = LogInActivity.databaseReference;
    final FirebaseAuth firebaseAuth = LogInActivity.firebaseAuth;

    @Override
    public void onReceive(final Context context, Intent intent) {

        System.out.println("RECEIVING INTRAY REMINDER!");

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersEnabled").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean enabled = (boolean) dataSnapshot.getValue();

                if (enabled) {

                    DatabaseReference inTrayReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray");

                    if (inTrayReference != null) {
                        inTrayReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long numberOfInTrayItems = dataSnapshot.getChildrenCount();

                                if (numberOfInTrayItems > 0) {
                                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                    Intent notificationIntent = new Intent(context, MainFragmentActivity.class);
                                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel inTrayClarificationChannel = new NotificationChannel(MainFragmentActivity.INTRAY_REMINDERS_CHANNEL,
                                                context.getResources().getString(R.string.in_tray_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                                        inTrayClarificationChannel.setLightColor(context.getColor(R.color.colorAccent));

                                        notificationManager.createNotificationChannel(inTrayClarificationChannel);

                                    }

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainFragmentActivity.INTRAY_REMINDERS_CHANNEL).setContentIntent(pendingIntent)
                                            .setSmallIcon(R.drawable.ic_notification_icon)
                                            .setContentTitle(context.getResources().getString(R.string.in_tray_notification_title))
                                            .setContentText("You have " + numberOfInTrayItems + " Items to Clarify")
                                            .setAutoCancel(true);

                                    notificationManager.notify(0, builder.build());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    System.out.println("WOULD SEND NOTIFICATION, BUT DISABLED!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}


