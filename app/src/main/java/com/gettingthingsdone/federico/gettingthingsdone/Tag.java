package com.gettingthingsdone.federico.gettingthingsdone;

import com.google.android.gms.location.places.Place;

/**
 * Created by Federico on 09-Nov-17.
 */

public class Tag {

    private String key;

    private String text;
    private String time;
    private String daysOfTheWeek;
    private String locationAddress;
    private String locationKey;

    public Tag() {
        daysOfTheWeek = "0000000";
    }

    public Tag(String text) {
        this.text = text;
        daysOfTheWeek = "0000000";
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

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setDaysOfTheWeek(String daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public String getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getLocationKey() {
        return locationKey;
    }
}
