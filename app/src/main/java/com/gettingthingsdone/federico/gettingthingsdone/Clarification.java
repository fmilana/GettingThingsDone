package com.gettingthingsdone.federico.gettingthingsdone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gettingthingsdone.federico.gettingthingsdone.activities.MainActivity;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.InTrayFragment;
import com.gettingthingsdone.federico.gettingthingsdone.fragments.ProjectsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by feder on 11-Mar-18.
 */

public class Clarification {

    private InTrayFragment inTrayFragment;

    private ArrayList<Project> projects;

    private int moveIntoListPosition;
    private int moveIntoProjectPosition;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public Clarification(InTrayFragment inTrayFragment) {
        this.inTrayFragment = inTrayFragment;

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

    public void showClarifyDialog(final int adapterPosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setTitle(inTrayFragment.getResources().getString(R.string.clarify_item));
        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showSkipDialog(adapterPosition);
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
                isThisItemActionablePopup(adapterPosition);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }



    private void showSkipDialog(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.move_item_to));

//            String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

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
                showClarifyDialog(adapterPosition);
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
                    thisItemBelongsInProjects(adapterPosition, "showSkipDialog");
                } else if (moveIntoListPosition == 2) {
                    moveItemTo(inTrayFragment.getItems().get(adapterPosition), "waitingfor");
                } else if (moveIntoListPosition == 3) {
                    moveItemTo(inTrayFragment.getItems().get(adapterPosition), "maybelater");
                } else if (moveIntoListPosition == 4) {
                    moveItemTo(inTrayFragment.getItems().get(adapterPosition), "reference");
                } else if (moveIntoListPosition == 5) {
                    moveItemTo(inTrayFragment.getItems().get(adapterPosition), "trash");
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void isThisItemActionablePopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_actionable));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showClarifyDialog(adapterPosition);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisTakeLessThanTwoMinutes(adapterPosition);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void isThisItemUsefulToRememberPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_useful_to_remember));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemActionablePopup(adapterPosition);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInTrashPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInReferencePopup(adapterPosition);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doesThisTakeLessThanTwoMinutes(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.does_this_item_take_more_than_two_minutes));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemActionablePopup(adapterPosition);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doItNowPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject(adapterPosition, "doesThisTakeLessThanTwoMinutes");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doItNowPopup(final int adapterPosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.do_it_now));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisTakeLessThanTwoMinutes(adapterPosition);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject(adapterPosition, "doItNowPopup");
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Item itemToRemove = inTrayFragment.getItems().get(adapterPosition);

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

    private void isThisItemPartOfAProject(final int adapterPosition, final String lastPopup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_this_item_part_of_a_project));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (lastPopup.equals("doesThisTakeLessThanTwoMinutes")) {
                    doesThisTakeLessThanTwoMinutes(adapterPosition);
                } else if (lastPopup.equals("doItNowPopup")) {
                    doItNowPopup(adapterPosition);
                }
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThereADateForThisItemPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInProjects(adapterPosition, "isThisItemPartOfAProject");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void isThereADateForThisItemPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.is_there_a_date_for_this_Item));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemPartOfAProject(adapterPosition, "doesThisTakeLessThanTwoMinutes");
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void doesThisItemNeedToWaitForPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.does_this_item_need_to_wait_for_something_or_someone));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThereADateForThisItemPopup(adapterPosition);
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInMaybeLaterPopup(adapterPosition);
            }
        });

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                thisItemBelongsInWaitForPopup(adapterPosition);
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInProjects(final int adapterPosition, final String lastPopup) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.move_item_to_project));

        System.out.println("PROJECTS.SIZE() = " + projects.size());

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
                    isThisItemPartOfAProject(adapterPosition, "doesThisTakeLessThanTwoMinutes");
                } else if (lastPopup.equals("showSkipDialog")) {
                    showSkipDialog(adapterPosition);
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
                    Item item = inTrayFragment.getItems().get(adapterPosition);
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
                } else {

                }

                Toast.makeText(inTrayFragment.getActivity(), "Item moved to Projects", Toast.LENGTH_SHORT).show();

            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void thisItemBelongsInMaybeLaterPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_maybe_later));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup(adapterPosition);
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
                moveItemTo(inTrayFragment.getItems().get(adapterPosition), "maybelater");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void thisItemBelongsInWaitForPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_waiting_for));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doesThisItemNeedToWaitForPopup(adapterPosition);
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
                moveItemTo(inTrayFragment.getItems().get(adapterPosition), "waitingfor");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInTrashPopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_trash));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup(adapterPosition);
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
                moveItemTo(inTrayFragment.getItems().get(adapterPosition), "trash");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void thisItemBelongsInReferencePopup(final int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(inTrayFragment.getActivity());

        builder.setTitle(inTrayFragment.getResources().getString(R.string.this_item_belongs_in_reference));

        String itemText = inTrayFragment.getItems().get(adapterPosition).getText();

        builder.setMessage(itemText);

        builder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isThisItemUsefulToRememberPopup(adapterPosition);
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
                moveItemTo(inTrayFragment.getItems().get(adapterPosition), "reference");
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void moveItemTo(Item item, String list) {

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
