package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;

import com.gettingthingsdone.federico.gettingthingsdone.activities.TagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.TagsAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 11-Feb-18.
 */

public class TagsFragment extends Fragment {

    private TagsAdapter tagsAdapter;

    private TextView youHaveNoTagsTextView;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public static final int REQUEST_NEW_TAG = 0;
    public static final int REQUEST_EDIT_TAG = 1;

//    private static ArrayList<Tag> tags;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        getActivity().setTitle(R.string.tags);

        setHasOptionsMenu(true);

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        youHaveNoTagsTextView = view.findViewById(R.id.empty_tags_textview);

        progressBar = view.findViewById(R.id.tags_progress_bar);

        return view;
    }

    @Nullable
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.tags_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TagActivity.class);
                intent.putExtra("requestCode", REQUEST_NEW_TAG);

                ArrayList<String> tagTexts = new ArrayList<>();

                for (int i = 0; i < tagsAdapter.getTags().size(); ++i) {
                    tagTexts.add(tagsAdapter.getTags().get(i).getText());
                }

                intent.putExtra("tag text list", tagTexts);


                startActivity(intent);
            }
        });


        recyclerView = getView().findViewById(R.id.tags_recycler_view);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);

        tagsAdapter = new TagsAdapter(this);

        if (tagsAdapter.getTags().size() > 0) {
            youHaveNoTagsTextView.setVisibility(View.GONE);
        }

        recyclerView.setAdapter(tagsAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_delete) {

            final ArrayList<Tag> tagsToRemove = new ArrayList<>();

            for (int i = 0; i < tagsAdapter.getSelectedIndexes().size(); ++i) {
                tagsToRemove.add(tagsAdapter.getTags().get(tagsAdapter.getSelectedIndexes().get(i)));
            }


            for (int i = 0; i < tagsToRemove.size(); ++i) {
                Tag tagToRemove = tagsToRemove.get(i);

                /////removes tag from items////
                for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                    Item item = MainFragmentActivity.getItems().get(j);

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(item.getKey()).child("itemTags").child(tagToRemove.getKey()).removeValue();
                }

                ////removes tag from tags////
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").child(tagToRemove.getKey()).removeValue();
            }


            tagsAdapter.clearSelected();

            ((MainFragmentActivity) getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            tagsAdapter.stopSelecting();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public TextView getYouHaveNoTagsTextView() {
        return youHaveNoTagsTextView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
