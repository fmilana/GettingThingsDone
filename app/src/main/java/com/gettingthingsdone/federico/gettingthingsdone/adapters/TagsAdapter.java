package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.NewTagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 09-Nov-17.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private ArrayList<Tag> tags;

    private TagsFragment tagsFragment;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static ArrayList<CardView> selectedCards;
    private static ArrayList<Integer> selectedIndexes;

    private static boolean selecting = false;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView tagTextView;

        private TagsFragment tagsFragment;

        public ViewHolder(final TagsFragment tagsFragment, View cardView) {
            super(cardView);

            this.tagsFragment = tagsFragment;

            tagTextView = (TextView) cardView.findViewById(R.id.tag_text_view);

            selectedCards = new ArrayList<>();
            selectedIndexes = new ArrayList<>();

            cardView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), NewTagActivity.class);
                        intent.putExtra("requestCode", TagsFragment.REQUEST_EDIT_TAG);
                        intent.putExtra("tag position", getAdapterPosition());
                        intent.putExtra("tag text", TagsFragment.getTags().get(getAdapterPosition()).getText());
                        intent.putExtra("tag key", TagsFragment.getTags().get(getAdapterPosition()).getKey());
                        intent.putExtra("tag time", TagsFragment.getTags().get(getAdapterPosition()).getTime());
                        intent.putExtra("tag address", TagsFragment.getTags().get(getAdapterPosition()).getLocationAddress());;


                        tagsFragment.startActivity(intent);

                    } else {
                        CardView cardView = (CardView)itemView.findViewById(R.id.tag_card_view);

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)((Fragment)tagsFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());
                        }
                    }
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CardView cardView = (CardView)view.findViewById(R.id.tag_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity)((Fragment)tagsFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        public static void clearSelected() {
            selectedCards.clear();
            selectedIndexes.clear();
        }

        public static void stopSelecting() {
            selecting = false;
        }

        public ArrayList<Integer> getSelectedIndexes() {
            return selectedIndexes;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag, parent, false);
        ViewHolder viewHolder = new ViewHolder(tagsFragment, cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TagsAdapter.ViewHolder holder, int position) {
        Tag tag = tags.get(position);

        holder.setIsRecyclable(false);

        holder.tagTextView.setText(tag.getText());
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }




    public TagsAdapter(final TagsFragment tagsFragment, ArrayList<Tag> tags) {
        this.tags = tags;

        this.tagsFragment = tagsFragment;

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if tag already exists, then return;
                for (int i = 0; i < TagsFragment.getTags().size(); ++i) {
                    if (TagsFragment.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }


                Tag newTag = dataSnapshot.getValue(Tag.class);
                newTag.setKey(dataSnapshot.getKey());
                TagsFragment.getTags().add(newTag);
                notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Tag editedTag = dataSnapshot.getValue(Tag.class);
                editedTag.setKey(dataSnapshot.getKey());

                for (int i = 0; i < TagsFragment.getTags().size(); ++i) {
                    if (TagsFragment.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        TagsFragment.getTags().set(i, editedTag);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0; i < TagsFragment.getTags().size(); ++i) {
                    if (TagsFragment.getTags().get(i).getKey().equals(dataSnapshot.getKey())) {
                        TagsFragment.getTags().remove(i);
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

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }
}