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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.MaybeLaterAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class MaybeLaterFragment extends Fragment {

    private MaybeLaterAdapter maybeLaterAdapter;

    private TextView emptyMaybeLaterText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public static final int REQUEST_EDIT_MAYBE_LATER_ITEM = 4;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        items = new ArrayList<>();

        System.out.println("++++++++++maybelaterFragmentOnStart+++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_maybe_later, container, false);

        emptyMaybeLaterText = (TextView) view.findViewById(R.id.emtpy_maybe_later_list_text);

        progressBar = (ProgressBar) view.findViewById(R.id.maybe_later_progress_bar);

        getActivity().setTitle(R.string.maybe_later);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.maybe_later_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        maybeLaterAdapter = new MaybeLaterAdapter(this);

        recyclerView.setAdapter(maybeLaterAdapter);

        if (maybeLaterAdapter.getItems().size() > 0) {
            emptyMaybeLaterText.setVisibility(View.GONE);
        }

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

            for (int i = 0; i < maybeLaterAdapter.getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(maybeLaterAdapter.getItems().get(maybeLaterAdapter.getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < itemsToRemove.size(); ++i) {
                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                    if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                        MainFragmentActivity.getItems().remove(mainActivityItem);
                        break;
                    }
                }

                for (int j = 0; j < maybeLaterAdapter.getItems().size(); ++j) {
                    if (maybeLaterAdapter.getItems().get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                        maybeLaterAdapter.getItems().remove(j);
                        break;
                    }
                }
//
                if (maybeLaterAdapter.getItems().size() == 0) {
                    getEmptyMaybeLaterText().setVisibility(View.VISIBLE);
                }

                maybeLaterAdapter.notifyDataSetChanged();

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("maybelater").child(itemsToRemove.get(i).getKey()).removeValue();
            }

            maybeLaterAdapter.clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            maybeLaterAdapter.stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    public TextView getEmptyMaybeLaterText() {
        return emptyMaybeLaterText;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void notifyAdapter() {
        maybeLaterAdapter.notifyDataSetChanged();
    }
}
