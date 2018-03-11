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
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 05-Mar-18.
 */

public class WaitingForAdapter extends RecyclerView.Adapter<WaitingForAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private WaitingForFragment waitingForFragment;

    private ArrayList<Item> items;

    private static ArrayList<Integer> selectedIndexes;
    private static ArrayList<CardView> selectedCards;

    private static boolean selecting;



    public WaitingForAdapter(final WaitingForFragment waitingForFragment, ArrayList<Item> items) {
        this.waitingForFragment = waitingForFragment;
        this.items = items;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("waitingfor").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < waitingForFragment.getItems().size(); ++i) {
                    if (waitingForFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        waitingForFragment.getItems().add(item);
                        break;
                    }
                }

                if (waitingForFragment.getItems().size() == 1) {
                    waitingForFragment.getEmptyWaitingForText().setVisibility(View.GONE);
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < waitingForFragment.getItems().size(); ++j) {

                            if (editedItemKey.equals(waitingForFragment.getItems().get(j).getKey())) {
                                waitingForFragment.getItems().set(j, item);
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
    public WaitingForAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.maybe_later_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(waitingForFragment, cardView);
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

        private static WaitingForFragment waitingForFragment;

        private int moveIntoListPosition;

        public ViewHolder(final WaitingForFragment waitingForFragment, final View view) {
            super(view);

            this.waitingForFragment = waitingForFragment;

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

                        Item selectedItem = WaitingForFragment.getItems().get(getAdapterPosition());

                        intent.putExtra("requestCode", WaitingForFragment.REQUEST_EDIT_WAITING_FOR_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        waitingForFragment.startActivity(intent);

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
                                ((MainFragmentActivity)waitingForFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
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

                    ((MainFragmentActivity)waitingForFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        private void showMoveDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(waitingForFragment.getActivity());

            builder.setTitle(waitingForFragment.getResources().getString(R.string.move_item_to));

            final String[] listItems = {waitingForFragment.getResources().getString(R.string.projects),
                    waitingForFragment.getResources().getString(R.string.calendar),
                    waitingForFragment.getResources().getString(R.string.maybe_later),
                    waitingForFragment.getResources().getString(R.string.reference),
                    waitingForFragment.getResources().getString(R.string.trash)};

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
                        moveItemTo(waitingForFragment.getItems().get(getAdapterPosition()), "maybelater");
                    } else if (moveIntoListPosition == 3) {
                        moveItemTo(waitingForFragment.getItems().get(getAdapterPosition()), "reference");
                    } else if (moveIntoListPosition == 4) {
                        moveItemTo(waitingForFragment.getItems().get(getAdapterPosition()), "trash");
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

            waitingForFragment.getItems().remove(item);

            waitingForFragment.notifyAdapter();

            if (waitingForFragment.getItems().size() == 0) {
                waitingForFragment.getEmptyWaitingForText().setVisibility(View.VISIBLE);
            }

            Toast.makeText(waitingForFragment.getActivity(), "Item moved to " + list, Toast.LENGTH_SHORT).show();
        }
    }
}
