package com.gettingthingsdone.federico.gettingthingsdone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayItem {

    private String key;
    private String text;

    private HashMap<String, String> itemTags;

    public InTrayItem() {}

    public InTrayItem(String text, HashMap<String, String> itemTags) {
        this.text = text;
        this.itemTags = itemTags;
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

}
