package com.gettingthingsdone.federico.gettingthingsdone;

import com.google.android.gms.location.places.Place;

/**
 * Created by Federico on 09-Nov-17.
 */

public class Tag {

    private String key;

    private String text;
    private String time;
//    private double latitude;
//    private double longitude;
    private String locationAddress;
    private String locationKey;


    public Tag() {}

    public Tag(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
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

//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }

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
