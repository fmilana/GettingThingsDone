package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Clarification;
import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayAdapter extends RecyclerView.Adapter<InTrayAdapter.ViewHolder> {

    private InTrayFragment inTrayFragment;

    private ArrayList<Item> items;
    private ArrayList<Integer> selectedIndexes;
    private ArrayList<CardView> selectedCards;

    private boolean selecting;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public InTrayAdapter(final InTrayFragment inTrayFragment) {
        this.inTrayFragment = inTrayFragment;
        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();

        items = new ArrayList<>();

        selecting = false;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

//        addMainActivityItemsListener();

        //////////////////makes it so intraylistener loads only after mainactivity.items are loaded//////////////////
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addInTrayListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {

                    inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
                    inTrayFragment.getProgressBar().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addInTrayListener() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < items.size(); ++i) {
                    if (items.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }


                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {

                        inTrayFragment.getEmptyInTrayText().setVisibility(View.GONE);
                        inTrayFragment.getProgressBar().setVisibility(View.GONE);

                        items.add(item);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < items.size(); ++j) {

                            if (editedItemKey.equals(items.get(j).getKey())) {
                                items.set(j, item);
                                break;
                            }
                        }
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

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

        System.out.println("CREATING VIEWHOLDER");

        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.in_tray_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(inTrayFragment, cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        Item item = items.get(holder.getAdapterPosition());
        holder.itemTextView.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void clearSelected() {
        selectedCards.clear();
        selectedIndexes.clear();
    }

    public void stopSelecting() {
        selecting = false;
    }





    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout cardConstraintLayout;
        private TextView itemTextView;
        private AppCompatButton clarifyButton;
        private CardView cardView;

        private InTrayFragment inTrayFragment;


        public ViewHolder(final InTrayFragment inTrayFragment, final View view) {

            super(view);

            this.inTrayFragment = inTrayFragment;

            itemTextView = (TextView) view.findViewById(R.id.in_tray_item_text_view);

            clarifyButton = (AppCompatButton) view.findViewById(R.id.clarify_button);

            cardView = (CardView) itemView.findViewById(R.id.in_tray_item_card_view);

            cardConstraintLayout = view.findViewById(R.id.item_constraint_layout);

            addCardListeners();

            addClarifyButtonListener();
        }

        private void addClarifyButtonListener() {
            clarifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Item item = items.get(getAdapterPosition());

                        Clarification clarification = new Clarification(inTrayFragment, item);

                        clarification.showClarifyDialog();
                    } else {
                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity) ((Fragment) inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());
                        }
                    }
                }
            });

            clarifyButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CardView cardView = (CardView) itemView.findViewById(R.id.in_tray_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());


                    ((MainFragmentActivity) ((Fragment) inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }


        private void addCardListeners() {
            cardConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = items.get(getAdapterPosition());

                        intent.putExtra("hasNotificationsEnabled", selectedItem.getNotificationsEnabled());

                        intent.putExtra("requestCode", InTrayFragment.REQUEST_EDIT_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        inTrayFragment.startActivity(intent);

                    } else {
                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity) ((Fragment) inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());

                        }
                    }
                }
            });

            cardConstraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView) itemView.findViewById(R.id.in_tray_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    clarifyButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity) ((Fragment) inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }
    }
}
