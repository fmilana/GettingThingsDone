package com.gettingthingsdone.federico.gettingthingsdone;

import java.util.HashMap;

/**
 * Created by feder on 09-Mar-18.
 */

public class Project {

    private String key;
    private String title;
    private String description;

    private HashMap<String, String> projectItems;

    public Project() {}

    public Project(String title, String description, HashMap<String, String> projectItems) {
        this.title = title;
        this.description = description;
        this.projectItems = projectItems;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setProjectItems(HashMap<String, String> projectItems) {
        this.projectItems = projectItems;
    }

    public HashMap<String, String> getProjectItems() {
        return projectItems;
    }
}
