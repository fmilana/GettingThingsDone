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
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 02-Mar-18.
 */

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TrashFragment trashFragment;
    private ArrayList<Item> items;

    private static ArrayList<Integer> selectedIndexes;
    private static ArrayList<CardView> selectedCards;

    private static boolean selecting;

    public TrashAdapter(final TrashFragment trashFragment, ArrayList<Item> items) {
        this.trashFragment = trashFragment;
        this.items = items;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < trashFragment.getItems().size(); ++i) {
                    if (trashFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        trashFragment.getItems().add(item);
                        break;
                    }
                }

                if (trashFragment.getItems().size() == 1) {
                    trashFragment.getEmptyTrashText().setVisibility(View.GONE);
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < trashFragment.getItems().size(); ++j) {

                            if (editedItemKey.equals(trashFragment.getItems().get(j).getKey())) {
                                trashFragment.getItems().set(j, item);
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

//                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
//                    Item item = MainFragmentActivity.getItems().get(i);
//
//                    if (dataSnapshot.getKey().equals(item.getKey())) {
//                        MainFragmentActivity.getItems().remove(item);
//                        break;
//                    }
//                }
//
//                for (int i = 0; i < trashFragment.getItems().size(); ++i) {
//                    if (trashFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
//                        trashFragment.getItems().remove(i);
//                        break;
//                    }
//                }
////
//                if (trashFragment.getItems().size() == 0) {
//                    trashFragment.getEmptyTrashText().setVisibility(View.VISIBLE);
//                }
//
//                notifyDataSetChanged();
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

    public static void clearSelected() {
        selectedCards.clear();
        selectedIndexes.clear();
    }

    public static void stopSelecting() {
        selecting = false;
    }







    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static ConstraintLayout constraintLayout;
        private static TextView itemTextView;
        private AppCompatButton restoreButton;

        private static TrashFragment trashFragment;


        public ViewHolder(final TrashFragment trashFragment, final View view) {

            super(view);

            this.trashFragment = trashFragment;

            constraintLayout = (ConstraintLayout) view.findViewById(R.id.trash_item_constraint_layout);

            itemTextView = (TextView) view.findViewById(R.id.trash_item_text_view);

            restoreButton = (AppCompatButton) view.findViewById(R.id.restore_button);


            restoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRestoreDialog();
                }
            });

            addCardListeners(constraintLayout);
        }

        private void restoreItemToInTray(Item item) {
            String editedInTrayItemValue = MainActivity.databaseReference.push().getKey();

            MainActivity.databaseReference.child("users").child(MainActivity.firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).setValue(editedInTrayItemValue);
            MainActivity.databaseReference.child("users").child(MainActivity.firebaseAuth.getCurrentUser().getUid()).child("trash").child(item.getKey()).removeValue();

            trashFragment.getItems().remove(item);

            trashFragment.notifyAdapter();

            if (trashFragment.getItems().size() == 0) {
                trashFragment.getEmptyTrashText().setVisibility(View.VISIBLE);
            }

            Toast.makeText(trashFragment.getActivity(), "Item restored to Trash", Toast.LENGTH_SHORT).show();
        }

        private void addCardListeners(ConstraintLayout constraintLayout) {
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = trashFragment.getItems().get(getAdapterPosition());

                        intent.putExtra("requestCode", TrashFragment.REQUEST_VIEW_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        trashFragment.startActivity(intent);

                    } else {
                        CardView cardView = (CardView)itemView.findViewById(R.id.trash_item_card_view);

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

            builder.setMessage(trashFragment.getItems().get(getAdapterPosition()).getText());

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    restoreItemToInTray(trashFragment.getItems().get(getAdapterPosition()));
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }

}
