package com.gettingthingsdone.federico.gettingthingsdone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by feder on 11-Mar-18.
 */

public class Clarification {

    private InTrayFragment inTrayFragment;

    private ArrayList<Project> projects;

    private Item item;

    private int moveIntoListPosition;
    private int moveIntoProjectPosition;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public Clarification(InTrayFragment inTrayFragment, Item item) {
        this.inTrayFragment = inTrayFragment;
        this.item = item;

        projects = new ArrayList<>();

        firebaseAuth = MainActivity.firebaseAuth;
        databaseReference = MainActivity.databaseReference;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < projects.size(); ++i) {
                    if (projects.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                Project project = dataSnapshot.getValue(Project.class);
                project.setKey(dataSnapshot.getKey());
                projects.add(project);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void showClarifyDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        String itemText = item.getText();

        builder.setTitle(inTrayFragment.getResources().getString(R.string.clarify_item));
        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showSkipDialog();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.clarify, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemActionablePopup();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }



    private void showSkipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.move_item_to));

//            String itemText = item.getText();

        final String[] listItems = {inTrayFragment.getResources().getString(R.string.projects),
                inTrayFragment.getResources().getString(R.string.calendar),
                inTrayFragment.getResources().getString(R.string.waiting_for),
                inTrayFragment.getResources().getString(R.string.maybe_later),
                inTrayFragment.getResources().getString(R.string.reference),
                inTrayFragment.getResources().getString(R.string.trash)};

        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Button moveButton = ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);

                if (!moveButton.isEnabled()) {
                    moveButton.setEnabled(true);
                }

                moveIntoListPosition = i;
            }
        });

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showClarifyDialog();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (moveIntoListPosition == 0) {
                    thisItemBelongsInProjects("showSkipDialog");
                } else if (moveIntoListPosition == 1) {
                    showDatePickerPopup("showSkipDialog");
                } else if (moveIntoListPosition == 2) {
                    moveItemTo("waitingfor");
                } else if (moveIntoListPosition == 3) {
                    moveItemTo("maybelater");
                } else if (moveIntoListPosition == 4) {
                    moveItemTo("reference");
                } else if (moveIntoListPosition == 5) {
                    moveItemTo("trash");
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void isThisItemActionablePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_actionable));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showClarifyDialog();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisTakeLessThanTwoMinutes();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void isThisItemUsefulToRememberPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_useful_to_remember));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemActionablePopup();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInTrashPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInReferencePopup();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doesThisTakeLessThanTwoMinutes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.does_this_item_take_more_than_two_minutes));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemActionablePopup();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doItNowPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject("doesThisTakeLessThanTwoMinutes");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doItNowPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.do_it_now));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisTakeLessThanTwoMinutes();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject("doItNowPopup");
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Item itemToRemove = item;

                MainActivity.databaseReference.child("users").child(MainActivity.firebaseAuth.getCurrentUser().getUid()).child("intray").child(itemToRemove.getKey()).removeValue();

                inTrayFragment.getItems().remove(itemToRemove);

                inTrayFragment.notifyAdapter();

                if (inTrayFragment.getItems().size() == 0) {
                    inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
                }

                Toast.makeText(inTrayFragment.getActivity(), "Item removed from In Tray", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void isThisItemPartOfAProject(final String lastPopup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_part_of_a_project));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (lastPopup.equals("doesThisTakeLessThanTwoMinutes")) {
                    doesThisTakeLessThanTwoMinutes();
                } else if (lastPopup.equals("doItNowPopup")) {
                    doItNowPopup();
                }
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThereADateForThisItemPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInProjects("isThisItemPartOfAProject");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void isThereADateForThisItemPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_there_a_date_for_this_Item));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject("doesThisTakeLessThanTwoMinutes");
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    thisItemBelongsInCalendar();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInCalendar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_calendar));
        builder.setMessage(item.getText());

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThereADateForThisItemPopup();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showDatePickerPopup("thisItemBelongsInCalendar");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void showDatePickerPopup(final String lastPopup) {
        LayoutInflater layoutInflater = (LayoutInflater) inTrayFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View calendarPopupLayout = layoutInflater.inflate(R.layout.popup_date_picker, null);
        CalendarView calendarView = calendarPopupLayout.findViewById(R.id.popup_calendar);
        calendarView.setMinDate(System.currentTimeMillis());

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
        final String defaultDate = simpleDateFormat.format(date);

        final String[] selectedDateContainer =  new String[1];

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                String convertedDay;
                String convertedMonth;
                String convertedYear;

                if (Integer.toString(day).length() == 1) {
                    convertedDay = "0" + day;
                } else {
                    convertedDay = Integer.toString(day);
                }

                if (Integer.toString(month+1).length() == 1) {
                    convertedMonth = "0" + (month+1);
                } else {
                    convertedMonth = Integer.toString(month+1);
                }

                convertedYear = Integer.toString(year);

                selectedDateContainer[0] = convertedDay+convertedMonth+convertedYear;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());
        builder.setTitle(R.string.pick_a_date_for_this_item);
        builder.setView(calendarPopupLayout);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (lastPopup.equals("thisItemBelongsInCalendar")) {
                    thisItemBelongsInCalendar();
                } else {
                    showSkipDialog();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String itemValue =  databaseReference.push().getKey();

                String date;

                if (selectedDateContainer[0] != null) {
                    date = selectedDateContainer[0];
                } else {
                    date = defaultDate;
                }

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").
                        child(date).child(item.getKey()).setValue(itemValue);
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).removeValue();

                inTrayFragment.getItems().remove(item);

                inTrayFragment.notifyAdapter();

                if (inTrayFragment.getItems().size() == 0) {
                    inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
                }

                Toast.makeText(inTrayFragment.getActivity(), "Item moved to Calendar", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doesThisItemNeedToWaitForPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.does_this_item_need_to_wait_for_something_or_someone));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThereADateForThisItemPopup();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInMaybeLaterPopup();
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInWaitForPopup();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInProjects(final String lastPopup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.move_item_to_project));

        final String[] projectTitles = new String[projects.size() + 1];

        for (int i = 0; i < projects.size(); ++i) {
            projectTitles[i] = projects.get(i).getTitle();
        }

        projectTitles[projectTitles.length-1] = "New Project";

        builder.setSingleChoiceItems(projectTitles, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Button moveButton = ((AlertDialog)dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);

                if (!moveButton.isEnabled()) {
                    moveButton.setEnabled(true);
                }

                moveIntoProjectPosition = i;
            }
        });

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (lastPopup.equals("isThisItemPartOfAProject")) {
                    isThisItemPartOfAProject("doesThisTakeLessThanTwoMinutes");
                } else if (lastPopup.equals("showSkipDialog")) {
                    showSkipDialog();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (moveIntoProjectPosition != projectTitles.length-1) {
                    Project project = projects.get(moveIntoProjectPosition);

                    String editedInTrayItemValue = databaseReference.push().getKey();

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).removeValue();
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                            .child("projects").child(project.getKey()).child("projectItems")
                            .child(item.getKey()).setValue(editedInTrayItemValue);

                    inTrayFragment.getItems().remove(item);

                    inTrayFragment.notifyAdapter();

                    if (inTrayFragment.getItems().size() == 0) {
                        inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(inTrayFragment.getActivity(), "Item moved to Projects", Toast.LENGTH_SHORT).show();

                } else {
                    newProjectPopup();
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void newProjectPopup() {
        Context context = inTrayFragment.getActivity();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText projectTitleEditText = new EditText(context);
        projectTitleEditText.setHint("Title");
        projectTitleEditText.setMaxLines(1);
        projectTitleEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        layout.addView(projectTitleEditText);

        final EditText projectDescriptionEditText = new EditText(context);
        projectDescriptionEditText.setHint("Description");
        projectDescriptionEditText.setMaxLines(5);
        projectDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(projectDescriptionEditText);

        int standardPadding = Math.round(inTrayFragment.getResources().getDimension(R.dimen.standard_padding));

        layout.setPadding(standardPadding, standardPadding, standardPadding, standardPadding);

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());
        builder.setTitle(inTrayFragment.getResources().getString(R.string.move_item_to_new_project));

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInProjects("isThisItemPartOfAProject");
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String projectTitle = projectTitleEditText.getText().toString().trim();
                String projectDescription = projectDescriptionEditText.getText().toString().trim();
                HashMap<String, String> projectItems = new HashMap<>();

                projectItems.put(item.getKey(), databaseReference.push().getKey());

                Project newProject = new Project(projectTitle, projectDescription, projectItems);

                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").push().setValue(newProject);
                databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).removeValue();

                inTrayFragment.getItems().remove(item);

                inTrayFragment.notifyAdapter();

                if (inTrayFragment.getItems().size() == 0) {
                    inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
                }

                Toast.makeText(inTrayFragment.getActivity(), "Item moved to Projects", Toast.LENGTH_SHORT).show();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setView(layout);

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        projectTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void thisItemBelongsInMaybeLaterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_maybe_later));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveItemTo("maybelater");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void thisItemBelongsInWaitForPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_waiting_for));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveItemTo("waitingfor");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInTrashPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_trash));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveItemTo("trash");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInReferencePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_reference));

        String itemText = item.getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveItemTo("reference");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void moveItemTo(String list) {

        String editedInTrayItemValue = databaseReference.push().getKey();

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child(list).child(item.getKey()).setValue(editedInTrayItemValue);
        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("intray").child(item.getKey()).removeValue();

        inTrayFragment.getItems().remove(item);

        inTrayFragment.notifyAdapter();

        if (inTrayFragment.getItems().size() == 0) {
            inTrayFragment.getEmptyInTrayText().setVisibility(View.VISIBLE);
        }

        Toast.makeText(inTrayFragment.getActivity(), "Item moved to " + list, Toast.LENGTH_SHORT).show();
    }
}
