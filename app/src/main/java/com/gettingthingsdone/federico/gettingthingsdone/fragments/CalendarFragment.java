package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.CalendarAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.WaitingForAdapter;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Federico on 07-Nov-17.
 */

public class CalendarFragment extends Fragment {

    private CalendarAdapter calendarAdapter;

    private CalendarFragment calendarFragment;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TextView emptyCalendarItemsText;
    private ProgressBar progressBar;

//    private CalendarView calendarView;
    private CompactCalendarView calendarView;
    private TextView monthTextView;
    private RecyclerView itemRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private String selectedDate;

//    private HashMap<String, ArrayList<Item>> calendarMap;

    public final static int REQUEST_EDIT_CALENDAR_ITEM = 8;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.calendarFragment = this;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

//        calendarMap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        getActivity().setTitle(R.string.calendar);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
        calendarView = (CompactCalendarView) view.findViewById(R.id.calendar_view);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        monthTextView = (TextView) view.findViewById(R.id.month_text_view);
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");
        monthTextView.setText(monthDateFormat.format(new Date()));

        itemRecyclerView = (RecyclerView)view.findViewById(R.id.day_items_recycler_view);

        emptyCalendarItemsText = (TextView)view.findViewById(R.id.this_day_has_no_items_textview);
        progressBar = (ProgressBar)view.findViewById(R.id.calendar_progress_bar);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
//        selectedDate = simpleDateFormat.format(new Date(calendarView.getDate()));
        selectedDate = simpleDateFormat.format(new Date());

        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        itemRecyclerView.setLayoutManager(layoutManager);

        calendarAdapter = new CalendarAdapter(this, selectedDate);

        itemRecyclerView.setAdapter(calendarAdapter);


        loadEvents();

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                calendarAdapter.clearSelected();
                calendarAdapter.stopSelecting();

                progressBar.setVisibility(View.VISIBLE);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
                selectedDate = simpleDateFormat.format(dateClicked);

                calendarAdapter = new CalendarAdapter(calendarFragment, selectedDate);

                itemRecyclerView.setAdapter(calendarAdapter);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calendarAdapter.clearSelected();
                calendarAdapter.stopSelecting();

                SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");

                monthTextView.setText(monthDateFormat.format(firstDayOfNewMonth));
            }
        });

        if (calendarAdapter.getItems().size() > 0) {
            emptyCalendarItemsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {

            ArrayList<Item> itemsToRemove = new ArrayList<>();

            for (int i = 0; i < calendarAdapter.getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(calendarAdapter.getItems().get(calendarAdapter.getSelectedIndexes().get(i)));
            }

            System.out.println("ITEMS TO REMOVE SIZE = " + itemsToRemove.size());

            for (int i = 0; i < itemsToRemove.size(); ++i) {

                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                    if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                        MainFragmentActivity.getItems().remove(mainActivityItem);
                    }
                }

                for (int j = 0; j < calendarAdapter.getItems().size(); ++j) {
                    if (calendarAdapter.getItems().get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                        calendarAdapter.getItems().remove(j);
                    }

                    ////removes dots////
                    try {
                        Date date = new SimpleDateFormat("ddMMyyyy").parse(selectedDate);
                        calendarView.removeEvent(calendarView.getEvents(date).get(calendarView.getEvents(date).size()-1));

                        System.out.println("DELETED EVENT");

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (calendarAdapter.getItems().size() == 0) {
                    emptyCalendarItemsText.setVisibility(View.VISIBLE);
                }

                calendarAdapter.notifyDataSetChanged();


                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).child(itemsToRemove.get(i).getKey()).removeValue();
            }

            calendarAdapter.clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            calendarAdapter.stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    public TextView getEmptyCalendarItemsText() {
        return emptyCalendarItemsText;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    private void loadEvents() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (int i = 0; i < dataSnapshot.getChildrenCount(); ++i) {
                    try {
                        Date date = new SimpleDateFormat("ddMMyyyy").parse(dataSnapshot.getKey());

                        if (isAdded() && getActivity() != null) {
                            calendarView.addEvent(new Event(getResources().getColor(R.color.colorAccent), date.getTime()));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    Date date = new SimpleDateFormat("ddMMyyyy").parse(dataSnapshot.getKey());

                    calendarView.removeEvents(date);

                } catch (ParseException e) {
                    e.printStackTrace();
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
}
