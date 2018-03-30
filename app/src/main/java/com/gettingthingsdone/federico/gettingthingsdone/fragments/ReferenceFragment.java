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
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ReferenceAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class ReferenceFragment extends Fragment {

    private ReferenceAdapter referenceAdapter;

    private TextView emptyReferenceText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public static final int REQUEST_EDIT_REFERENCE_ITEM = 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        System.out.println("++++++++++referenceFragmentOnStart+++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

        View view = inflater.inflate(R.layout.fragment_reference, container, false);

        emptyReferenceText = (TextView) view.findViewById(R.id.emtpy_reference_list_text);
        progressBar = (ProgressBar) view.findViewById(R.id.reference_progress_bar);

        getActivity().setTitle(R.string.reference);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.reference_recycler_view);
        layoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);


        referenceAdapter = new ReferenceAdapter(this);
        recyclerView.setAdapter(referenceAdapter);

        if (referenceAdapter.getItems().size() > 0) {
            emptyReferenceText.setVisibility(View.GONE);
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

            for (int i = 0; i < referenceAdapter.getSelectedIndexes().size(); ++i) {
                itemsToRemove.add(referenceAdapter.getItems().get(referenceAdapter.getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < itemsToRemove.size(); ++i) {

                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                    if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                        MainFragmentActivity.getItems().remove(mainActivityItem);
                        break;
                    }
                }

                for (int j = 0; j < referenceAdapter.getItems().size(); ++j) {
                    if (referenceAdapter.getItems().get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                        referenceAdapter.getItems().remove(j);
                        break;
                    }
                }
//
                if (referenceAdapter.getItems().size() == 0) {
                    getEmptyReferenceText().setVisibility(View.VISIBLE);
                }

                referenceAdapter.notifyDataSetChanged();

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("reference").child(itemsToRemove.get(i).getKey()).removeValue();
            }

            referenceAdapter.clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            referenceAdapter.stopSelecting();

            if (itemsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Item deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    public TextView getEmptyReferenceText() {
        return emptyReferenceText;
    }

    public void notifyAdapter() {
        referenceAdapter.notifyDataSetChanged();
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
