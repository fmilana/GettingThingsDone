package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gettingthingsdone.federico.gettingthingsdone.InTrayItem;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.InTrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<InTrayItem> items;

    public void initialiseItems() {
        items = new ArrayList<InTrayItem>();

        items.add(new InTrayItem("somebody"));
        items.add(new InTrayItem("once"));
        items.add(new InTrayItem("told"));
        items.add(new InTrayItem("me"));
        items.add(new InTrayItem("the"));
        items.add(new InTrayItem("world"));
        items.add(new InTrayItem("was"));
        items.add(new InTrayItem("gonna"));
        items.add(new InTrayItem("roll"));
        items.add(new InTrayItem("me"));
        items.add(new InTrayItem("i"));
        items.add(new InTrayItem("aint"));
        items.add(new InTrayItem("the"));
        items.add(new InTrayItem("sharpest"));
        items.add(new InTrayItem("tool"));
        items.add(new InTrayItem("in"));
        items.add(new InTrayItem("the"));
        items.add(new InTrayItem("shed"));
        items.add(new InTrayItem("clean house"));
        items.add(new InTrayItem("Phone number"));
        items.add(new InTrayItem("029487639"));
        items.add(new InTrayItem("call mike"));
        items.add(new InTrayItem("buy pc"));
        items.add(new InTrayItem("clean house"));
        items.add(new InTrayItem("Phone number"));
        items.add(new InTrayItem("029487639"));
    }

    public List<InTrayItem> getItems() {
        return items;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_in_tray, container, false);

        getActivity().setTitle(R.string.in_tray);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.in_tray_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                getActivity().setTitle(R.string.enter_item_in_tray);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.add(R.id.content_main_constraint_layout, new InTrayInputDialogFragment()).addToBackStack(null).commit();
            }
        });

        recyclerView = getView().findViewById(R.id.in_tray_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        initialiseItems();

        adapter = new InTrayAdapter(items);
        recyclerView.setAdapter(adapter);
    }
}
