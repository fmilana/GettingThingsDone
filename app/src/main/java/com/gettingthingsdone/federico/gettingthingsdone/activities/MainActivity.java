package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private InTrayFragment inTrayFragment;
    private ProjectsFragment projectsFragment;
    private CalendarFragment calendarFragment;
    private WaitingForFragment waitingForFragment;
    private MaybeLaterFragment maybeLaterFragment;
    private ReferenceFragment referenceFragment;
    private TrashFragment trashFragment;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
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

        fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_main_constraint_layout, inTrayFragment).commit();

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_log_out) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (id == R.id.nav_in_tray) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, inTrayFragment).commit();
        } else if (id == R.id.nav_projects) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, projectsFragment).commit();
        } else if (id == R.id.nav_calendar) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, calendarFragment).commit();
        } else if (id == R.id.nav_waiting_for) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, waitingForFragment).commit();
        } else if (id == R.id.nav_maybe_later) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, maybeLaterFragment).commit();
        } else if (id == R.id.nav_reference) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, referenceFragment).commit();
        } else if (id == R.id.nav_trash) {
            fragmentTransaction.replace(R.id.content_main_constraint_layout, trashFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
