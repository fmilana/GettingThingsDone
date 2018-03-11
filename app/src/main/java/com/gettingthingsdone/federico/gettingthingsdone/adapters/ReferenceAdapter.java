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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 05-Mar-18.
 */

public class ReferenceAdapter extends RecyclerView.Adapter<ReferenceAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ReferenceFragment referenceFragment;

    private ArrayList<Item> items;

    private static ArrayList<Integer> selectedIndexes;
    private static ArrayList<CardView> selectedCards;

    private static boolean selecting;



    public ReferenceAdapter(final ReferenceFragment referenceFragment, ArrayList<Item> items) {
        this.referenceFragment = referenceFragment;
        this.items = items;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("reference").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < referenceFragment.getItems().size(); ++i) {
                    if (referenceFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        referenceFragment.getItems().add(item);
                        break;
                    }
                }

                if (referenceFragment.getItems().size() == 1) {
                    referenceFragment.getEmptyReferenceText().setVisibility(View.GONE);
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < referenceFragment.getItems().size(); ++j) {

                            if (editedItemKey.equals(referenceFragment.getItems().get(j).getKey())) {
                                referenceFragment.getItems().set(j, item);
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
//                for (int i = 0; i < referenceFragment.getItems().size(); ++i) {
//                    if (referenceFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
//                        referenceFragment.getItems().remove(i);
//                        break;
//                    }
//                }
////
//                if (referenceFragment.getItems().size() == 0) {
//                    referenceFragment.getEmptyReferenceText().setVisibility(View.VISIBLE);
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
    public ReferenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.maybe_later_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(referenceFragment, cardView);
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



    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static CardView cardView;
        private AppCompatButton moveButton;
        private static TextView itemTextView;
        private static ConstraintLayout constraintLayout;

        private static ReferenceFragment referenceFragment;

        private int moveIntoListPosition;

        public ViewHolder(final ReferenceFragment referenceFragment, final View view) {
            super(view);

            this.referenceFragment = referenceFragment;

            constraintLayout = (ConstraintLayout) view.findViewById(R.id.maybe_later_item_constraint_layout);

            itemTextView = (TextView) view.findViewById(R.id.maybe_later_item_text_view);

            cardView = (CardView) view.findViewById(R.id.maybe_later_item_card_view);

            moveButton = (AppCompatButton) view.findViewById(R.id.move_button);

            moveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMoveDialog();
                }
            });

            addCardListeners(constraintLayout);
        }

        private void addCardListeners(ConstraintLayout constraintLayout) {
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = ReferenceFragment.getItems().get(getAdapterPosition());

                        intent.putExtra("requestCode", ReferenceFragment.REQUEST_EDIT_REFERENCE_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        referenceFragment.startActivity(intent);

                    } else {

                        CardView cardView = (CardView)itemView.findViewById(R.id.maybe_later_item_card_view);

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)referenceFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());
                        }
                    }
                }
            });

            constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView)itemView.findViewById(R.id.maybe_later_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

                    ((MainFragmentActivity)referenceFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        private void showMoveDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(referenceFragment.getActivity());

            builder.setTitle(referenceFragment.getResources().getString(R.string.move_item_to));

            final String[] listItems = {referenceFragment.getResources().getString(R.string.projects),
                    referenceFragment.getResources().getString(R.string.calendar),
                    referenceFragment.getResources().getString(R.string.waiting_for),
                    referenceFragment.getResources().getString(R.string.maybe_later),
                    referenceFragment.getResources().getString(R.string.trash)};

            builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Button moveButton = ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);

                    if (!moveButton.isEnabled()) {
                        moveButton.setEnabled(true);
                    }

                    moveIntoListPosition = i;
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (moveIntoListPosition == 2) {
                        moveItemTo(referenceFragment.getItems().get(getAdapterPosition()), "waitingfor");
                    } else if (moveIntoListPosition == 3) {
                        moveItemTo(referenceFragment.getItems().get(getAdapterPosition()), "maybelater");
                    } else if (moveIntoListPosition == 4) {
                        moveItemTo(referenceFragment.getItems().get(getAdapterPosition()), "trash");
                    }
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }

        private void moveItemTo(Item item, String list) {
            String editedInTrayItemValue = MainActivity.databaseReference.push().getKey();

            MainActivity.databaseReference.child("users").child(MainActivity.firebaseAuth.getCurrentUser().getUid()).child(list).child(item.getKey()).setValue(editedInTrayItemValue);
            MainActivity.databaseReference.child("users").child(MainActivity.firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).removeValue();

            referenceFragment.getItems().remove(item);

            referenceFragment.notifyAdapter();

            if (referenceFragment.getItems().size() == 0) {
                referenceFragment.getEmptyReferenceText().setVisibility(View.VISIBLE);
            }

            Toast.makeText(referenceFragment.getActivity(), "Item moved to " + list, Toast.LENGTH_SHORT).show();
        }
    }
}
