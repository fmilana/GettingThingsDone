package com.gettingthingsdone.federico.gettingthingsdone.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ProjectActivity;

import com.gettingthingsdone.federico.gettingthingsdone.adapters.ProjectsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Federico on 09-Mar-18.
 */

public class ProjectsFragment extends Fragment {

    private ProjectsAdapter projectsAdapter;

    private TextView emptyProjectsText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private String projectKeyToShow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        projects = new ArrayList<>();

        System.out.println("++++++++++projectsfragmentOnStart+++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        emptyProjectsText = (TextView) view.findViewById(R.id.empty_project_list_textview);
        progressBar = (ProgressBar) view.findViewById(R.id.projects_progress_bar);

        getActivity().setTitle(R.string.projects);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.projects_recycler_view);
        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(layoutManager);

        projectsAdapter = new ProjectsAdapter(this);
        recyclerView.setAdapter(projectsAdapter);

        if (projectsAdapter.getProjects().size() > 0) {
            emptyProjectsText.setVisibility(View.GONE);
        }

        if (projectKeyToShow != null) {

            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        if (childDataSnapshot.getKey().equals(projectKeyToShow)) {
                            Project projectToOpen = childDataSnapshot.getValue(Project.class);

                            if (projectKeyToShow == null) {
                                System.out.println("PROJECTKEYTOSHOW IS NULL BOYS");
                            }

                            Intent intent = new Intent(getActivity(), ProjectActivity.class);

                            intent.putExtra("project title", projectToOpen.getTitle());
                            intent.putExtra("project description", projectToOpen.getDescription());
                            intent.putExtra("project key", childDataSnapshot.getKey());
                            intent.putExtra("project items", projectToOpen.getProjectItems());

                            projectKeyToShow = null;

                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_delete) {

            ArrayList<Project> projectsToRemove = new ArrayList<>();

            for (int i = 0; i < projectsAdapter.getSelectedIndexes().size(); ++i) {
                projectsToRemove.add(projectsAdapter.getProjects().get(projectsAdapter.getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < projectsToRemove.size(); ++i) {
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectsToRemove.get(i).getKey()).removeValue();
            }


            ////remove items when deleting projects///
            for (int i = 0; i < projectsToRemove.size(); ++i) {
                Project project = projectsToRemove.get(i);

                if (project.getProjectItems() != null) {
                    for (String key : project.getProjectItems().keySet()) {
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(key).removeValue();
                    }
                }
            }

            projectsAdapter.clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            projectsAdapter.stopSelecting();

            if (projectsToRemove.size() > 1) {
                Toast.makeText(getActivity(), "Projects deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Project deleted", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    public TextView getEmptyProjectsText() {
        return emptyProjectsText;
    }

    public ArrayList<Project> getProjects() {
        return projectsAdapter.getProjects();
    }

    public ProjectsAdapter getAdapter() {
        return projectsAdapter;
    }

    public ProgressBar getProgressBar() { return progressBar;}

    public void setProjectToShow(String projectKeyToShow) {
        this.projectKeyToShow = projectKeyToShow;
    }
}
