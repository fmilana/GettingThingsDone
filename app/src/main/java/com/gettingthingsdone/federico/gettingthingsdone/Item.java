package com.gettingthingsdone.federico.gettingthingsdone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class Item {

    private String key;
    private String text;

    private HashMap<String, String> itemTags;

    private boolean notificationsEnabled;

    private String listName;

    public Item() {
        this.notificationsEnabled = false;
    }

    public Item(String text, HashMap<String, String> itemTags, boolean notificationsEnabled) {
        this.text = text;
        this.itemTags = itemTags;
        this.notificationsEnabled = notificationsEnabled;
        this.listName = "intray";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setItemTags(HashMap<String, String> tagKeys) {
        this.itemTags = tagKeys;
    }

    public HashMap<String, String> getItemTags() {
        return itemTags;
    }

    public boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean enabled) {
        notificationsEnabled = enabled;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

}
