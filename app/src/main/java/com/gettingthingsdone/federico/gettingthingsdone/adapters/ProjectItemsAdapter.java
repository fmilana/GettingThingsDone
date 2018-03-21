package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ProjectActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 09-Mar-18.
 */

public class ProjectItemsAdapter extends RecyclerView.Adapter<ProjectItemsAdapter.ViewHolder> {

    private static ProjectActivity projectActivity;

    private static String projectKey;

    private ArrayList<Item> projectItems;
    private static ArrayList<Integer> selectedIndexes;
    private static ArrayList<CardView> selectedCards;

    private static boolean selecting;

//    private static Toolbar toolbar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public ProjectItemsAdapter(final ProjectActivity projectActivity, String projectKey, ArrayList<Item> projectItems) {
        this.projectActivity = projectActivity;
        this.projectKey = projectKey;
        this.projectItems = projectItems;
        this.selectedIndexes = new ArrayList<Integer>();
        selectedCards = new ArrayList<CardView>();
//        this.toolbar = ((AppCompatActivity)((Fragment)inTrayFragment).getActivity()).findViewById(R.id.toolbar);

        selecting = false;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectKey).child("projectItems").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if item already exists, then return;
                for (int i = 0; i < projectActivity.getProjectItems().size(); ++i) {
                    if (projectActivity.getProjectItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        projectActivity.getProjectItems().add(item);
                    }
                }

                notifyDataSetChanged();

                if (projectActivity.getProjectItems().size() == 1) {
                    projectActivity.getThisProjectHasNoItems().setVisibility(View.GONE);
                }

//                Item projectItem = dataSnapshot.getValue(Item.class);
//                projectItem.setKey(dataSnapshot.getKey());
//                projectActivity.getProjectItems().add(projectItem);
//                notifyDataSetChanged();
//
//                if (projectActivity.getProjectItems().size() == 1) {
//                    projectActivity.getThisProjectHasNoItems().setVisibility(View.GONE);
//                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

//                Item projectItem = dataSnapshot.getValue(Item.class);
//                projectItem.setKey(dataSnapshot.getKey());
//
//                for (int i = 0; i < projectActivity.getProjectItems().size(); ++i) {
//                    if (projectActivity.getProjectItems().get(i).getKey().equals(dataSnapshot.getKey())) {
//                        projectActivity.getProjectItems().set(i, projectItem);
//                        break;
//                    }
//                }
//
//                notifyDataSetChanged();

                String editedProjectItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedProjectItemKey.equals(item.getKey())) {

                        for (int j = 0; j < projectActivity.getProjectItems().size(); ++j) {

                            if (editedProjectItemKey.equals(projectActivity.getProjectItems().get(j).getKey())) {
                                projectActivity.getProjectItems().set(j, item);
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

//                for (int i = 0; i < projectActivity.getProjectItems().size(); ++i) {
//                    if (projectActivity.getProjectItems().get(i).getKey().equals(dataSnapshot.getKey())) {
//                        projectActivity.getProjectItems().remove(i);
//                    }
//                }
//
//                if (projectActivity.getProjectItems().size() == 0) {
//                    projectActivity.getThisProjectHasNoItems().setVisibility(View.VISIBLE);
//                }
//
//                notifyDataSetChanged();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (dataSnapshot.getKey().equals(item.getKey())) {
                        MainFragmentActivity.getItems().remove(item);
                        break;
                    }
                }

                for (int i = 0; i < projectActivity.getProjectItems().size(); ++i) {
                    if (projectActivity.getProjectItems().get(i).getKey().equals(dataSnapshot.getKey())) {
                        projectActivity.getProjectItems().remove(i);
                        break;
                    }
                }
//
                if (projectActivity.getProjectItems().size() == 0) {
                    projectActivity.getThisProjectHasNoItems().setVisibility(View.VISIBLE);
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
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        ViewHolder viewHolder = new ViewHolder(projectActivity, cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        Item projectItem = projectItems.get(holder.getAdapterPosition());
        holder.itemTextView.setText(projectItem.getText());
    }

    @Override
    public int getItemCount() {
        return projectItems.size();
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

        private static CardView cardView;
        private static TextView itemTextView;

        private int moveIntoListPosition;

        private static ProjectActivity projectActivity;


        public ViewHolder(final ProjectActivity projectActivity, final View view) {

            super(view);

            this.projectActivity = projectActivity;

            itemTextView = (TextView) view.findViewById(R.id.item_text_view);

            cardView = view.findViewById(R.id.item_card_view);

            addCardListeners(cardView);
        }


        private void addCardListeners(CardView cardView) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = projectActivity.getProjectItems().get(getAdapterPosition());

                        intent.putExtra("requestCode", ProjectActivity.REQUEST_EDIT_PROJECT_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item key", selectedItem.getKey());
                        intent.putExtra("project key", projectKey);

                        intent.putExtra("hasNotificationsEnabled", selectedItem.getNotificationsEnabled());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        projectActivity.startActivity(intent);

                    } else {
                        CardView cardView = (CardView)itemView.findViewById(R.id.item_card_view);

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                projectActivity.getMenu().findItem(R.id.menu_delete).setVisible(false);
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

                    CardView cardView = (CardView)itemView.findViewById(R.id.item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    projectActivity.getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

    }

    public ArrayList<Item> getProjectItems() {
        return projectItems;
    }

}