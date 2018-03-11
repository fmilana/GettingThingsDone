package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by Federico on 07-Nov-17.
 */

public class CalendarFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private CalendarView calendarView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        getActivity().setTitle(R.string.calendar);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        calendarView = (CalendarView)view.findViewById(R.id.calendar_view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
