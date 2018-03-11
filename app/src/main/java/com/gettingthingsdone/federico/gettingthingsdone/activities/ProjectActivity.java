package com.gettingthingsdone.federico.gettingthingsdone.activities;

/**
 * Created by feder on 09-Mar-18.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ItemTagsAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ProjectItemsAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.MaybeLaterFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ReferenceFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TrashFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.WaitingForFragment;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectActivity extends AppCompatActivity {

//    private InTrayFragment inTrayFragment;

    private Menu menu;

    private EditText projectTitleEditText;
    private EditText projectDescriptionEditText;

    private TextView thisProjectHasNoItems;

    private static ArrayList<Item> projectItems;

    private String projectKey;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public final static int REQUEST_EDIT_PROJECT_ITEM = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectItems = new ArrayList<Item>();

        thisProjectHasNoItems = (TextView)findViewById(R.id.this_project_has_no_items_textview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        projectTitleEditText = (EditText) findViewById(R.id.project_title_edit_text);
        projectDescriptionEditText = (EditText) findViewById(R.id.project_description_edit_text);

        projectTitleEditText.setText(getIntent().getStringExtra("project title"));
        projectTitleEditText.setSelection(projectTitleEditText.getText().length());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        projectDescriptionEditText.setText(getIntent().getStringExtra("project description"));
        projectDescriptionEditText.setSelection(projectDescriptionEditText.getText().length());

        setTitle(R.string.edit_project);

        recyclerView = findViewById(R.id.project_items_recycler_view);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        projectKey = getIntent().getStringExtra("project key");

        adapter = new ProjectItemsAdapter(this, projectKey, projectItems);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.activity_project_menu, menu);

        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_delete:

                ArrayList<Item> itemsToRemove = new ArrayList<>();

                for (int i = 0; i < ((ProjectItemsAdapter)adapter).getSelectedIndexes().size(); ++i) {
                    itemsToRemove.add(projectItems.get(((ProjectItemsAdapter)adapter).getSelectedIndexes().get(i)));
                }

                for (int i = 0; i < itemsToRemove.size(); ++i) {

                    for (int j = 0; j < MainFragmentActivity.getItems().size(); ++j) {
                        Item mainActivityItem = MainFragmentActivity.getItems().get(j);

                        if (itemsToRemove.get(i).getKey().equals(mainActivityItem.getKey())) {
                            MainFragmentActivity.getItems().remove(mainActivityItem);
                            break;
                        }
                    }

                    for (int j = 0; j < projectItems.size(); ++j) {
                        if (projectItems.get(j).getKey().equals(itemsToRemove.get(i).getKey())) {
                            projectItems.remove(j);
                            break;
                        }
                    }
//
                    if (projectItems.size() == 0) {
                        getThisProjectHasNoItems().setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemsToRemove.get(i).getKey()).removeValue();
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectKey).child("projectItems").child(itemsToRemove.get(i).getKey()).removeValue();
                }

                ((ProjectItemsAdapter)adapter).clearSelected();

                menuItem.setVisible(false);

                ((ProjectItemsAdapter)adapter).stopSelecting();

                if (itemsToRemove.size() > 1) {
                    Toast.makeText(this, "Items deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.in_tray_new_item_done:

                if (projectTitleEditText.getText().toString().trim().length() > 0) {

                    HashMap<String, String> itemKeys = itemKeysToAdd();

                    String projectTitle = projectTitleEditText.getText().toString().trim();
                    String projectDescription = projectDescriptionEditText.getText().toString().trim();

                    Project project = new Project(projectTitle, projectDescription, itemKeys);

                    String projectKey = getIntent().getStringExtra("project key");
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectKey).setValue(project);
                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private HashMap<String, String> itemKeysToAdd() {

        HashMap<String, String> itemKeys = new HashMap<>();

        for (int i = 0; i < projectItems.size(); ++i) {

            Item item = projectItems.get(i);

            itemKeys.put(item.getKey(), item.getText());
        }

        return itemKeys;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static ArrayList<Item> getProjectItems() {
        return projectItems;
    }

    public TextView getThisProjectHasNoItems() {
        return thisProjectHasNoItems;
    }

    public Menu getMenu() {
        return menu;
    }
}
