package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.TagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Federico on 15-Feb-18.
 */

public class ItemTagsAdapter extends RecyclerView.Adapter<ItemTagsAdapter.ViewHolder> {

    private static ArrayList<Tag> tags;

    private static int requestCode;

    private static ItemActivity itemActivity;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

//    private static ArrayList<String> selectedTexts;
    private static HashMap<String, String> itemTags;
    private static ArrayList<View> selectedViews;
    private static ArrayList<Integer> selectedIndexes;

    private RecyclerView recyclerView;

    private View cardView;

    private static boolean newTagJustAdded;

    public ItemTagsAdapter(final ItemActivity itemActivity, final ArrayList<Tag> tags, final int requestCode) {
        this.tags = tags;

        this.requestCode = requestCode;

        recyclerView = itemActivity.findViewById(R.id.item_tags_recycler_view);

        newTagJustAdded = false;
        this.itemActivity = itemActivity;

        itemTags = (HashMap<String, String>) itemActivity.getIntent().getSerializableExtra("item tags");

//        selectedTexts = new ArrayList<>();
        selectedViews = new ArrayList<>();
        selectedIndexes = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if tag already exists, then return;
                for (int i = 0; i < itemActivity.getTags().size(); ++i) {

                    if (itemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                Tag newItemTag = dataSnapshot.getValue(Tag.class);
                newItemTag.setKey(dataSnapshot.getKey());


                if (requestCode == TrashFragment.REQUEST_VIEW_ITEM) {
                    if (itemTags != null) {
                        if (itemTags.containsValue(newItemTag.getText())) {
                            itemActivity.getThisItemHasNoTags().setVisibility(View.GONE);
                            itemActivity.getTags().add(newItemTag);
                        }
                    }
                } else {
                    itemActivity.removeAddNewTag();
                    itemActivity.getTags().add(newItemTag);
                    itemActivity.addAddNewTag();
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Tag editedTag = dataSnapshot.getValue(Tag.class);
                editedTag.setKey(dataSnapshot.getKey());

                for (int i = 0; i < itemActivity.getTags().size(); ++i) {
                    if (itemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        itemActivity.getTags().set(i, editedTag);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0; i < itemActivity.getTags().size(); ++i) {
                    if (itemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        itemActivity.getTags().remove(i);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public ItemTagsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemActivity, cardView);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tag tag = tags.get(position);

        holder.setIsRecyclable(false);

        holder.itemTagTextView.setText(tag.getText());


        selectTagIfSelected(cardView, holder.itemTagTextView, holder);

        if (requestCode != TrashFragment.REQUEST_VIEW_ITEM) {

            if (newTagJustAdded && position == itemActivity.getTags().size() - 2) {
                holder.itemTagTextView.setBackgroundColor(holder.itemTagTextView.getResources().getColor(R.color.colorAccent));

                selectedIndexes.add(position);
                selectedViews.add(cardView);

                newTagJustAdded = false;
            }


            if (tag.getKey().equals("addNewTag")) {
                holder.itemTagTextView.setBackgroundColor(holder.itemTagTextView.getResources().getColor(R.color.colorPrimary));
                holder.itemTagTextView.setTextColor(holder.itemTagTextView.getResources().getColor(R.color.colorWhite));
            }
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }


    public void selectTagIfSelected(View cardView, TextView itemTagTextView, ViewHolder holder) {
        if (itemTags != null) {

            itemActivity.getNotificationSwitch().setEnabled(true);

            if (itemTags.containsValue(itemTagTextView.getText().toString().trim())) {
                itemTagTextView.setBackgroundColor(itemTagTextView.getResources().getColor(R.color.colorAccent));

                selectedIndexes.add(holder.getAdapterPosition());
                selectedViews.add(cardView);
            }
        }
    }


    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }

    public void setNewTagJustAdded(boolean added) {
        newTagJustAdded = added;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemTagTextView;

        private static ItemActivity itemActivity;

        public ViewHolder(final ItemActivity itemActivity, View cardView) {
            super(cardView);

            this.itemActivity = itemActivity;

            itemTagTextView = (TextView) cardView.findViewById(R.id.item_tag_text_view);

            if (requestCode != TrashFragment.REQUEST_VIEW_ITEM) {

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ///if new tag + is clicked///
                        if (getAdapterPosition() == (tags.size() - 1)) {

                            Intent intent = new Intent(itemActivity, TagActivity.class);
                            intent.putExtra("requestCode", TagsFragment.REQUEST_NEW_TAG);

                            ArrayList<String> tagTexts = new ArrayList<>();

                            for (int i = 0; i < tags.size() - 1; ++i) {
                                tagTexts.add(tags.get(i).getText());
                            }

                            intent.putExtra("tag text list", tagTexts);

                            itemActivity.startActivityForResult(intent, TagsFragment.REQUEST_NEW_TAG);

                        } else {
                            ///selecting///
                            if (!selectedViews.contains(view)) {
                                itemTagTextView.setBackgroundColor(view.getResources().getColor(R.color.colorAccent));

                                if (requestCode != TrashFragment.REQUEST_VIEW_ITEM && selectedViews.size() == 0) {
                                    itemActivity.getNotificationSwitch().setEnabled(true);
                                }

                                selectedIndexes.add(getAdapterPosition());
                                selectedViews.add(view);

                                ///deselecting///
                            } else {
                                itemTagTextView.setBackgroundColor(view.getResources().getColor(R.color.colorWhite));

                                if (selectedViews.size() == 1) {
                                    itemActivity.getNotificationSwitch().setChecked(false);
                                    itemActivity.getNotificationSwitch().setEnabled(false);
                                }

                                selectedIndexes.remove(new Integer(getAdapterPosition()));
                                selectedViews.remove(view);
                            }
                        }
                    }
                });
            }
        }



        public TextView getItemTagTextView() {
            return itemTagTextView;
        }
    }
}
