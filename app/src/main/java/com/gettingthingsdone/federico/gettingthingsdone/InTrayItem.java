package com.gettingthingsdone.federico.gettingthingsdone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 07-Nov-17.
 */

public class InTrayItem {

    private String key;
    private String text;

    public InTrayItem() {}

    public InTrayItem(String text) {
        this.text = text;
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
}
