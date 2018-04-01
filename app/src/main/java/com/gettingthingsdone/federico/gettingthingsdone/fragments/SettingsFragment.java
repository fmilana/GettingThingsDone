package com.gettingthingsdone.federico.gettingthingsdone.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by feder on 22-Mar-18.
 */

public class SettingsFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;


    private Switch inTrayRemindersSwitch;

    private ToggleButton mondayToggleButton;
    private ToggleButton tuesdayToggleButton;
    private ToggleButton wednesdayToggleButton;
    private ToggleButton thursdayToggleButton;
    private ToggleButton fridayToggleButton;
    private ToggleButton saturdayToggleButton;
    private ToggleButton sundayToggleButton;

    private Button inTrayRemindersTimeButton;
    private Button calendarNotificationsTimeButton;

    private Switch calendarNotificationsSwitch;

    private Button resetAccountButton;
//    private Button deleteAccountButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        inTrayRemindersSwitch = (Switch) view.findViewById(R.id.settings_intray_reminders_switch);

        mondayToggleButton = (ToggleButton) view.findViewById(R.id.monday_toggle_button);
        tuesdayToggleButton = (ToggleButton) view.findViewById(R.id.tuesday_toggle_button);
        wednesdayToggleButton = (ToggleButton) view.findViewById(R.id.wednesday_toggle_button);
        thursdayToggleButton = (ToggleButton) view.findViewById(R.id.thursday_toggle_button);
        fridayToggleButton = (ToggleButton) view.findViewById(R.id.friday_toggle_button);
        saturdayToggleButton = (ToggleButton) view.findViewById(R.id.saturday_toggle_button);
        sundayToggleButton = (ToggleButton) view.findViewById(R.id.sunday_toggle_button);

        inTrayRemindersTimeButton = (Button) view.findViewById(R.id.settings_intray_reminders_time_button);
        calendarNotificationsTimeButton = (Button) view.findViewById(R.id.settings_calendar_notifications_time_button);

        calendarNotificationsSwitch = (Switch) view.findViewById(R.id.settings_calendar_notification_switch);

        resetAccountButton = (Button) view.findViewById(R.id.reset_account_button);
//        deleteAccountButton = (Button) view.findViewById(R.id.delete_account_button);

        setToggleButtonListener(mondayToggleButton);
        setToggleButtonListener(tuesdayToggleButton);
        setToggleButtonListener(wednesdayToggleButton);
        setToggleButtonListener(thursdayToggleButton);
        setToggleButtonListener(fridayToggleButton);
        setToggleButtonListener(saturdayToggleButton);
        setToggleButtonListener(sundayToggleButton);

        selectInTrayReminderToggleButton();

        selectInTrayRemindersSwitch();

        setInTrayRemindersSwitchListener();

        displayTimeInInTrayReminderTimeButton();
        setInTrayReminderTimeButtonListener();

        selectCalendarNotificationsSwitch();
        setCalendarNotificationsSwitchListener();

        displayTimeInCalendarNotificationTimeButton();
        setCalendarNotificationButtonListener();

        setResetAccountButtonListener();
//        setDeleteAccountButtonListener();

        getActivity().setTitle(R.string.settings);

        return view;
    }

    private void selectInTrayRemindersSwitch() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersEnabled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean enabled = (boolean) dataSnapshot.getValue();

                inTrayRemindersSwitch.setChecked(enabled);

                mondayToggleButton.setEnabled(enabled);
                tuesdayToggleButton.setEnabled(enabled);
                wednesdayToggleButton.setEnabled(enabled);
                thursdayToggleButton.setEnabled(enabled);
                fridayToggleButton.setEnabled(enabled);
                saturdayToggleButton.setEnabled(enabled);
                sundayToggleButton.setEnabled(enabled);

                inTrayRemindersTimeButton.setEnabled(enabled);

                if (enabled) {
                    inTrayRemindersTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                } else {
                    inTrayRemindersTimeButton.setTextColor(getResources().getColor(R.color.grey));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectCalendarNotificationsSwitch() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsEnabled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean enabled = (boolean) dataSnapshot.getValue();

                calendarNotificationsSwitch.setChecked(enabled);

                if (enabled) {
                    calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                } else {
                    calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.grey));
                }

                calendarNotificationsTimeButton.setEnabled(enabled);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectInTrayReminderToggleButton() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String split[] = ((String)dataSnapshot.getValue()).split(" ", 2);

                switch (Integer.parseInt(split[0])) {
                    case 1:
                        mondayToggleButton.setChecked(true);
                        break;
                    case 2:
                        tuesdayToggleButton.setChecked(true);
                        break;
                    case 3:
                        wednesdayToggleButton.setChecked(true);
                        break;
                    case 4:
                        thursdayToggleButton.setChecked(true);
                        break;
                    case 5:
                        fridayToggleButton.setChecked(true);
                        break;
                    case 6:
                        saturdayToggleButton.setChecked(true);
                        break;
                    case 7:
                        sundayToggleButton.setChecked(true);
                        break;
                    default:
                        sundayToggleButton.setChecked(true);
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setInTrayRemindersSwitchListener() {
        inTrayRemindersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersEnabled").setValue(isChecked);
            }
        });
    }

    private void displayTimeInCalendarNotificationTimeButton() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded() && getActivity() != null) {
                    calendarNotificationsTimeButton.setText((String) dataSnapshot.getValue());

                    if (calendarNotificationsSwitch.isChecked()) {
                        calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                    } else {
                        calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.grey));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setCalendarNotificationButtonListener() {
        calendarNotificationsTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialHour = 8;
                int initialMinutes = 00;

                String buttonText = calendarNotificationsTimeButton.getText().toString();

                if (!buttonText.equals(getResources().getString(R.string.time))) {
                    String time[] = buttonText.split(":",2);

                    initialHour = Integer.parseInt(time[0]);
                    initialMinutes = Integer.parseInt(time[1]);
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
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

                        final String time = selectedHourAsString + ":" + selectedMinuteAsString;

                        calendarNotificationsTimeButton.setText(time);
                        calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsTime").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsTime").setValue(time);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }, initialHour, initialMinutes, true);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });
    }

    private void setCalendarNotificationsSwitchListener() {
        calendarNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsEnabled").setValue(isChecked);
            }
        });
    }

    private void setToggleButtonListener(final ToggleButton toggleButton) {
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ToggleButton)view).isChecked()) {
                    if (toggleButton == mondayToggleButton) {
                        tuesdayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(1);

                    } else if (toggleButton == tuesdayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(2);

                    } else if (toggleButton == wednesdayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        tuesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(3);

                    } else if (toggleButton == thursdayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        tuesdayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(4);

                    } else if (toggleButton == fridayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        tuesdayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(5);

                    } else if (toggleButton == saturdayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        tuesdayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        sundayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(6);

                    } else if (toggleButton == sundayToggleButton) {
                        mondayToggleButton.setChecked(false);
                        tuesdayToggleButton.setChecked(false);
                        wednesdayToggleButton.setChecked(false);
                        thursdayToggleButton.setChecked(false);
                        fridayToggleButton.setChecked(false);
                        saturdayToggleButton.setChecked(false);

                        updateInTrayRemindersDay(7);

                    }
                } else {
                    toggleButton.setChecked(true);
                }
            }
        });
    }

    private void updateInTrayRemindersDay(final int day) {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newTime = day + ((String)dataSnapshot.getValue()).substring(1);

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").setValue(newTime);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayTimeInInTrayReminderTimeButton() {
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String split[] = ((String)dataSnapshot.getValue()).split(" ", 2);

                inTrayRemindersTimeButton.setText(split[1]);

                inTrayRemindersTimeButton.setEnabled(inTrayRemindersSwitch.isChecked());

                if (calendarNotificationsSwitch.isChecked()) {
                    calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));
                } else {
                    calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.grey));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setInTrayReminderTimeButtonListener() {
        inTrayRemindersTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int initialHour = 12;
                int initialMinutes = 00;

                String buttonText = inTrayRemindersTimeButton.getText().toString();

                if (!buttonText.equals(getResources().getString(R.string.time))) {
                    String time[] = buttonText.split(":",2);

                    initialHour = Integer.parseInt(time[0]);
                    initialMinutes = Integer.parseInt(time[1]);
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
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

                        final String time = selectedHourAsString + ":" + selectedMinuteAsString;

                        inTrayRemindersTimeButton.setText(time);
                        inTrayRemindersTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String oldTime = (String)dataSnapshot.getValue();
                                String newTime = oldTime.replaceFirst("([0-9]){2}:+([0-9]){2}", time);

                                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").setValue(newTime);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }, initialHour, initialMinutes, true);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });
    }

    private void setResetAccountButtonListener() {
        resetAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(getResources().getString(R.string.reset_account_confirmation_title));
                builder.setMessage(getResources().getString(R.string.reset_account_confirmation_message));

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetAccount();
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
    }

    private void resetAccount() {
        DatabaseReference calendarReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar");
        DatabaseReference inTrayReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray");
        DatabaseReference maybeLaterReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("maybelater");
        DatabaseReference projectsReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects");
        DatabaseReference referenceReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("reference");
        DatabaseReference trashReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("trash");
        DatabaseReference waitingForReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("waitingfor");
        DatabaseReference tagsReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("tags");
        DatabaseReference tagLocationsReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("taglocations");
        DatabaseReference itemsReference = databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items");

        if (calendarReference != null) {
            calendarReference.removeValue();
        }
        if (inTrayReference != null) {
            inTrayReference.removeValue();
        }
        if (maybeLaterReference != null) {
            maybeLaterReference.removeValue();
        }
        if (projectsReference != null) {
            projectsReference.removeValue();
        }
        if (referenceReference != null) {
            referenceReference.removeValue();
        }
        if (trashReference != null) {
            trashReference.removeValue();
        }
        if (waitingForReference != null) {
            waitingForReference.removeValue();
        }
        if (tagsReference != null) {
            tagsReference.removeValue();
        }
        if (tagLocationsReference != null) {
            tagLocationsReference.removeValue();
        }
        if (itemsReference != null) {
            itemsReference.removeValue();
        }

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotifications").setValue(true);
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayReminders").setValue(true);

        mondayToggleButton.setChecked(false);
        tuesdayToggleButton.setChecked(false);
        wednesdayToggleButton.setChecked(false);
        thursdayToggleButton.setChecked(false);
        fridayToggleButton.setChecked(false);
        saturdayToggleButton.setChecked(false);

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("inTrayRemindersTime").setValue("7 12:00");
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendarNotificationsTime").setValue("08:00");

        calendarNotificationsSwitch.setChecked(true);
        inTrayRemindersSwitch.setChecked(true);

        calendarNotificationsTimeButton.setText("08:00");
        calendarNotificationsTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));

        inTrayRemindersTimeButton.setText("12:00");
        inTrayRemindersTimeButton.setTextColor(getResources().getColor(R.color.colorBlack));
    }

//    private void setDeleteAccountButtonListener() {
//        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//                builder.setTitle(getResources().getString(R.string.delete_account_confirmation_title));
//                builder.setMessage(getResources().getString(R.string.delete_account_confirmation_message));
//
//                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
//
//                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        askForPassword();
//                    }
//                });
//
//                AlertDialog dialog = builder.create();
//
//                dialog.show();
//            }
//        });
//    }

//    private void askForPassword() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        builder.setTitle(getResources().getString(R.string.enter_password_to_delete_account));
//
//        LinearLayout layout = new LinearLayout(getActivity());
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        int standardPadding = Math.round(getResources().getDimension(R.dimen.standard_padding));
//        layout.setPadding(standardPadding, standardPadding, standardPadding, standardPadding);
//
//        final EditText passwordEditText = new EditText(getActivity());
//        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//
//        layout.addView(passwordEditText);
//
//        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        });
//
//        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                deleteAccount(passwordEditText.getText().toString());
//            }
//        });
//
//        final AlertDialog dialog = builder.create();
//        dialog.setView(layout);
//
//        dialog.show();
//
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//
//        passwordEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length() > 0) {
//                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
//                } else {
//                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//
//        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                }
//            }
//        });
//    }

//    private void deleteAccount(String password) {
//
//        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        final String currentUserId = currentUser.getUid();
//
//        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
//
//        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//
//                    Intent intent = new Intent(getActivity(), LogInActivity.class);
//                    startActivity(intent);
//
//                    SplashActivity.firebaseAuth = null;
//
//                    getActivity().finish();
//
//                    FirebaseAuth.getInstance().signOut();
//
//                    currentUser.delete();
//
////                    databaseReference.child("users").child(currentUserId).removeValue();
//                } else {
//                    askForPassword();
//
//                    Toast.makeText(getActivity(), getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
}
