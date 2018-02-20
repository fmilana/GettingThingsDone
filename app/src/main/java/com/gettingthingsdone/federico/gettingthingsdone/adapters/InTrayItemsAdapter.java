package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.InTrayItem;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.InTrayNewItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayItemsAdapter extends RecyclerView.Adapter<InTrayItemsAdapter.ViewHolder> {

    private InTrayFragment inTrayFragment;

    private ArrayList<InTrayItem> items;
    private static ArrayList<Integer> selectedIndexes;

    private static Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static TextView itemTextView;
        private static ArrayList<CardView> selectedCards;
        private static boolean selecting = false;

        private static InTrayFragment inTrayFragment;


        public ViewHolder(final InTrayFragment inTrayFragment, final View view) {

            super(view);

            this.inTrayFragment = inTrayFragment;

            itemTextView = (TextView) view.findViewById(R.id.in_tray_item_text_view);
            selectedCards = new ArrayList<CardView>();

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), InTrayNewItemActivity.class);
                        intent.putExtra("requestCode", InTrayFragment.REQUEST_EDIT_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", inTrayFragment.getItems().get(getAdapterPosition()).getText());
                        intent.putExtra("item key", inTrayFragment.getItems().get(getAdapterPosition()).getKey());


                        inTrayFragment.startActivity(intent);

                    } else {
                        CardView cardView = (CardView)itemView.findViewById(R.id.in_tray_item_card_view);

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)((Fragment)inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView)itemView.findViewById(R.id.in_tray_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity)((Fragment)inTrayFragment).getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

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

        InTrayItem inTrayItem = items.get(holder.getAdapterPosition());
        holder.itemTextView.setText(inTrayItem.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }


    public InTrayItemsAdapter(final InTrayFragment inTrayFragment, ArrayList<InTrayItem> items) {
        this.inTrayFragment = inTrayFragment;
        this.items = items;
        this.selectedIndexes = new ArrayList<Integer>();
        this.toolbar = ((AppCompatActivity)((Fragment)inTrayFragment).getActivity()).findViewById(R.id.toolbar);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intrayitems").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if item already exists, then return;
                for (int i = 0; i < inTrayFragment.getItems().size(); ++i) {
                    if (inTrayFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                InTrayItem newItem = dataSnapshot.getValue(InTrayItem.class);
                newItem.setKey(dataSnapshot.getKey());
                inTrayFragment.getItems().add(newItem);
                notifyDataSetChanged();

           }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                InTrayItem editedItem = dataSnapshot.getValue(InTrayItem.class);
                editedItem.setKey(dataSnapshot.getKey());

                for (int i = 0; i < inTrayFragment.getItems().size(); ++i) {
                    if (inTrayFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        inTrayFragment.getItems().set(i, editedItem);
                        break;
                    }
                }


                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                for (int i = 0; i < inTrayFragment.getItems().size(); ++i) {
                    if (inTrayFragment.getItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        inTrayFragment.getItems().remove(i);
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

//    public void updateAdapters() {
//        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
//        ItemTagsAdapter itemTagsAdapter = (ItemTagsAdapter) ((RecyclerView) layoutInflater.inflate(R.layout.activity_in_tray_new_item, false).findViewById(R.id.item_tags_recycler_view)).getListAdapter();
//    }
}
