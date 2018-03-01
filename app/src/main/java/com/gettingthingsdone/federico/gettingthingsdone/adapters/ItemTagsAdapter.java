package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.content.ClipData;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.InTrayItem;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.activities.InTrayNewItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.NewTagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Federico on 15-Feb-18.
 */

public class ItemTagsAdapter extends RecyclerView.Adapter<ItemTagsAdapter.ViewHolder> {

    private static ArrayList<Tag> tags;

    private static InTrayNewItemActivity inTrayNewItemActivity;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

//    private static ArrayList<String> selectedTexts;
    private static HashMap<String, String> itemTags;
    private static ArrayList<View> selectedViews;
    private static ArrayList<Integer> selectedIndexes;

    private RecyclerView recyclerView;

    private View cardView;

    private static boolean newTagJustAdded;

    public ItemTagsAdapter(final InTrayNewItemActivity inTrayNewItemActivity, final ArrayList<Tag> tags) {
        this.tags = tags;

        recyclerView = inTrayNewItemActivity.findViewById(R.id.item_tags_recycler_view);

        newTagJustAdded = false;

        System.out.println("TAGS = ");
        for (int i = 0; i < this.tags.size(); ++i) {
            System.out.println(this.tags.get(i).getText());
        }

        if (this.tags.size() == 0) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        this.inTrayNewItemActivity = inTrayNewItemActivity;

        itemTags = (HashMap<String, String>) inTrayNewItemActivity.getIntent().getSerializableExtra("item tags");

//        selectedTexts = new ArrayList<>();
        selectedViews = new ArrayList<>();
        selectedIndexes = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if tag already exists, then return;
                for (int i = 0; i < inTrayNewItemActivity.getTags().size(); ++i) {

                    if (inTrayNewItemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {

                        System.out.println("NOT ADDING TAG WITH KEY " + inTrayNewItemActivity.getTags().get(i).getKey());
                        return;
                    }
                }

                Tag newItemTag = dataSnapshot.getValue(Tag.class);
                newItemTag.setKey(dataSnapshot.getKey());

                inTrayNewItemActivity.removeAddNewTag();
                inTrayNewItemActivity.getTags().add(newItemTag);
                inTrayNewItemActivity.addAddNewTag();

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Tag editedTag = dataSnapshot.getValue(Tag.class);
                editedTag.setKey(dataSnapshot.getKey());

                for (int i = 0; i < inTrayNewItemActivity.getTags().size(); ++i) {
                    if (inTrayNewItemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        inTrayNewItemActivity.getTags().set(i, editedTag);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0; i < inTrayNewItemActivity.getTags().size(); ++i) {
                    if (inTrayNewItemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        inTrayNewItemActivity.getTags().remove(i);
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
        ViewHolder viewHolder = new ViewHolder(inTrayNewItemActivity, cardView);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tag tag = tags.get(position);

        holder.setIsRecyclable(false);

        holder.itemTagTextView.setText(tag.getText());


        selectTagIfSelected(cardView, holder.itemTagTextView, holder);


        if (newTagJustAdded && position == inTrayNewItemActivity.getTags().size()-2) {

            holder.itemTagTextView.setBackgroundColor(holder.itemTagTextView.getResources().getColor(R.color.colorAccent));

            selectedIndexes.add(position);
            selectedViews.add(recyclerView.getChildAt(position));

            newTagJustAdded = false;
        }


        if (tag.getKey().equals("addNewTag")) {
            holder.itemTagTextView.setBackgroundColor(holder.itemTagTextView.getResources().getColor(R.color.colorPrimary));
            holder.itemTagTextView.setTextColor(holder.itemTagTextView.getResources().getColor(R.color.colorWhite));
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }


    public void selectTagIfSelected(View cardView, TextView itemTagTextView, ViewHolder holder) {
        if (itemTags != null) {
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

    public boolean getNewTagJustAdded() {
        return newTagJustAdded;
    }






    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemTagTextView;

        private View cardView;

        private static InTrayNewItemActivity inTrayNewItemActivity;

        public ViewHolder(final InTrayNewItemActivity inTrayNewItemActivity, View cardView) {
            super(cardView);

            this.cardView = cardView;

            this.inTrayNewItemActivity = inTrayNewItemActivity;

            itemTagTextView = (TextView) cardView.findViewById(R.id.item_tag_text_view);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ///if new tag + is clicked///
                    if (getAdapterPosition() == (tags.size()-1)) {

                        Intent intent = new Intent(inTrayNewItemActivity, NewTagActivity.class);
                        intent.putExtra("requestCode", TagsFragment.REQUEST_NEW_TAG);

                        ArrayList<String> tagTexts = new ArrayList<>();

                        for (int i = 0; i < tags.size()-1; ++i) {
                            tagTexts.add(tags.get(i).getText());
                        }

                        intent.putExtra("tag text list", tagTexts);

                        System.out.println("STARTING ACTIVITY WITH REQUESTNEWTAG = " + TagsFragment.REQUEST_NEW_TAG);

                        inTrayNewItemActivity.startActivityForResult(intent, TagsFragment.REQUEST_NEW_TAG);

                    } else {
                        ///selecting///
                        if (!selectedViews.contains(view)) {
                            itemTagTextView.setBackgroundColor(view.getResources().getColor(R.color.colorAccent));

                            selectedIndexes.add(getAdapterPosition());
                            selectedViews.add(view);

                        ///deselecting///
                        } else {
                            itemTagTextView.setBackgroundColor(view.getResources().getColor(R.color.colorWhite));

                            selectedIndexes.remove(new Integer(getAdapterPosition()));
                            selectedViews.remove(view);
                        }
                    }
                }
            });
        }



        public TextView getItemTagTextView() {
            return itemTagTextView;
        }
    }
}
