package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;

import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by feder on 02-Mar-18.
 */

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TrashFragment trashFragment;
    private ArrayList<Item> items;

    private ArrayList<Integer> selectedIndexes;
    private ArrayList<CardView> selectedCards;

    private boolean selecting;

    public TrashAdapter(final TrashFragment trashFragment) {
        this.trashFragment = trashFragment;

        items = new ArrayList<>();

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    trashFragment.getEmptyTrashText().setVisibility(View.VISIBLE);
                    trashFragment.getProgressBar().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash").addChildEventListener(new ChildEventListener() {
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

                        trashFragment.getEmptyTrashText().setVisibility(View.GONE);
                        trashFragment.getProgressBar().setVisibility(View.GONE);

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
    public TrashAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trash_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(trashFragment, cardView);
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

    public void clearSelected() {
        selectedCards.clear();
        selectedIndexes.clear();
    }

    public void stopSelecting() {
        selecting = false;
    }

    public ArrayList<Item> getItems() { return items; }





    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ConstraintLayout constraintLayout;
        private TextView itemTextView;
        private AppCompatButton restoreButton;

        private TrashFragment trashFragment;


        public ViewHolder(final TrashFragment trashFragment, final View view) {

            super(view);

            this.trashFragment = trashFragment;

            constraintLayout = (ConstraintLayout) view.findViewById(R.id.trash_item_constraint_layout);

            cardView = (CardView) view.findViewById(R.id.trash_item_card_view);

            itemTextView = (TextView) view.findViewById(R.id.trash_item_text_view);

            restoreButton = (AppCompatButton) view.findViewById(R.id.restore_button);


            addRestoreItemListener();

            addCardListeners();
        }

        private void addRestoreItemListener() {
            restoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        showRestoreDialog();
                    } else {
                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)((Fragment)trashFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());

                        }
                    }
                }
            });

            restoreButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView)itemView.findViewById(R.id.trash_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity) trashFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        private void restoreItemToInTray(Item item) {
            String editedInTrayItemValue = databaseReference.push().getKey();

            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).setValue(editedInTrayItemValue);
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash").child(item.getKey()).removeValue();

            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(item.getKey()).child("listName").setValue("intray");

            items.remove(item);

            trashFragment.notifyAdapter();

            if (items.size() == 0) {
                trashFragment.getEmptyTrashText().setVisibility(View.VISIBLE);
            }

            Toast.makeText(trashFragment.getActivity(), trashFragment.getResources().getString(R.string.item_restored), Toast.LENGTH_SHORT).show();
        }

        private void addCardListeners() {
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = items.get(getAdapterPosition());

                        intent.putExtra("hasNotificationsEnabled", selectedItem.getNotificationsEnabled());

                        intent.putExtra("requestCode", TrashFragment.REQUEST_VIEW_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        trashFragment.startActivity(intent);

                    } else {
                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)((Fragment)trashFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());

                        }
                    }
                }
            });

            constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView)itemView.findViewById(R.id.trash_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    restoreButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity)((Fragment)trashFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        private void showRestoreDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(trashFragment.getActivity());

            builder.setTitle(trashFragment.getResources().getString(R.string.restore_item_alert_title));

            builder.setMessage(items.get(getAdapterPosition()).getText());

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    restoreItemToInTray(items.get(getAdapterPosition()));
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }

}
