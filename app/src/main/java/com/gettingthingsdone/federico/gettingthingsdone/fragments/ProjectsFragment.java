package com.gettingthingsdone.federico.gettingthingsdone.fragments;


import android.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ProjectsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by Federico on 09-Mar-18.
 */

public class ProjectsFragment extends Fragment {

    private static ArrayList<Project> projects;

    private TextView emptyProjectsText;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projects = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        emptyProjectsText = (TextView) view.findViewById(R.id.empty_project_list_textview);

        getActivity().setTitle(R.string.projects);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.projects_recycler_view);
        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ProjectsAdapter(this, projects);
        recyclerView.setAdapter(adapter);

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

            for (int i = 0; i < ((ProjectsAdapter)adapter).getSelectedIndexes().size(); ++i) {
                projectsToRemove.add(projects.get(((ProjectsAdapter)adapter).getSelectedIndexes().get(i)));
            }

            for (int i = 0; i < projectsToRemove.size(); ++i) {
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectsToRemove.get(i).getKey()).removeValue();
            }


            ////remove items when deleting projects///
            for (int i = 0; i < projectsToRemove.size(); ++i) {
                Project project = projectsToRemove.get(i);

                for (String key : project.getProjectItems().keySet()) {
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(key).removeValue();
                }
            }

            ((ProjectsAdapter)adapter).clearSelected();

            ((MainFragmentActivity)getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);

            ((ProjectsAdapter)adapter).stopSelecting();

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

    public static ArrayList<Project> getProjects() {
        return projects;
    }
}
