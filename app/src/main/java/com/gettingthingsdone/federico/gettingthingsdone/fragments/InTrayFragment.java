package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.InTrayItemsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView emtpyInTrayText;

    public static final int REQUEST_NEW_ITEM = 0;
    public static final int REQUEST_EDIT_ITEM = 1;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static ArrayList<Item> items;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            getActivity().startActivity(intent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_in_tray, container, false);

        emtpyInTrayText = (TextView) view.findViewById(R.id.empty_in_tray_text);

        getActivity().setTitle(R.string.in_tray);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.in_tray_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ItemActivity.class);
                intent.putExtra("requestCode", REQUEST_NEW_ITEM);


                startActivity(intent);
            }
        });

        recyclerView = getView().findViewById(R.id.in_tray_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new InTrayItemsAdapter(this, items);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {

            ArrayList<Item> itemsToRemove = new ArrayList<>();

            for (int i = 0; i < ((InTrayItemsAdapter)adapter).getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(items.get(((InTrayItemsAdapter)adapter).getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < itemsToRemove.size(); ++i) {

                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                    if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                        MainFragmentActivity.getItems().remove(mainActivityItem);
                        break;
                    }
                }

                for (int j = 0; j < items.size(); ++j) {
                    if (items.get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                        items.remove(j);
                        break;
                    }
                }
//
                if (items.size() == 0) {
                    getEmptyInTrayText().setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();


                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(itemsToRemove.get(i).getKey()).removeValue();
            }

            ((InTrayItemsAdapter)adapter).clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            ((InTrayItemsAdapter)adapter).stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<Item> getItems(){
        return items;
    }

    public TextView getEmptyInTrayText() {
        return emtpyInTrayText;
    }

    public void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }
}
