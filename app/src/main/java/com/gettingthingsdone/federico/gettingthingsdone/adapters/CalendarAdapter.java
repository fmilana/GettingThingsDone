package com.gettingthingsdone.federico.gettingthingsdone.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.Item;
import com.gettingthingsdone.federico.gettingthingsdone.Project;
import com.gettingthingsdone.federico.gettingthingsdone.R;
import com.gettingthingsdone.federico.gettingthingsdone.activities.ItemActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.LogInActivity;
import com.gettingthingsdone.federico.gettingthingsdone.activities.MainFragmentActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.CalendarFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by feder on 12-Mar-18.
 */

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private CalendarFragment calendarFragment;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ArrayList<Item> items;

    private ArrayList<Project> projects;

    private Item movingItem;

    private int moveIntoProjectPosition;
    private int moveIntoListPosition;

    private String selectedDate;

    private ArrayList<Integer> selectedIndexes;
    private ArrayList<CardView> selectedCards;
    private boolean selecting;

    public CalendarAdapter(final CalendarFragment calendarFragment, final String selectedDate) {
        this.calendarFragment = calendarFragment;
        this.selectedDate = selectedDate;

        System.out.println("+++++++++++onstart+++++++++++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

        System.out.println("STARTING CALENDAR ADAPTER WITH SELECTEDDATE = " + selectedDate);

        items = new ArrayList<>();

        projects = new ArrayList<>();

        firebaseAuth = LogInActivity.firebaseAuth;
        databaseReference = LogInActivity.databaseReference;

        selectedIndexes = new ArrayList<>();
        selectedCards = new ArrayList<>();
        selecting = false;

        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {

                    calendarFragment.getEmptyCalendarItemsText().setVisibility(View.VISIBLE);
                    calendarFragment.getProgressBar().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < items.size(); ++i) {
                    if (items.get(i).getKey().equals(dataSnapshot.getKey())) {
                        return;
                    }
                }

                System.out.println("++++++++++++++++++++++++++++++++++ MainFragmentActivity.getItems().size() = "+ MainFragmentActivity.getItems().size());

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);


//                    System.out.println("++++++++++++++++ COMPARING " + dataSnapshot.getKey() + " WITH " + item.getKey());

                    if (dataSnapshot.getKey().equals(item.getKey())) {
//                        System.out.println("+++++++++++++++++++++++++++++++++ IDENTIFIED ITEM " + item.getText());

                        calendarFragment.getEmptyCalendarItemsText().setVisibility(View.GONE);
                        calendarFragment.getProgressBar().setVisibility(View.GONE);

                        items.add(item);
                        break;
                    }
                }

//                System.out.println("++++++++++++++++++++++++++++++++++++++++ NOTIFYING");

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String editedItemKey = dataSnapshot.getKey();

                for (int i = 0; i < MainFragmentActivity.getItems().size(); ++i) {
                    Item item = MainFragmentActivity.getItems().get(i);

                    if (editedItemKey.equals(item.getKey())) {

                        for (int j = 0; j < items.size(); ++j) {

                            if (editedItemKey.equals(items.get(j).getKey())) {
                                items.set(j, item);
                                break;
                            }
                        }
                        break;
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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

                Project project = dataSnapshot.getValue(Project.class);
                project.setKey(dataSnapshot.getKey());
                projects.add(project);
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
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                for (int i = 0; i < projects.size(); ++i) {
                    Project project = projects.get(i);

                    if (project.getKey().equals(dataSnapshot.getKey())) {
                        projects.remove(project);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public CalendarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_with_move_button, parent, false);
        CalendarAdapter.ViewHolder viewHolder = new CalendarAdapter.ViewHolder(calendarFragment, cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CalendarAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        Item item = items.get(holder.getAdapterPosition());
        holder.itemTextView.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }


    public void clearSelected() {
        selectedCards.clear();
        selectedIndexes.clear();
    }

    public void stopSelecting() {
        selecting = false;
        ((MainFragmentActivity) calendarFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
    }

    public ArrayList<Item> getItems() {
        return items;
    }






    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView itemTextView;

        private Button moveButton;

        private ConstraintLayout constraintLayout;

        private CalendarFragment calendarFragment;

        public ViewHolder(final CalendarFragment calendarFragment, final View view) {
            super(view);

            this.calendarFragment = calendarFragment;

            itemTextView = (TextView) view.findViewById(R.id.maybe_later_item_text_view);

            moveButton = (Button) view.findViewById(R.id.move_button);

            cardView = (CardView) view.findViewById(R.id.maybe_later_item_card_view);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.maybe_later_item_constraint_layout);

            addCardListeners();

            addMoveButtonListener();
        }

        private void addMoveButtonListener() {
            moveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        showMoveDialog();
                    } else {
                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity) calendarFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());
                        }
                    }
                }
            });

            moveButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    CardView cardView = (CardView)itemView.findViewById(R.id.maybe_later_item_card_view);

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

//                    toolbar.setBackgroundResource(R.color.colorPrimaryLight);

                    ((MainFragmentActivity) calendarFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }

        private void addCardListeners() {
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selecting) {
                        Intent intent = new Intent(view.getContext(), ItemActivity.class);

                        Item selectedItem = items.get(getAdapterPosition());

                        intent.putExtra("hasNotificationsEnabled", selectedItem.getNotificationsEnabled());

                        intent.putExtra("requestCode", CalendarFragment.REQUEST_EDIT_CALENDAR_ITEM);
                        intent.putExtra("item position", getAdapterPosition());
                        intent.putExtra("item text", selectedItem.getText());
                        intent.putExtra("item day", selectedDate);
                        intent.putExtra("item key", selectedItem.getKey());

                        intent.putExtra("item tags", selectedItem.getItemTags());


                        calendarFragment.startActivity(intent);

                    } else {

                        //deselecting card
                        if (selectedCards.contains(cardView)) {
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorLightGrey));

                            selectedCards.remove(cardView);
                            selectedIndexes.remove(new Integer(getAdapterPosition()));

                            if (selectedCards.size() == 0) {
                                selecting = false;
//                                toolbar.setBackgroundResource(R.color.colorPrimary);
                                ((MainFragmentActivity) calendarFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(false);
                            }

                        } else {
                            //adding another card to the selection
                            cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                            moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                            selectedCards.add(cardView);
                            selectedIndexes.add(getAdapterPosition());
                        }
                    }
                }
            });

            constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    cardView.setCardBackgroundColor(view.getResources().getColor(R.color.colorLightGrey2));
                    moveButton.setBackgroundColor(view.getResources().getColor(R.color.colorClarifyButtonSelected));

                    selecting = true;

                    selectedCards.add(cardView);
                    selectedIndexes.add(getAdapterPosition());

                    ((MainFragmentActivity) calendarFragment.getActivity()).getMenu().findItem(R.id.menu_delete).setVisible(true);

                    return true;
                }
            });
        }


        private void showMoveDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(calendarFragment.getActivity());

            movingItem = items.get(getAdapterPosition());

            builder.setTitle(calendarFragment.getActivity().getResources().getString(R.string.move_item_to));

            final String[] listItems = {calendarFragment.getActivity().getResources().getString(R.string.projects),
                    calendarFragment.getActivity().getResources().getString(R.string.calendar),
                    calendarFragment.getActivity().getResources().getString(R.string.waiting_for),
                    calendarFragment.getActivity().getResources().getString(R.string.maybe_later),
                    calendarFragment.getActivity().getResources().getString(R.string.reference),
                    calendarFragment.getActivity().getResources().getString(R.string.trash)};

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

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (moveIntoListPosition == 0) {
                        moveItemToProjectPopup();
                    } else if (moveIntoListPosition == 1) {
                        showDatePickerPopup();
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

        private void moveItemToProjectPopup() {
            AlertDialog.Builder builder = new AlertDialog.Builder(calendarFragment.getActivity());

            builder.setTitle(calendarFragment.getActivity().getResources().getString(R.string.move_item_to_project));

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
                    showMoveDialog();
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

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).child(movingItem.getKey()).removeValue();
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid())
                                .child("projects").child(project.getKey()).child("projectItems")
                                .child(movingItem.getKey()).setValue(editedInTrayItemValue);

                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(movingItem.getKey()).child("listName").setValue("projects " + project.getKey());

                        items.remove(movingItem);

                        calendarFragment.notifyAdapter();

                        if (items.size() == 0) {
                            calendarFragment.getEmptyCalendarItemsText().setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(calendarFragment.getActivity(), "Item moved to Projects", Toast.LENGTH_SHORT).show();

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
            Context context = calendarFragment.getActivity();
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText projectTitleEditText = new EditText(context);
            projectTitleEditText.setHint("Title");
            projectTitleEditText.setMaxLines(1);
            projectTitleEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(projectTitleEditText);

            final EditText projectDescriptionEditText = new EditText(context);
            projectDescriptionEditText.setHint("Description");
            projectDescriptionEditText.setMaxLines(1);
            projectDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(projectDescriptionEditText);

            int standardPadding = Math.round(calendarFragment.getActivity().getResources().getDimension(R.dimen.standard_padding));

            layout.setPadding(standardPadding, standardPadding, standardPadding, standardPadding);

            AlertDialog.Builder builder = new AlertDialog.Builder(calendarFragment.getActivity());
            builder.setTitle(calendarFragment.getActivity().getString(R.string.move_item_to_new_project));

            builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    moveItemToProjectPopup();
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

                    projectItems.put(movingItem.getKey(), databaseReference.push().getKey());

                    Project newProject = new Project(projectTitle, projectDescription, projectItems);

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("projects").push().setValue(newProject, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            System.out.println("CREATING NEW PROJECT AND GIVING PROJECTKEY " + databaseReference.getKey() + " TO ITEM");

                            LogInActivity.databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(movingItem.getKey()).child("listName").setValue("projects " + databaseReference.getKey());
                        }
                    });
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).child(movingItem.getKey()).removeValue();

//                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(movingItem.getKey()).child("listName").setValue("projects " + newProject.getKey());

                    projectItems.remove(movingItem);

                    calendarFragment.notifyAdapter();

                    if (projectItems.size() == 0) {
                        calendarFragment.getEmptyCalendarItemsText().setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(calendarFragment.getActivity(), "Item moved to Projects", Toast.LENGTH_SHORT).show();
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

        private void showDatePickerPopup() {
            LayoutInflater layoutInflater = (LayoutInflater) calendarFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View calendarPopupLayout = layoutInflater.inflate(R.layout.popup_date_picker, null);
            final CalendarView calendarView = calendarPopupLayout.findViewById(R.id.popup_calendar);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(calendarFragment.getActivity());
            builder.setTitle(R.string.pick_a_date_for_this_item);
            builder.setView(calendarPopupLayout);

            builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showMoveDialog();
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

                    String dateAsString;

                    if (selectedDateContainer[0] != null) {
                        dateAsString = selectedDateContainer[0];
                    } else {
                        dateAsString = defaultDate;
                    }

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).child(movingItem.getKey()).removeValue();
                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(dateAsString).child(movingItem.getKey()).setValue(itemValue);

                    databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(movingItem.getKey()).child("listName").setValue("calendar " + dateAsString);

                    Date date = new Date();

                    try {
                        date = new SimpleDateFormat("ddMMyyyy").parse(dateAsString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    calendarFragment.addEventToCalendarDay(date);

                    Date oldDate = new Date();

                    try {
                        oldDate = new SimpleDateFormat("ddMMyyyy").parse(selectedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    calendarFragment.removeEventFromCalendarDay(oldDate);

                    items.remove(movingItem);

                    calendarFragment.notifyAdapter();

                    if (items.size() == 0) {
                        calendarFragment.getEmptyCalendarItemsText().setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(calendarFragment.getActivity(), "Item moved to Calendar", Toast.LENGTH_SHORT).show();
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();
        }

        private void moveItemTo(String list) {
            String editedInTrayItemValue = databaseReference.push().getKey();

            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child(list).child(movingItem.getKey()).setValue(editedInTrayItemValue);
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("calendar").child(selectedDate).child(movingItem.getKey()).removeValue();

            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("items").child(movingItem.getKey()).child("listName").setValue(list);

            items.remove(movingItem);

            calendarFragment.notifyAdapter();

            if (items.size() == 0) {
                calendarFragment.getEmptyCalendarItemsText().setVisibility(View.VISIBLE);
            }

            switch (list) {
                case "waitingfor":
                    Toast.makeText(calendarFragment.getActivity(), "Item moved to " + calendarFragment.getActivity().getResources().getString(R.string.waiting_for), Toast.LENGTH_SHORT).show();
                    break;
                case "maybelater":
                    Toast.makeText(calendarFragment.getActivity(), "Item moved to " + calendarFragment.getActivity().getResources().getString(R.string.maybe_later), Toast.LENGTH_SHORT).show();
                    break;
                case "reference":
                    Toast.makeText(calendarFragment.getActivity(), "Item moved to " + calendarFragment.getActivity().getResources().getString(R.string.reference), Toast.LENGTH_SHORT).show();
                    break;
                case "trash":
                    Toast.makeText(calendarFragment.getActivity(), "Item moved to " + calendarFragment.getActivity().getResources().getString(R.string.trash), Toast.LENGTH_SHORT).show();
                    break;
            }


        }
    }
}
