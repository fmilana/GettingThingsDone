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
import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ProjectActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by feder on 09-Mar-18.
 */

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProjectsFragment projectsFragment;

    private ArrayList<Project> projects;

    private ArrayList<Integer> selectedIndexes;
    private ArrayList<CardView> selectedCards;

    private boolean selecting;



    public ProjectsAdapter(final ProjectsFragment projectsFragment) {
        this.projectsFragment = projectsFragment;

        this.projects = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {

                    projectsFragment.getEmptyProjectsText().setVisibility(View.VISIBLE);
                    projectsFragment.getProgressBar().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (int i = 0; i < projects.size(); ++i) {
                    if (projects.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                projectsFragment.getEmptyProjectsText().setVisibility(View.GONE);
                projectsFragment.getProgressBar().setVisibility(View.GONE);

                Project project = dataSnapshot.getValue(Project.class);
                project.setKey(dataSnapshot.getKey());
                projects.add(project);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Project editedProject = dataSnapshot.getValue(Project.class);
                editedProject.setKey(dataSnapshot.getKey());

                for (int i = 0; i < projects.size(); ++i) {
                    if (projects.get(i).getKey().equals(dataSnapshot.getKey())) {
                        projects.set(i, editedProject);
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0; i < projects.size(); ++i) {
                    if (projects.get(i).getKey().equals(dataSnapshot.getKey())) {
                        projects.remove(i);
                    }
                }

                if (projects.size() == 0) {
                    projectsFragment.getEmptyProjectsText().setVisibility(View.VISIBLE);
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
    public ProjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project, parent, false);
        ViewHolder viewHolder = new ViewHolder(projectsFragment, cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        Project project = projects.get(holder.getAdapterPosition());
        holder.projectTitleTextView.setText(project.getTitle());
        holder.projectDescriptionTextView.setText(project.getDescription());
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void clearSelected() {
        selectedCards.clear();
        selectedIndexes.clear();
    }

    public void stopSelecting() {
        selecting = false;
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }

    public ArrayList<Project> getProjects() { return projects; }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView projectTitleTextView;
        private TextView projectDescriptionTextView;

        private ConstraintLayout constraintLayout;
        private CardView cardView;

        private ProjectsFragment projectsFragment;

        public ViewHolder(final ProjectsFragment projectsFragment, final View view) {
            super(view);

            this.projectsFragment = projectsFragment;

            constraintLayout = (ConstraintLayout) view.findViewById(R.id.project_constraint_layout);

            cardView = (CardView) view.findViewById(R.id.project_card_view);

            projectTitleTextView = (TextView) view.findViewById(R.id.project_title_text_view);
            projectDescriptionTextView = (TextView) view.findViewById(R.id.project_description_text_view);

            addCardListeners(cardView);
        }

        private void addCardListeners(final CardView cardView) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ProjectActivity.class);

                        Project selectedProject = projects.get(getAdapterPosition());

                        intent.putExtra("project position", getAdapterPosition());
                        intent.putExtra("project title", selectedProject.getTitle());
                        intent.putExtra("project description", selectedProject.getDescription());
                        intent.putExtra("project key", selectedProject.getKey());

                        intent.putExtra("project items", selectedProject.getProjectItems());


                        projectsFragment.startActivity(intent);

                    } else {

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity)projectsFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
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


                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

                    ((MainFragmentActivity)projectsFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }
    }
}
