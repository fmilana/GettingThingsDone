package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class NewTagActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private GeoFire geoFire;

    private EditText editText;

    private TextInputEditText timeEditText;
    private TextInputEditText locationEditText;

    private String time;
    private Place place;

    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.new_tag_input_edit_text);
        editText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        timeEditText = (TextInputEditText) findViewById(R.id.new_tag_time_input_edit_text);
        locationEditText = (TextInputEditText) findViewById(R.id.new_tag_location_input_edit_text);

        timeEditText.setInputType(InputType.TYPE_NULL);
        locationEditText.setInputType(InputType.TYPE_NULL);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("taglocations"));

        editText = (EditText)findViewById(R.id.new_tag_input_edit_text);

        if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_EDIT_TAG) {

            editText.setText(getIntent().getStringExtra("tag text"));
            editText.setSelection(editText.getText().length());

            time = getIntent().getStringExtra("tag time");

            timeEditText.setText(time);

            locationEditText.setText(getIntent().getStringExtra("tag address"));

            setTitle(R.string.edit_tag);

        } else {

            setTitle(R.string.new_tag);
        }

        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (timeEditText.getText().length() == 0) {

                    selectTagTime();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewTagActivity.this);

                    builder.setTitle(time);

                    builder.setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            time = null;

                            timeEditText.getText().clear();
                        }
                    });

                    builder.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            selectTagTime();

                        }
                    });

                    AlertDialog dialog = builder.create();

                    dialog.show();
                }
            }
        });

        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (locationEditText.getText().length() == 0) {

                    selectTagLocation();

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(NewTagActivity.this);

                    builder.setTitle(place.getAddress());

                    builder.setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            place = null;

                            locationEditText.getText().clear();
                        }
                    });

                    builder.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            selectTagLocation();

                        }
                    });

                    AlertDialog dialog = builder.create();

                    dialog.show();

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_tag_item_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.new_tag_done:
                if (editText.getText().toString().trim().length() > 0) {
                    if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_NEW_TAG) {

                        Tag newTag = new Tag(editText.getText().toString().trim());

                        if (time != null) {
                            newTag.setTime(time);
                        }

                        if (place != null) {
                            newTag.setLocationKey(place.getId());
                            newTag.setLocationAddress(place.getAddress().toString());

                            geoFire.setLocation(newTag.getLocationKey(), new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(NewTagActivity.this, "Location stored successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").push().setValue(newTag);

                    } else if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_EDIT_TAG) {

                        Tag editedTag = new Tag(editText.getText().toString().trim());

                        if (time != null) {
                            editedTag.setTime(time);
                        }
                        if (place != null) {
                            editedTag.setLocationKey(place.getId());
                            editedTag.setLocationAddress(place.getAddress().toString());


                            geoFire.setLocation(editedTag.getLocationKey(), new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(NewTagActivity.this, "Location stored successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        String tagKey = getIntent().getStringExtra("tag key");
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").child(tagKey).setValue(editedTag);
                    }
                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectTagTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(NewTagActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                String selectedHourAsString;
                String selectedMinuteAsString;

                if (selectedHour < 10) {
                    selectedHourAsString = "0" + selectedHour;
                } else {
                    selectedHourAsString = Integer.toString(selectedHour);
                }

                if (selectedMinute < 10) {
                    selectedMinuteAsString = "0" + selectedMinute;
                } else {
                    selectedMinuteAsString = Integer.toString(selectedMinute);
                }

                time = selectedHourAsString + ":" + selectedMinuteAsString;

                timeEditText.setText(time);

            }
        }, 12, 0, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void selectTagLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

//        builder.setLatLngBounds()

        try {
            startActivityForResult(builder.build(NewTagActivity.this), PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(NewTagActivity.this, "Google Play Services not available at this time", Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(NewTagActivity.this, "Google Play Services not available at this time", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(this, data);

                locationEditText.setText(place.getAddress());
            }
        }
    }
}
