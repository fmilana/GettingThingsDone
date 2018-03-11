package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Federico on 03-Feb-18.
 */

public class MainFragmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private InTrayFragment inTrayFragment;
    private ProjectsFragment projectsFragment;
    private CalendarFragment calendarFragment;
    private WaitingForFragment waitingForFragment;
    private MaybeLaterFragment maybeLaterFragment;
    private ReferenceFragment referenceFragment;
    private TrashFragment trashFragment;
    private TagsFragment tagsFragment;

    private FragmentManager fragmentManager;

    private Menu menu;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static ArrayList<Item> items;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        items = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        ///sets nav header email address to email address///
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ((TextView)findViewById(R.id.nav_header_email_address)).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        addFirebaseItemListener();


        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainFragmentActivity.this, MainActivity.class);
            MainFragmentActivity.this.startActivity(intent);
            finish();
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        inTrayFragment = new InTrayFragment();
        projectsFragment = new ProjectsFragment();
        calendarFragment = new CalendarFragment();
        waitingForFragment = new WaitingForFragment();
        maybeLaterFragment = new MaybeLaterFragment();
        referenceFragment = new ReferenceFragment();
        trashFragment = new TrashFragment();
        tagsFragment = new TagsFragment();

        fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_main_constraint_layout, inTrayFragment).commit();

        navigationView.getMenu().getItem(0).setChecked(true);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_log_out) {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainFragmentActivity.this, MainActivity.class);
            MainFragmentActivity.this.startActivity(intent);

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

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
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public Menu getMenu() {
        return menu;
    }

    private void addFirebaseItemListener() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Item newItem = dataSnapshot.getValue(Item.class);
                newItem.setKey(dataSnapshot.getKey());

                items.add(newItem);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Item editedItem = dataSnapshot.getValue(Item.class);
                editedItem.setKey(dataSnapshot.getKey());

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

    public static ArrayList<Item> getItems() {
        return items;
    }
}
