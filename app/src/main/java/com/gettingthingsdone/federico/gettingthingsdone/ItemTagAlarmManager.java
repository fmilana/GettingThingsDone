package com.gettingthingsdone.federico.gettingthingsdone;

import android.app.AlarmManager;
import android.app.PendingIntent;

public class ItemTagAlarmManager {

    private String itemKey;
    private String tagKey;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public ItemTagAlarmManager(String itemKey, String tagKey, AlarmManager alarmManager, PendingIntent pendingIntent) {
        this.itemKey = itemKey;
        this.tagKey = tagKey;
        this.alarmManager = alarmManager;
        this.pendingIntent = pendingIntent;
    }

    public String getItemKey() {
        return itemKey;
    }

    public String getTagKey() {
        return tagKey;
    }

    public AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }
}
