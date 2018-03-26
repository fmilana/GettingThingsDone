package com.gettingthingsdone.federico.gettingthingsdone.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by feder on 23-Mar-18.
 */

public class CalendarNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        final DatabaseReference databaseReference = MainActivity.databaseReference;
        final FirebaseAuth firebaseAuth = MainActivity.firebaseAuth;

        System.out.println("RECEIVING CALENDAR NOTIFICATION!");

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsEnabled").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean enabled = (boolean) dataSnapshot.getValue();

                if (enabled) {

                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
                    String todaysDate = simpleDateFormat.format(date);

                    DatabaseReference todaysReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(todaysDate);

                    if (todaysReference != null) {

                        todaysReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                System.out.println("DATASNAPSHOT.GETCHILDRENCOUNT() = " + dataSnapshot.getChildrenCount());

                                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                Intent notificationIntent = new Intent(context, MainFragmentActivity.class);
//                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                notificationIntent.putExtra("fragmentToLaunch", "calendarFragment");

                                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel calendarNotificationChannel = new NotificationChannel(MainFragmentActivity.CALENDAR_NOTIFICATIONS_CHANNEL,
                                            context.getResources().getString(R.string.calendar_notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                                    calendarNotificationChannel.setLightColor(context.getColor(R.color.colorAccent));

                                    notificationManager.createNotificationChannel(calendarNotificationChannel);

                                }

                                final ArrayList<String> todaysItemsTexts = new ArrayList<>();


                                /////finds names of items due today/////
                                for (final DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(childDataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Item item = dataSnapshot.getValue(Item.class);

                                            todaysItemsTexts.add(item.getText());

                                            System.out.println("========================================> ADDING " + item.getText() + " TO todaysItemsTexts");

                                            String concatenatedItemTexts = "";

                                            for (int i = 0; i < todaysItemsTexts.size(); ++i) {
                                                if (concatenatedItemTexts.length() > 0) {
                                                    concatenatedItemTexts += ", ";
                                                }

                                                concatenatedItemTexts += todaysItemsTexts.get(i);
                                            }

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainFragmentActivity.CALENDAR_NOTIFICATIONS_CHANNEL).setContentIntent(pendingIntent)
                                                    .setSmallIcon(R.drawable.ic_notification_icon)
                                                    .setContentTitle("You have " + dataSnapshot.getChildrenCount() + " Items due today")
                                                    .setContentText(concatenatedItemTexts)
                                                    .setAutoCancel(true);

                                            notificationManager.notify(0, builder.build());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        System.out.println("NO ITEMS TODAY");
                    }


                } else {
                    System.out.println("WOULD SEND CALENDAR NOTIFICATION, BUT DISABLED!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
