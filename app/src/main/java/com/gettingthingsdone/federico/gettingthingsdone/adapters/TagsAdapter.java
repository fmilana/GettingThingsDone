package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;

import com.gettingthingsdone.federico.gettingthingsdone.activities.TagActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Federico on 09-Nov-17.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private ArrayList<Tag> tags;

    private TagsFragment tagsFragment;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private GeoFire geoFire;

    private ArrayList<CardView> selectedCards;
    private ArrayList<Integer> selectedIndexes;

    private boolean selecting = false;

    public TagsAdapter(final TagsFragment tagsFragment) {
        this.tagsFragment = tagsFragment;

        tags = new ArrayList<>();

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        geoFire = new GeoFire(databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("taglocations"));

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    tagsFragment.getYouHaveNoTagsTextView().setVisibility(View.VISIBLE);
                    tagsFragment.getProgressBar().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if tag already exists, then return;
                for (int i = 0; i < tags.size(); ++i) {
                    if (tags.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                tagsFragment.getYouHaveNoTagsTextView().setVisibility(View.GONE);
                tagsFragment.getProgressBar().setVisibility(View.GONE);

                Tag newTag = dataSnapshot.getValue(Tag.class);
                newTag.setKey(dataSnapshot.getKey());
                tags.add(newTag);
                notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Tag editedTag = dataSnapshot.getValue(Tag.class);
                editedTag.setKey(dataSnapshot.getKey());

                for (int i = 0; i < tags.size(); ++i) {
                    if (tags.get(i).getKey().equals(dataSnapshot.getKey())) {
                        tags.set(i, editedTag);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0; i < tags.size(); ++i) {
                    if (tags.get(i).getKey().equals(dataSnapshot.getKey())) {

                        Tag tagToRemove = tags.get(i);

                        if (tagToRemove.getLocationKey() != null) {
                            geoFire.removeLocation(tagToRemove.getLocationKey(), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    System.out.println("Location also removed");
                                }
                            });
                        }


                        /////removes tag from items as well/////////
                        for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {

                            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items")
                                    .child(MainFragmentActivity.getItems().get(j).getKey()).child("itemTags").child(tagToRemove.getKey()).removeValue();

//                            if (itemTagsDatabaseReference != null) {
//                            }
                        }


                        tags.remove(tagToRemove);

                        if (tags.size() == 0) {
                            tagsFragment.getYouHaveNoTagsTextView().setVisibility(View.VISIBLE);
                        }
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

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void clearSelected() {

        for (int i = 0; i < selectedCards.size(); ++i) {
            selectedCards.get(i).setCardBackgroundColor(selectedCards.get(i).getResources().getColor(R.color.colorWhite));
        }

        selectedCards.clear();
        selectedIndexes.clear();
    }

    public void stopSelecting() {
        selecting = false;
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tagTextView;
        private CardView cardView;

        private TagsFragment tagsFragment;

        public ViewHolder(final TagsFragment tagsFragment, View view) {
            super(view);

            this.tagsFragment = tagsFragment;

//            tags = tags;

            tagTextView = (TextView) view.findViewById(R.id.tag_text_view);

            cardView = (CardView) view.findViewById(R.id.tag_card_view);

            selectedCards = new ArrayList<>();
            selectedIndexes = new ArrayList<>();

            addCardListeners();
        }

        private void addCardListeners() {
            cardView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), TagActivity.class);
                        intent.putExtra("requestCode", TagsFragment.REQUEST_EDIT_TAG);
                        intent.putExtra("tag position", getAdapterPosition());
                        intent.putExtra("tag text", tags.get(getAdapterPosition()).getText());
                        intent.putExtra("tag key", tags.get(getAdapterPosition()).getKey());
                        intent.putExtra("tag daysoftheweek", tags.get(getAdapterPosition()).getDaysOfTheWeek());
                        intent.putExtra("tag time", tags.get(getAdapterPosition()).getTime());
                        intent.putExtra("tag location address", tags.get(getAdapterPosition()).getLocationAddress());
                        intent.putExtra("tag location key", tags.get(getAdapterPosition()).getLocationKey());

                        ArrayList<String> tagTexts = new ArrayList<>();


                        for (int i = 0; i < tags.size(); ++i) {
                            tagTexts.add(tags.get(i).getText());
                        }

                        intent.putExtra("tag text list", tagTexts);

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


    }
}