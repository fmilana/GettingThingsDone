package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.TagsNotificationManager;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.SettingsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;
import com.gettingthingsdone.federico.gettingthingsdone.receivers.CalendarNotificationReceiver;
import com.gettingthingsdone.federico.gettingthingsdone.receivers.InTrayReminderReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Created by Federico on 03-Feb-18.
 */

public class MainFragmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static ArrayList<Item> items;

    private MenuItem previousItem;

    private Handler handler;

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;

    private InTrayFragment inTrayFragment;
    private ProjectsFragment projectsFragment;
    private CalendarFragment calendarFragment;
    private WaitingForFragment waitingForFragment;
    private MaybeLaterFragment maybeLaterFragment;
    private ReferenceFragment referenceFragment;
    private TrashFragment trashFragment;
    private TagsFragment tagsFragment;
    private SettingsFragment settingsFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private FusedLocationProviderClient fusedLocationClient;

    private Menu menu;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public static final int PERMISSION_REQUEST_READ_FINE_LOCATION = 0;

    public static final String INTRAY_REMINDERS_CHANNEL = "InTrayRemindersChannel";
    public static final String CALENDAR_NOTIFICATIONS_CHANNEL = "CalendarNotificationsChannel";
    public static final String TAGS_NOTIFICATIONS_CHANNEL = "TagsNotificationsChannel";

    private TagsNotificationManager tagsNotificationManager;

    private static HashMap<AlarmManager, PendingIntent> calendarAlarmManagerAndPendingIntent;
    private static HashMap<AlarmManager, PendingIntent> inTrayAlarmManagerAndPendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        System.out.println("+++++++++++++++++++++++++++++++++++++++++++ resetting items!!!");
        items = new ArrayList<>();

        handler = new Handler();

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainFragmentActivity.this, LogInActivity.class);
            MainFragmentActivity.this.startActivity(intent);
            finish();
        }

        addFirebaseItemListener();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        inTrayFragment = new InTrayFragment();
        projectsFragment = new ProjectsFragment();
        calendarFragment = new CalendarFragment();
        waitingForFragment = new WaitingForFragment();
        maybeLaterFragment = new MaybeLaterFragment();
        referenceFragment = new ReferenceFragment();
        trashFragment = new TrashFragment();
        tagsFragment = new TagsFragment();
        settingsFragment = new SettingsFragment();

        fragmentManager = getFragmentManager();

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_main_constraint_layout, inTrayFragment).commit();

        navigationView.getMenu().getItem(0).setCheckable(true);
        navigationView.getMenu().getItem(0).setChecked(true);

        setUpInTrayReminders();
        setUpCalendarNotifications();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tagsNotificationManager = new TagsNotificationManager(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {

            System.out.println("LOCATION ALREADY GRANTED");

            tagsNotificationManager.startLocationUpdates();
        }

        tagsNotificationManager.setUpNonLocationNotifications();


        String fragmentToLaunch = getIntent().getStringExtra("fragmentToLaunch");

        if (fragmentToLaunch != null) {
            if (fragmentToLaunch.equals("waitingfor")) {

                navigationView.getMenu().getItem(3).setCheckable(true);
                navigationView.getMenu().getItem(3).setChecked(true);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, waitingForFragment);

            } else if (fragmentToLaunch.equals("maybelater")) {

                navigationView.getMenu().getItem(4).setCheckable(true);
                navigationView.getMenu().getItem(4).setChecked(true);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, maybeLaterFragment);

            } else if (fragmentToLaunch.equals("reference")) {

                navigationView.getMenu().getItem(5).setCheckable(true);
                navigationView.getMenu().getItem(5).setChecked(true);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, referenceFragment);

            } else if (fragmentToLaunch.equals("trash")) {

                navigationView.getMenu().getItem(6).setCheckable(true);
                navigationView.getMenu().getItem(6).setChecked(true);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, trashFragment);

            } else if (fragmentToLaunch.split(" ", 2)[0].equals("calendar")) {

                navigationView.getMenu().getItem(2).setCheckable(true);
                navigationView.getMenu().getItem(2).setChecked(true);

                calendarFragment.setDateToShow(fragmentToLaunch.split(" ", 2)[1]);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, calendarFragment);

            } else if (fragmentToLaunch.split(" ", 2)[0].equals("projects")) {

                navigationView.getMenu().getItem(1).setCheckable(true);
                navigationView.getMenu().getItem(1).setChecked(true);

                projectsFragment.setProjectToShow(fragmentToLaunch.split(" ", 2)[1]);

                fragmentTransaction.replace(R.id.content_main_constraint_layout, projectsFragment);
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        //mine
        this.menu = menu;

        ///sets nav header email address to email address///
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((TextView)drawerLayout.findViewById(R.id.nav_header_email_address)).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_log_out) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainFragmentActivity.this, LogInActivity.class);
            MainFragmentActivity.this.startActivity(intent);

            tagsNotificationManager.resetAll(false);

            finish();

            return true;
        } else if (id == R.id.menu_settings) {
            fragmentManager = getFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_main_constraint_layout, settingsFragment).commit();

            navigationView.getMenu().getItem(8).setCheckable(true);
            navigationView.getMenu().getItem(8).setChecked(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {

        item.setCheckable(true);
        item.setChecked(true);

        if (previousItem != null && previousItem != item) {
            previousItem.setChecked(false);
        }

        previousItem = item;

//        menuItemSelected = item;

        FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        switch (item.getItemId()) {
            case R.id.nav_in_tray:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, inTrayFragment).commit();
                break;
            case R.id.nav_projects:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, projectsFragment).commit();
                break;
            case R.id.nav_calendar:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, calendarFragment).commit();
                break;
            case R.id.nav_waiting_for:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, waitingForFragment).commit();
                break;
            case R.id.nav_maybe_later:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, maybeLaterFragment).commit();
                break;
            case R.id.nav_reference:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, referenceFragment).commit();
                break;
            case R.id.nav_trash:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, trashFragment).commit();
                break;
            case R.id.nav_tags:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, tagsFragment).commit();
                break;
            case R.id.nav_settings:
                fragmentTransaction.replace(R.id.content_main_constraint_layout, settingsFragment).commit();
                break;
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 250);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tagsNotificationManager.startLocationUpdates();
                }
                return;
            }
        }
    }

    public Menu getMenu() {
        return menu;
    }

    private void addFirebaseItemListener() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for(int i = 0; i < items.size(); ++i) {
                    if (items.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                Item newItem = dataSnapshot.getValue(Item.class);

                HashMap<String, String> itemTags = new HashMap<>();

                for (DataSnapshot childDataSnapshot : dataSnapshot.child("itemTags").getChildren()) {
                    System.out.println("READING ITEMTAG BEFORE PUTTING IT IN HASHMAP, TAG KEY IS " + childDataSnapshot.getKey() + ", text = " + childDataSnapshot.getValue());

                    itemTags.put(childDataSnapshot.getKey(), (String)childDataSnapshot.getValue());
                }

                if (itemTags.size() > 0) {
                    newItem.setItemTags(itemTags);
                }

                newItem.setNotificationsEnabled((boolean)dataSnapshot.child("notificationsEnabled").getValue());

                newItem.setKey(dataSnapshot.getKey());

                items.add(newItem);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Item editedItem = dataSnapshot.getValue(Item.class);
                editedItem.setKey(dataSnapshot.getKey());

                HashMap<String, String> itemTags = new HashMap<>();

                for (DataSnapshot childDataSnapshot : dataSnapshot.child("itemTags").getChildren()) {
                    itemTags.put(childDataSnapshot.getKey(), (String)childDataSnapshot.getValue());
                }

                if (itemTags.size() > 0) {
                    editedItem.setItemTags(itemTags);
                }

                editedItem.setNotificationsEnabled((boolean)dataSnapshot.child("notificationsEnabled").getValue());

                for (int i = 0; i < items.size(); ++i) {
                    if (items.get(i).getKey().equals(dataSnapshot.getKey())) {
                        items.set(i, editedItem);


                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                System.out.println("REMOVING ITEM FROM ITEM LIST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                for (int i = 0; i < items.size(); ++i) {
                    if (items.get(i).getKey().equals(dataSnapshot.getKey())) {
                        items.remove(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpInTrayReminders() {

        final Calendar calendar = Calendar.getInstance();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("READING TIME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                String split1[] = ((String)dataSnapshot.getValue()).split(" ", 2);
                String split2[] = split1[1].split(":", 2);

                int dayAsInt = Integer.parseInt(split1[0]) + 1;
                if (dayAsInt == 7) {
                    dayAsInt = 1;
                }

                calendar.set(Calendar.DAY_OF_WEEK, dayAsInt);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split2[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(split2[1]));
                calendar.set(Calendar.SECOND, 00);

                long triggerAtMillis;

                if (new Date(calendar.getTimeInMillis()).before(new Date())) {
                    triggerAtMillis = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(7);
                } else {
                    triggerAtMillis = calendar.getTimeInMillis();
                }

                System.out.println("DATE  =================> " + new Date(triggerAtMillis));

                Intent intent = new Intent(getApplicationContext(), InTrayReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

                if (inTrayAlarmManagerAndPendingIntent == null) {
                    inTrayAlarmManagerAndPendingIntent = new HashMap<>();
                } else {
                    for (AlarmManager inTrayAlarmManager : inTrayAlarmManagerAndPendingIntent.keySet()) {
                        inTrayAlarmManager.cancel(inTrayAlarmManagerAndPendingIntent.get(inTrayAlarmManager));
                    }
                    inTrayAlarmManagerAndPendingIntent.clear();
                }

                inTrayAlarmManagerAndPendingIntent.put(alarmManager, pendingIntent);


                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setUpCalendarNotifications() {
        final Calendar calendar = Calendar.getInstance();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String split[] = ((String)dataSnapshot.getValue()).split(":", 2);

                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(split[1]));
                calendar.set(Calendar.SECOND, 00);

                long triggerAtMillis;

                if (new Date(calendar.getTimeInMillis()).before(new Date())) {
                    triggerAtMillis = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1);
                } else {
                    triggerAtMillis = calendar.getTimeInMillis();
                }

                System.out.println("CALENDAR NOTIFICATION DATE  =================> " + new Date(triggerAtMillis));

                Intent intent = new Intent(getApplicationContext(), CalendarNotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

                if (calendarAlarmManagerAndPendingIntent == null) {
                    calendarAlarmManagerAndPendingIntent = new HashMap<>();
                } else {
                    for (AlarmManager calendarAlarmManager : calendarAlarmManagerAndPendingIntent.keySet()) {
                        calendarAlarmManager.cancel(calendarAlarmManagerAndPendingIntent.get(calendarAlarmManager));
                    }
                    calendarAlarmManagerAndPendingIntent.clear();
                }

                calendarAlarmManagerAndPendingIntent.put(alarmManager, pendingIntent);

                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    public static ArrayList<Item> getItems() {
        return items;
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }

    public TagsNotificationManager getTagsNotificationManager() {
        return tagsNotificationManager;
    }
}
