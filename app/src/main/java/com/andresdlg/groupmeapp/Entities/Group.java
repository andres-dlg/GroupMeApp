package com.andresdlg.groupmeapp.Entities;

import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 11/07/17.
 */

public class Group {
    private String groupKey;
    private String imageUrl;
    private String name;
    private String objetive;
    private Map<String,String> members;

    public Group(String groupKey, String imageUrl, String name, String objetive, Map<String,String> members){
        this.groupKey = groupKey;
        this.imageUrl = imageUrl;
        this.name = name;
        this.objetive = objetive;
        this.members = members;
    }

    public Group(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjetive() {
        return objetive;
    }

    public void setObjetive(String objetive) {
        this.objetive = objetive;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String,String> getMembers() {
        return members;
    }

    public void setMembers(Map<String,String> members) {
        this.members = members;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}


