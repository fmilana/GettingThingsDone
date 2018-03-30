package com.gettingthingsdone.federico.gettingthingsdone;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.receivers.TagsNotificationReceiver;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.location.LocationCallback;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by feder on 26-Mar-18.
 */

public class TagsNotificationManager {

    private MainFragmentActivity mainFragmentActivity;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private DatabaseReference geoFireReference;
    private GeoFire geoFire;


    private static ArrayList<ItemTagAlarmManager> itemTagAlarmManagers;


    public TagsNotificationManager(MainFragmentActivity mainFragmentActivity) {
        this.mainFragmentActivity = mainFragmentActivity;

        databaseReference = LogInActivity.databaseReference;
        firebaseAuth = LogInActivity.firebaseAuth;

        geoFireReference = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getCurrentUser().getUid() + "/taglocations");
        geoFire = new GeoFire(geoFireReference);


        itemTagAlarmManagers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(mainFragmentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mainFragmentActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MainFragmentActivity.PERMISSION_REQUEST_READ_FINE_LOCATION);

        }
    }

    public void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(60000);
        locationRequest.setInterval(300000);


        if (ContextCompat.checkSelfPermission(mainFragmentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            mainFragmentActivity.getFusedLocationClient().requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    System.out.println("INSIDE ONLOCATIONRESULT");

                    if (locationResult == null) {

                        System.out.println("INSIDE LOCATIONRESULT == NULL");

                        return;
                    }
                    for (Location location : locationResult.getLocations()) {

                        System.out.println("INSIDE FOR LOCATION:LOCATIONRESULT.GETLOCATIONS()");

                        setUpTagLocationNotifications(location);
                    }
                }

                ;
            }, null);
        }

    }

    private void setUpTagLocationNotifications(Location currentLocation) {

        System.out.println("INSIDE SETUPTAGLOCATIONNOTIFICATIONS");

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 0.2);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {

                System.out.println("ENTERED");

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 0");

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            final Tag tag = childSnapshot.getValue(Tag.class);
                            tag.setKey(childSnapshot.getKey());

                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1");

                            if (tag.getLocationKey() != null && tag.getLocationKey().equals(key)) {
                                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2");

                                            Item item = childDataSnapshot.getValue(Item.class);
                                            item.setKey(childDataSnapshot.getKey());

                                            HashMap<String, String> itemTags = new HashMap<>();

                                            for (DataSnapshot grandChildDataSnapshot : childDataSnapshot.child("itemTags").getChildren()) {
                                                itemTags.put(grandChildDataSnapshot.getKey(), (String)grandChildDataSnapshot.getValue());
                                            }

                                            if (itemTags.size() > 0) {
                                                item.setItemTags(itemTags);
                                            }

                                            item.setNotificationsEnabled((boolean)childDataSnapshot.child("notificationsEnabled").getValue());


                                            if (item.getItemTags() != null && item.getItemTags().containsKey(tag.getKey()) && item.getNotificationsEnabled()) {

                                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 3");

                                                boolean alreadySet = false;

                                                for (int i = 0; i < itemTagAlarmManagers.size(); ++i) {
                                                    ItemTagAlarmManager itemTagAlarmManager = itemTagAlarmManagers.get(i);

                                                    if (itemTagAlarmManager.getItemKey().equals(item.getKey()) && itemTagAlarmManager.getTagKey().equals(tag.getKey())) {
                                                        alreadySet = true;
                                                        break;
                                                    }
                                                }

                                                if (!alreadySet) {
                                                    System.out.println("SETTING UP LOCATIONMANAGER");

                                                    setUpAlarmManager(item, tag);
                                                }

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(final String key) {

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tag").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            Tag tag = childDataSnapshot.getValue(Tag.class);

                            if (tag.getLocationKey() != null && tag.getLocationKey().equals(key)) {

                                ArrayList<ItemTagAlarmManager> itemTagAlarmManagersToRemove = new ArrayList<>();

                                for (int i = 0; i < itemTagAlarmManagers.size(); ++i) {
                                    ItemTagAlarmManager itemTagAlarmManager = itemTagAlarmManagers.get(i);
                                    if (itemTagAlarmManager.getTagKey().equals(tag.getKey())) {
                                        itemTagAlarmManager.getAlarmManager().cancel(itemTagAlarmManager.getPendingIntent());
                                        itemTagAlarmManagersToRemove.add(itemTagAlarmManager);
                                    }
                                }

                                itemTagAlarmManagers.removeAll(itemTagAlarmManagersToRemove);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    public void setUpNonLocationNotifications() {

        System.out.println("INSIDE SETUPNONLOCATIONNOTIFICATIONS");

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Tag tag = dataSnapshot.getValue(Tag.class);
                tag.setKey(dataSnapshot.getKey());

                System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, 0");

                if (dataSnapshot.child("locationAddress").getValue() == null) {
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, 1");

                            Item item = dataSnapshot.getValue(Item.class);
                            item.setKey(dataSnapshot.getKey());

                            HashMap<String, String> itemTags = new HashMap<>();

                            for (DataSnapshot childDataSnapshot : dataSnapshot.child("itemTags").getChildren()) {
                                itemTags.put(childDataSnapshot.getKey(), (String) childDataSnapshot.getValue());
                            }

                            if (itemTags.size() > 0) {
                                item.setItemTags(itemTags);
                            }

                            item.setNotificationsEnabled((boolean) dataSnapshot.child("notificationsEnabled").getValue());

                            if (item.getItemTags() != null && item.getItemTags().containsKey(tag.getKey()) && item.getNotificationsEnabled()) {

                                System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, 2");

                                boolean alreadySet = false;

                                for (int i = 0; i < itemTagAlarmManagers.size(); ++i) {
                                    ItemTagAlarmManager itemTagAlarmManager = itemTagAlarmManagers.get(i);

                                    if (itemTagAlarmManager.getItemKey().equals(item.getKey()) && itemTagAlarmManager.getTagKey().equals(tag.getKey())) {
                                        alreadySet = true;
                                        break;
                                    }
                                }

                                if (!alreadySet) {

                                    setUpAlarmManager(item, tag);
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            resetAll(true);

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                            resetAll(true);

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                resetAll(true);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                resetAll(true);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpAlarmManager(Item item, Tag tag) {

        System.out.println("ITEM NAME =========== " +item.getText());

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 4");

        if ((tag.getDaysOfTheWeek().equals("0000000") || tag.getDaysOfTheWeek().equals("1111111")) && tag.getTime() == null) {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 5");

            Calendar calendar = Calendar.getInstance();

            Intent intent = new Intent(mainFragmentActivity, TagsNotificationReceiver.class);
            intent.putExtra("item name", item.getText());
            intent.putExtra("list name", item.getListName());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mainFragmentActivity, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) mainFragmentActivity.getSystemService(mainFragmentActivity.ALARM_SERVICE);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+2, AlarmManager.INTERVAL_DAY, pendingIntent);

            itemTagAlarmManagers.add(new ItemTagAlarmManager(item.getKey(), tag.getKey(), alarmManager, pendingIntent));

//            finaliseAlarmManager(item, tag, calendar, 1);


        } else if ((tag.getDaysOfTheWeek().equals("0000000") || tag.getDaysOfTheWeek().equals("1111111")) && tag.getTime() != null) {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 6");

            Calendar calendar = Calendar.getInstance();

            String split[] = tag.getTime().split(":", 2);

            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
            calendar.set(Calendar.SECOND, 00);

            long triggerAtMillis;

            if (new Date(calendar.getTimeInMillis()).before(new Date())) {
               triggerAtMillis = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1);
            }
            else {
                triggerAtMillis = calendar.getTimeInMillis();
            }

            Intent intent = new Intent(mainFragmentActivity, TagsNotificationReceiver.class);
            intent.putExtra("item name", item.getText());
            intent.putExtra("list name", item.getListName());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mainFragmentActivity, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)mainFragmentActivity.getSystemService(mainFragmentActivity.ALARM_SERVICE);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);

            itemTagAlarmManagers.add(new ItemTagAlarmManager(item.getKey(), tag.getKey(), alarmManager, pendingIntent));

//            finaliseAlarmManager(item, tag, calendar, 1);

        } else if (!tag.getDaysOfTheWeek().equals("0000000") && tag.getTime() != null) {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 7");

            String tagDaysOfTheWeek = tag.getDaysOfTheWeek();
            String tagTime = tag.getTime();

            for (int i = 0; i < tagDaysOfTheWeek.length(); ++i) {
                if (tagDaysOfTheWeek.charAt(i) == '1') {

                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 8");

                    Calendar calendar = Calendar.getInstance();

                    String split[] = tagTime.split(":", 2);

                    int dayAsInt = i + 2;
                    if (dayAsInt == 8) {
                        dayAsInt = 1;
                    }

                    calendar.set(Calendar.DAY_OF_WEEK, dayAsInt);
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
                    calendar.set(Calendar.SECOND, 00);

                    System.out.println("SETTING ALARMMANAGER AT DAY " + dayAsInt + ", HOUR: " + Integer.parseInt(split[0]) + ":" + Integer.parseInt(split[1]));

                    long triggerAtMillis;

                    if (new Date(calendar.getTimeInMillis()).before(new Date())) {
                        triggerAtMillis = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(7);
                    }
                    else {
                        triggerAtMillis = calendar.getTimeInMillis();
                    }

                    Intent intent = new Intent(mainFragmentActivity, TagsNotificationReceiver.class);
                    intent.putExtra("item name", item.getText());
                    intent.putExtra("list name", item.getListName());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mainFragmentActivity, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager)mainFragmentActivity.getSystemService(mainFragmentActivity.ALARM_SERVICE);

                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                    itemTagAlarmManagers.add(new ItemTagAlarmManager(item.getKey(), tag.getKey(), alarmManager, pendingIntent));

//                    finaliseAlarmManager(item, tag, calendar, 7);
                }
            }


        } else if (!tag.getDaysOfTheWeek().equals("0000000") && !tag.getDaysOfTheWeek().equals("1111111") && tag.getTime() == null) {

            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 9");

            String tagDaysOfTheWeek = tag.getDaysOfTheWeek();

            for (int i = 0; i < tagDaysOfTheWeek.length(); ++i) {
                if (tagDaysOfTheWeek.charAt(i) == '1') {

                    Calendar calendar = Calendar.getInstance();

                    int dayOfWeek = i + 2;

                    if (dayOfWeek == 8) {
                        dayOfWeek = 1;
                    }

                    int todaysDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);


                    System.out.println("dayOfWeek = " + dayOfWeek + ", todaysDayOfWeek = " + todaysDayOfWeek);

                    if (dayOfWeek == todaysDayOfWeek) {

                        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 2);
                        calendar.set(Calendar.SECOND, 00);


                        Intent intent = new Intent(mainFragmentActivity, TagsNotificationReceiver.class);
                        intent.putExtra("item name", item.getText());
                        intent.putExtra("list name", item.getListName());

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainFragmentActivity, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) mainFragmentActivity.getSystemService(mainFragmentActivity.ALARM_SERVICE);

                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

                        itemTagAlarmManagers.add(new ItemTagAlarmManager(item.getKey(), tag.getKey(), alarmManager, pendingIntent));

//                        finaliseAlarmManager(item, tag, calendar, 7);

                        break;
                    }
                }
            }

        }
    }

    public void resetAll(boolean setUp) {

        if (itemTagAlarmManagers.size() > 0) {

            ArrayList<ItemTagAlarmManager> itemTagAlarmManagersToRemove = new ArrayList<>();

            for (ItemTagAlarmManager itemTagAlarmManager : itemTagAlarmManagers) {
                itemTagAlarmManager.getAlarmManager().cancel(itemTagAlarmManager.getPendingIntent());
                itemTagAlarmManagersToRemove.add(itemTagAlarmManager);
            }

            itemTagAlarmManagers.removeAll(itemTagAlarmManagersToRemove);
        }

        if (setUp) {
            startLocationUpdates();

            setUpNonLocationNotifications();
        }
    }

}
