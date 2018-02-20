package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.activities.InTrayNewItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * Created by Federico on 15-Feb-18.
 */

public class ItemTagsAdapter extends RecyclerView.Adapter<ItemTagsAdapter.ViewHolder> {

    private ArrayList<Tag> tags;

    private InTrayNewItemActivity inTrayNewItemActivity;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView itemTagTextView;

        private static InTrayNewItemActivity inTrayNewItemActivity;

        public ViewHolder(final InTrayNewItemActivity inTrayNewItemActivity, View cardView) {
            super(cardView);

            this.inTrayNewItemActivity = inTrayNewItemActivity;

            itemTagTextView = (TextView) cardView.findViewById(R.id.item_tag_text_view);
        }
    }

    @Override
    public ItemTagsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        ViewHolder viewHolder = new ViewHolder(inTrayNewItemActivity, cardView);



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tag tag = tags.get(position);

        holder.setIsRecyclable(false);

        holder.itemTagTextView.setText(tag.getText());
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }


    public ItemTagsAdapter(final InTrayNewItemActivity inTrayNewItemActivity, final ArrayList<Tag> tags) {
        this.tags = tags;

        this.inTrayNewItemActivity = inTrayNewItemActivity;

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if tag already exists, then return;
                for (int i = 0; i < inTrayNewItemActivity.getTags().size(); ++i) {
                    if (inTrayNewItemActivity.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {

                        return;
                    }
                }

                Tag newItemTag = dataSnapshot.getValue(Tag.class);
                newItemTag.setKey(dataSnapshot.getKey());
                inTrayNewItemActivity.getTags().add(newItemTag);

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

}
