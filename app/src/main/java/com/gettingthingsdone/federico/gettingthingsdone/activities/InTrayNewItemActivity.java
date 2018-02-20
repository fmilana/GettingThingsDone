package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.InTrayItem;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ItemTagsAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.TagsAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class InTrayNewItemActivity extends AppCompatActivity {

//    private InTrayFragment inTrayFragment;

    private EditText editText;

    private ArrayList<Tag> tags;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

//    private ArrayList<Tag> itemTags = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_tray_new_item);

        tags = new ArrayList<Tag>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        editText = (EditText) findViewById(R.id.in_tray_input_edit_text);
        editText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        if (getIntent().getIntExtra("requestCode", -1) == InTrayFragment.REQUEST_EDIT_ITEM) {

            editText.setText(getIntent().getStringExtra("item text"));
            editText.setSelection(editText.getText().length());

            setTitle(R.string.in_tray_edit_item);

        } else {

            setTitle(R.string.in_tray_new_item);
        }

        recyclerView = findViewById(R.id.item_tags_recycler_view);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ItemTagsAdapter(this, tags);
        recyclerView.setAdapter(adapter);

//        populateContextTagsAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.in_tray_new_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.in_tray_new_item_done:
                if (editText.getText().toString().trim().length() > 0) {

                    if (getIntent().getIntExtra("requestCode", -1) == InTrayFragment.REQUEST_NEW_ITEM) {

                        InTrayItem newInTrayItem = new InTrayItem(editText.getText().toString().trim());
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intrayitems").push().setValue(newInTrayItem);

                    } else if (getIntent().getIntExtra("requestCode", -1) == InTrayFragment.REQUEST_EDIT_ITEM) {

                        InTrayItem editedInTrayItem = new InTrayItem(editText.getText().toString().trim());

                        String itemKey = getIntent().getStringExtra("item key");
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intrayitems").child(itemKey).setValue(editedInTrayItem);
                    }

                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void populateContextTagsAdapter() {
//        itemTags.add(new Tag("tag1"));
//        itemTags.add(new Tag("tag2"));
//        itemTags.add(new Tag("tag3"));
//        itemTags.add(new Tag("tag4"));
//        itemTags.add(new Tag("tag5"));
//        itemTags.add(new Tag("tag6"));
//
//        adapter.notifyDataSetChanged();
//    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }
}
