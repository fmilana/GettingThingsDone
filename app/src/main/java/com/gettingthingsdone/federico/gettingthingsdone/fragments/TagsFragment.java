package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.NewTagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.TagsAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Federico on 11-Feb-18.
 */

public class TagsFragment extends Fragment {

    public FirebaseAuth firebaseAuth;
    public DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    public static final int REQUEST_NEW_TAG = 0;
    public static final int REQUEST_EDIT_TAG = 1;

    private static ArrayList<Tag> tags;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        getActivity().setTitle(R.string.tags);

        setHasOptionsMenu(true);

        tags = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    @Nullable
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.tags_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewTagActivity.class);
                intent.putExtra("requestCode", REQUEST_NEW_TAG);
                startActivity(intent);
            }
        });


        recyclerView = getView().findViewById(R.id.tags_recycler_view);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TagsAdapter(this, tags);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {

            ArrayList<Tag> tagsToRemove = new ArrayList<>();

            for (int i = 0; i < ((TagsAdapter) adapter).getSelectedIndexes().size(); ++i) {
                tagsToRemove.add(tags.get(((TagsAdapter) adapter).getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < tagsToRemove.size(); ++i) {
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").child(tagsToRemove.get(i).getKey()).removeValue();
            }

            TagsAdapter.ViewHolder.clearSelected();

            ((MainFragmentActivity) getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            TagsAdapter.ViewHolder.stopSelecting();
        }

        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<Tag> getTags() {
        return tags;
    }
}
