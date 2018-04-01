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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;

import com.gettingthingsdone.federico.gettingthingsdone.adapters.InTrayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayFragment extends Fragment {

    private RecyclerView recyclerView;
    private InTrayAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ProgressBar progressBar;

    private TextView emptyInTrayText;

    public static final int REQUEST_NEW_ITEM = 0;
    public static final int REQUEST_EDIT_ITEM = 1;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("CREATING INTRAY");

//        items = new ArrayList<>();

        System.out.println("++++++++++intrayFragmentOnStart+++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;


        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            getActivity().startActivity(intent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_in_tray, container, false);

        emptyInTrayText = (TextView) view.findViewById(R.id.empty_in_tray_text);

        progressBar = (ProgressBar) view.findViewById(R.id.in_tray_progress_bar);

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

        recyclerView = view.findViewById(R.id.in_tray_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        System.out.println("set new adapter");
        adapter = new InTrayAdapter(this);

        recyclerView.setAdapter(adapter);

        if (adapter.getItems().size() > 0) {
            emptyInTrayText.setVisibility(View.GONE);
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

            for (int i = 0; i < adapter.getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(adapter.getItems().get(adapter.getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < itemsToRemove.size(); ++i) {

                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                    if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                        MainFragmentActivity.getItems().remove(mainActivityItem);
                        break;
                    }
                }

                for (int j = 0; j < adapter.getItems().size(); ++j) {
                    if (adapter.getItems().get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                        adapter.getItems().remove(j);
                        break;
                    }
                }
//
                if (adapter.getItems().size() == 0) {
                    getEmptyInTrayText().setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();


                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(itemsToRemove.get(i).getKey()).removeValue();
            }

            adapter.clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            adapter.stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public ArrayList<Item> getItems(){
        return adapter.getItems();
    }

    public TextView getEmptyInTrayText() {
        return emptyInTrayText;
    }

    public void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }

    public InTrayAdapter getAdapter() {
        return adapter;
    }

    public ProgressBar getProgressBar() {return progressBar;}

//    public static void resetAdapter() {
//        adapter = null;
//    }
}
