package com.gettingthingsdone.federico.gettingthingsdone;

/**
 * Created by Federico on 08-Feb-18.
 */

public class User {

    private String email;

    private boolean inTrayRemindersEnabled;
    private boolean calendarNotificationsEnabled;

    private String inTrayRemindersTime;
    private String calendarNotificationsTime;


    public User(String email) {
        this.email = email;
        inTrayRemindersEnabled = true;
        calendarNotificationsEnabled = true;
        inTrayRemindersTime = "7 12:00";
        calendarNotificationsTime = "08:00";
    }

    public String getEmail() {
        return email;
    }

    public boolean getInTrayRemindersEnabled() {
        return inTrayRemindersEnabled;
    }

    public boolean getCalendarNotificationsEnabled() {
        return calendarNotificationsEnabled;
    }

    public String getInTrayRemindersTime() {
        return inTrayRemindersTime;
    }

    public String getCalendarNotificationsTime() {
        return calendarNotificationsTime;
    }
}
