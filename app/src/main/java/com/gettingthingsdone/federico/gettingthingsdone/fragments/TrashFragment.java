package com.gettingthingsdone.federico.gettingthingsdone.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.TrashAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class TrashFragment extends Fragment {

    private static ArrayList<Item> items;

    private TextView emptyTrashText;

    public static final int REQUEST_VIEW_ITEM = 3;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_trash, container, false);

        emptyTrashText = (TextView) view.findViewById(R.id.empty_trash_text);

        getActivity().setTitle(R.string.trash);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.trash_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TrashAdapter(this, items);
        recyclerView.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {

            ArrayList<Item> itemsToRemove = new ArrayList<>();

            for (int i = 0; i < ((TrashAdapter) adapter).getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(items.get(((TrashAdapter) adapter).getSelectedIndexes().get(i)));
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
                    getEmptyTrashText().setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash").child(itemsToRemove.get(i).getKey()).removeValue();
            }

            ((TrashAdapter) adapter).clearSelected();

            ((MainFragmentActivity) getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            ((TrashAdapter) adapter).stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public TextView getEmptyTrashText() {
        return emptyTrashText;
    }

    public static ArrayList<Item> getItems() {
        return items;
    }

    public void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }
}
