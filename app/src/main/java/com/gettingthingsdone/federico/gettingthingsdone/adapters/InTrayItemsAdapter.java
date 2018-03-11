package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Clarification;
import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayItemsAdapter extends RecyclerView.Adapter<InTrayItemsAdapter.ViewHolder> {

    private static InTrayFragment inTrayFragment;

    private ArrayList<Item> items;
    private static ArrayList<Integer> selectedIndexes;
    private static ArrayList<CardView> selectedCards;

    private static boolean selecting;

//    private static Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public InTrayItemsAdapter(final InTrayFragment inTrayFragment, ArrayList<Item> items) {
        this.inTrayFragment = inTrayFragment;
        this.items = items;
        this.selectedIndexes = new ArrayList<Integer>();
        selectedCards = new ArrayList<CardView>();
//        this.toolbar = ((AppCompatActivity)((Fragment)inTrayFragment).getActivity()).findViewById(R.id.toolbar);

        selecting = false;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < inTrayFragment.getItems().size(); ++i) {
                    if (inTrayFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        inTrayFragment.getItems().add(item);
                        break;
                    }
                }

                System.out.println("NOTIFYING ADAPTER");

                notifyDataSetChanged();

                if (inTrayFragment.getItems().size() == 1) {
                    inTrayFragment.getEmptyInTrayText().setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < inTrayFragment.getItems().size(); ++j) {

                            if (editedItemKey.equals(inTrayFragment.getItems().get(j).getKey())) {
                                inTrayFragment.getItems().set(j, item);
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
        private AppCompatButton clarifyButton;

        private static InTrayFragment inTrayFragment;


        public ViewHolder(final InTrayFragment inTrayFragment, final View view) {

            super(view);

            this.inTrayFragment = inTrayFragment;

            itemTextView = (TextView) view.findViewById(R.id.in_tray_item_text_view);

            clarifyButton = (AppCompatButton) view.findViewById(R.id.clarify_button);

            constraintLayout = view.findViewById(R.id.item_constraint_layout);

            addCardListeners(constraintLayout);

            clarifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Clarification clarification = new Clarification(inTrayFragment);

                    clarification.showClarifyDialog(getAdapterPosition());

//                    showClarifyDialog(getAdapterPosition());
                }
            });
        }


        private void addCardListeners(ConstraintLayout cardConstraintLayout) {
            cardConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = inTrayFragment.getItems().get(getAdapterPosition());

                        intent.putExtra("requestCode", InTrayFragment.REQUEST_EDIT_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        inTrayFragment.startActivity(intent);

                    } else {
                        CardView cardView = (CardView) itemView.findViewById(R.id.in_tray_item_card_view);

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
