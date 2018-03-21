package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.adapters.ItemTagsAdapter;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemActivity extends AppCompatActivity {

//    private InTrayFragment inTrayFragment;

    private EditText editText;

    private TextView thisItemHasNoTags;

    private Switch notificationSwitch;

    private int requestCode;

    private ArrayList<Tag> tags;

    private RecyclerView recyclerView;
    private ItemTagsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_in_tray_item);

        requestCode = getIntent().getIntExtra("requestCode", -1);

        tags = new ArrayList<Tag>();

        notificationSwitch = (Switch) findViewById(R.id.notification_switch);

        if (requestCode == TrashFragment.REQUEST_VIEW_ITEM) {

            notificationSwitch.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.notification_switch_text)).setVisibility(View.GONE);

            thisItemHasNoTags = (TextView) findViewById(R.id.this_item_has_no_tags_text);

            thisItemHasNoTags.setVisibility(View.VISIBLE);

        } else {

            notificationSwitch.setEnabled(false);

            System.out.println("requestCode = " + requestCode);

            notificationSwitch.setChecked(getIntent().getBooleanExtra("hasNotificationsEnabled", false));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;


        editText = (EditText) findViewById(R.id.in_tray_input_edit_text);

        if (requestCode == TrashFragment.REQUEST_VIEW_ITEM) {
            editText.setEnabled(false);
            editText.setTextColor(getResources().getColor(R.color.colorBlack));
            editText.setInputType(InputType.TYPE_NULL);
        } else if (requestCode != InTrayFragment.REQUEST_NEW_ITEM){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }


        if (requestCode == InTrayFragment.REQUEST_EDIT_ITEM ||
                requestCode == MaybeLaterFragment.REQUEST_EDIT_MAYBE_LATER_ITEM ||
                requestCode == ReferenceFragment.REQUEST_EDIT_REFERENCE_ITEM ||
                requestCode == WaitingForFragment.REQUEST_EDIT_WAITING_FOR_ITEM ||
                requestCode == ProjectActivity.REQUEST_EDIT_PROJECT_ITEM ||
                requestCode == CalendarFragment.REQUEST_EDIT_CALENDAR_ITEM) {
            editText.setText(getIntent().getStringExtra("item text"));
            editText.setSelection(editText.getText().length());

            setTitle(R.string.edit_item);

        } else if (requestCode == InTrayFragment.REQUEST_NEW_ITEM) {

            setTitle(R.string.new_item);

        } else if (requestCode == TrashFragment.REQUEST_VIEW_ITEM) {

            editText.setText(getIntent().getStringExtra("item text"));
            editText.setSelection(editText.getText().length());

            setTitle(R.string.view_item);
        }

        recyclerView = findViewById(R.id.item_tags_recycler_view);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ItemTagsAdapter(this, tags, requestCode);
        recyclerView.setAdapter(adapter);

        if (requestCode != TrashFragment.REQUEST_VIEW_ITEM){
            addAddNewTag();
        }

        adapter.notifyDataSetChanged();

//        populateContextTagsAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (requestCode != TrashFragment.REQUEST_VIEW_ITEM) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.in_tray_new_item_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (requestCode == ProjectActivity.REQUEST_EDIT_PROJECT_ITEM) {
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                finish();
                return true;
            case R.id.in_tray_new_item_done:
                if (editText.getText().toString().trim().length() > 0) {

                    HashMap<String, String> tagKeys = tagKeysToAdd();

                    final Item item = new Item(editText.getText().toString().trim(), tagKeys, notificationSwitch.isChecked());

                    if (requestCode == InTrayFragment.REQUEST_NEW_ITEM) {

                        final DatabaseReference inTrayDatabaseReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray");

                        ///pushes item to items and key/text pair to intray///
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").push().setValue(item, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                String itemKey = databaseReference.getKey();

                                inTrayDatabaseReference.child(itemKey).setValue(databaseReference.getKey());
                            }
                        });


                    } else if (requestCode == InTrayFragment.REQUEST_EDIT_ITEM) {

                        String itemKey = getIntent().getStringExtra("item key");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(itemKey).setValue(editedInTrayItemValue);

                    } else if (requestCode == MaybeLaterFragment.REQUEST_EDIT_MAYBE_LATER_ITEM) {

                        String itemKey = getIntent().getStringExtra("item key");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("maybelater").child(itemKey).setValue(editedInTrayItemValue);

                    } else if (requestCode == ReferenceFragment.REQUEST_EDIT_REFERENCE_ITEM) {

                        String itemKey = getIntent().getStringExtra("item key");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("reference").child(itemKey).setValue(editedInTrayItemValue);

                    } else if (requestCode == WaitingForFragment.REQUEST_EDIT_WAITING_FOR_ITEM) {

                        String itemKey = getIntent().getStringExtra("item key");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("waitingfor").child(itemKey).setValue(editedInTrayItemValue);

                    } else if (requestCode == ProjectActivity.REQUEST_EDIT_PROJECT_ITEM) {

                        String projectKey = getIntent().getStringExtra("project key");
                        String itemKey = getIntent().getStringExtra("item key");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").child(projectKey).child("projectItems").child(itemKey).setValue(editedInTrayItemValue);

                    } else if (requestCode == CalendarFragment.REQUEST_EDIT_CALENDAR_ITEM) {

                        String itemKey = getIntent().getStringExtra("item key");
                        String day = getIntent().getStringExtra("item day");

                        String editedInTrayItemValue = databaseReference.push().getKey();

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(itemKey).setValue(item);
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(day).child(itemKey).setValue(editedInTrayItemValue);

                    }

                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private HashMap<String, String> tagKeysToAdd() {

        HashMap<String, String> tagKeys = new HashMap<>();

        for (int i = 0; i < adapter.getSelectedIndexes().size(); ++i) {

            Tag selectedTag = tags.get(adapter.getSelectedIndexes().get(i));

            tagKeys.put(selectedTag.getKey(), selectedTag.getText());
        }

        return tagKeys;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void removeAddNewTag() {

        Tag addNewTag = new Tag();

        for (int i = 0; i < tags.size(); ++i) {
            if (tags.get(i).getKey().equals("addNewTag")) {
                addNewTag = tags.get(i);

                break;
            }
        }

        tags.remove(addNewTag);
    }

    public void addAddNewTag() {

        Tag addNewTag = new Tag("New Tag + ");
        addNewTag.setKey("addNewTag");

        tags.add(addNewTag);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TagsFragment.REQUEST_NEW_TAG && resultCode == RESULT_OK) {
            adapter.setNewTagJustAdded(true);

            if (!notificationSwitch.isEnabled()) {
                notificationSwitch.setEnabled(true);
            }
        }
    }

    public Switch getNotificationSwitch() {
        return notificationSwitch;
    }

    public TextView getThisItemHasNoTags() {
        return thisItemHasNoTags;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }
}
