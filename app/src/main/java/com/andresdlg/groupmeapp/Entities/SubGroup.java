package com.andresdlg.groupmeapp.Entities;


import java.io.Serializable;
import java.util.List;

/**
 * Created by andresdlg on 17/02/18.
 */

public class SubGroup implements Serializable{

    private String name;
    private String imageUrl;
    private String subGroupKey;
    private List<String> members;
    private List<Task> tasks;

    public SubGroup(String name, String imageUrl, String subGroupKey, List<String> members, List<Task> tasks) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.subGroupKey = subGroupKey;
        this.members = members;
        this.tasks = tasks;
    }

    public SubGroup(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSubGroupKey() {
        return subGroupKey;
    }

    public void setSubGroupKey(String subGroupKey) {
        this.subGroupKey = subGroupKey;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
