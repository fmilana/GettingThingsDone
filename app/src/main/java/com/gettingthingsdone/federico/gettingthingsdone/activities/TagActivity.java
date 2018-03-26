package com.gettingthingsdone.federico.gettingthingsdone.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.Tag;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.TagsFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class TagActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private GeoFire geoFire;

    private EditText textEditText;

    private ToggleButton mondayToggleButton;
    private ToggleButton tuesdayToggleButton;
    private ToggleButton wednesdayToggleButton;
    private ToggleButton thursdayToggleButton;
    private ToggleButton fridayToggleButton;
    private ToggleButton saturdayToggleButton;
    private ToggleButton sundayToggleButton;

    private Button timeButton;
    private Button locationButton;

    private boolean timeSet;
    private boolean locationSet;

    private String time;
    private String locationAddress;
    private Place place;

    private String oldLocationKey;

    private GeoLocation selectedLocation;

    private Tag editedTag;


    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textEditText = (EditText) findViewById(R.id.new_tag_input_edit_text);

        if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_NEW_TAG) {
            textEditText.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_EDIT_TAG) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }


        mondayToggleButton = (ToggleButton) findViewById(R.id.monday_toggle_button);
        tuesdayToggleButton = (ToggleButton) findViewById(R.id.tuesday_toggle_button);
        wednesdayToggleButton = (ToggleButton) findViewById(R.id.wednesday_toggle_button);
        thursdayToggleButton = (ToggleButton) findViewById(R.id.thursday_toggle_button);
        fridayToggleButton = (ToggleButton) findViewById(R.id.friday_toggle_button);
        saturdayToggleButton = (ToggleButton) findViewById(R.id.saturday_toggle_button);
        sundayToggleButton = (ToggleButton) findViewById(R.id.sunday_toggle_button);

        timeButton = (Button) findViewById(R.id.new_tag_time_button);
        locationButton = (Button) findViewById(R.id.new_tag_location_button);

        timeSet = false;
        locationSet = false;

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        geoFire = new GeoFire(databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("taglocations"));

        textEditText = (EditText)findViewById(R.id.new_tag_input_edit_text);

        oldLocationKey = getIntent().getStringExtra("tag location key");

        if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_EDIT_TAG) {

            textEditText.setText(getIntent().getStringExtra("tag text"));
            textEditText.setSelection(textEditText.getText().length());

            String tagDaysOfTheWeek = getIntent().getStringExtra("tag daysoftheweek");

            if (tagDaysOfTheWeek.charAt(0) == '1') {
                mondayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(1) == '1') {
                tuesdayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(2) == '1') {
                wednesdayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(3) == '1') {
                thursdayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(4) == '1') {
                fridayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(5) == '1') {
                saturdayToggleButton.setChecked(true);
            }
            if (tagDaysOfTheWeek.charAt(6) == '1') {
                sundayToggleButton.setChecked(true);
            }

            time = getIntent().getStringExtra("tag time");
            locationAddress = getIntent().getStringExtra("tag location address");

            if (time != null) {
                timeButton.setText(time);
                timeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                timeSet = true;
            }

            if (locationAddress != null) {
                locationButton.setText(locationAddress);
                locationButton.setTextColor(getResources().getColor(R.color.colorBlack));
                locationSet = true;
            }

            setTitle(R.string.edit_tag);

        } else {

            setTitle(R.string.new_tag);
        }

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!timeSet) {

                    selectTagTime();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);

                    builder.setTitle(R.string.tag_time_alert_title);
                    builder.setMessage(time);

                    builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            time = null;

                            timeButton.setText(R.string.time);
                            timeButton.setTextColor(getResources().getColor(R.color.grey));
                            timeSet = false;

                            selectedLocation = null;

                        }
                    });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

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

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!locationSet) {

                    selectTagLocation();

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);

                    builder.setTitle(R.string.tag_location_alert_title);
                    builder.setMessage(locationButton.getText().toString().trim());

                    builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            place = null;

                            locationButton.setText(R.string.location);
                            locationButton.setTextColor(getResources().getColor(R.color.grey));
                            locationSet = false;

                            if (selectedLocation != null) {
                                selectedLocation = null;
                            }
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

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

                String tagText = textEditText.getText().toString().trim();

                if (tagText.length() > 0) {
                    if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_NEW_TAG) {

                        ArrayList<String> tagTexts = getIntent().getStringArrayListExtra("tag text list");

                        //checks if a tag with the same name exists
                        for (int i = 0; i < tagTexts.size(); ++i) {
                            if (tagTexts.get(i).equals(tagText)) {

                                Toast.makeText(this, "Tag with that name already exists", Toast.LENGTH_SHORT).show();

                                return true;
                            }
                        }


                        Tag newTag = new Tag(tagText);


                        String daysOfTheWeek = daysOfTheWeekToString();

                        newTag.setDaysOfTheWeek(daysOfTheWeek);


                        if (time != null) {
                            newTag.setTime(time);
                        }

                        if (place != null) {
                            newTag.setLocationKey(place.getId());
                            newTag.setLocationAddress(place.getAddress().toString());

                            geoFire.setLocation(place.getId(), new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    Toast.makeText(TagActivity.this, "Location stored successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").push().setValue(newTag);


                        setResult(Activity.RESULT_OK);

                    } else if (getIntent().getIntExtra("requestCode", -1) == TagsFragment.REQUEST_EDIT_TAG) {

                        ArrayList<String> tagTexts = getIntent().getStringArrayListExtra("tag text list");

                        int tagPosition = getIntent().getIntExtra("tag position", -1);

                        //checks if a tag with the same name exists and it's not the one being edited
                        for (int i = 0; i < tagTexts.size(); ++i) {
                            if (tagTexts.get(i).equals(tagText) && i != tagPosition) {

                                Toast.makeText(this, "Tag with that name already exists", Toast.LENGTH_SHORT).show();

                                return true;
                            }
                        }

                        editedTag = new Tag(tagText);

                        String daysOfTheWeek = daysOfTheWeekToString();

                        editedTag.setDaysOfTheWeek(daysOfTheWeek);


                        if (time != null) {
                            editedTag.setTime(time);
                        }

                        String locationKey = getIntent().getStringExtra("tag location key");

                        if (locationButton.getText().length() != 0) {

                            if (place != null) {

                                editedTag.setLocationKey(place.getId());
                                editedTag.setLocationAddress(place.getAddress().toString());

                                geoFire.setLocation(place.getId(), new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        Toast.makeText(TagActivity.this, "Location stored successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            ///PLACE WAS NOT SET BUT WAS ALREADY THERE///
                            } else if (locationKey != null) {

                                GeoDataClient geoDataClient = Places.getGeoDataClient(this, null);

                                geoDataClient.getPlaceById(locationKey).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                                    @Override
                                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                        if (task.isSuccessful()) {
                                            PlaceBufferResponse places = task.getResult();
                                            place = places.get(0);

                                            editedTag.setLocationKey(place.getId());

                                            editedTag.setLocationAddress(place.getAddress().toString());

                                            String tagKey = getIntent().getStringExtra("tag key");
                                            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags").child(tagKey).setValue(editedTag);

                                            places.release();

                                        } else {
                                            Toast.makeText(TagActivity.this, "There was something wrong with the tag's location. Please delete it and make a new one", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                finish();
                                return true;

                            }

                            //////////////////////////////////////////////////////////////////////
                            if (oldLocationKey != null && !oldLocationKey.equals(locationKey)) {

                                geoFire.removeLocation(oldLocationKey, new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        System.out.println("Old location deleted");
                                    }
                                });
                            }
                        }

                        else {
                            if (oldLocationKey != null) {
                                geoFire.removeLocation(oldLocationKey, new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        System.out.println("Old location deleted");
                                    }
                                });
                            }
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
        int initialHour = 12;
        int initialMinutes = 00;

        String buttonText = timeButton.getText().toString();

        if (!buttonText.equals(getResources().getString(R.string.time))) {
            String time[] = buttonText.split(":",2);

            initialHour = Integer.parseInt(time[0]);
            initialMinutes = Integer.parseInt(time[1]);
        }


        TimePickerDialog timePickerDialog = new TimePickerDialog(TagActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                timeButton.setText(time);
                timeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                timeSet = true;

            }
        }, initialHour, initialMinutes, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void selectTagLocation() {
        if (!locationSet) {

            setUpPlacePicker(null);

        } else if (selectedLocation != null) {

            setUpPlacePicker(selectedLocation);

        } else if (oldLocationKey != null) {

            geoFire.getLocation(oldLocationKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {

                        setUpPlacePicker(location);

                    } else {
                        System.out.println(String.format("There is no location for key %s in GeoFire", key));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("There was an error getting the GeoFire location: " + databaseError);
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            place = PlacePicker.getPlace(this, data);

            locationButton.setText(place.getAddress());
            locationButton.setTextColor(getResources().getColor(R.color.colorBlack));
            locationSet = true;

            selectedLocation = new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude);
        }
    }

    private void setUpPlacePicker(GeoLocation location) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        if (location != null) {

            LatLng bottomLeft = new LatLng(location.latitude - 0.005, location.longitude - 0.005);
            LatLng topRight = new LatLng(location.latitude + 0.005, location.longitude + 0.005);

            LatLngBounds bounds = new LatLngBounds(bottomLeft, topRight);

            builder.setLatLngBounds(bounds);
        }

        try {
            startActivityForResult(builder.build(TagActivity.this), PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(TagActivity.this, "Google Play Services not available at this time", Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(TagActivity.this, "Google Play Services not available at this time", Toast.LENGTH_SHORT).show();
        }
    }

    private String daysOfTheWeekToString() {
        String daysOfTheWeek = "";

        if (mondayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (tuesdayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (wednesdayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (thursdayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (fridayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (saturdayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }
        if (sundayToggleButton.isChecked()) {
            daysOfTheWeek += "1";
        } else {
            daysOfTheWeek += "0";
        }

        return daysOfTheWeek;
    }
}
