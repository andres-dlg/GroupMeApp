package com.andresdlg.groupmeapp.Entities;

import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 11/07/17.
 */

public class Group {
    private int photoId;
    private String name;
    private String objetive;
    private List<String> userIds;

    public Group(int photoId, String name, String objetive, List<String> userIds){
        this.photoId = photoId;
        this.name = name;
        this.objetive = objetive;
        this.userIds = userIds;
    }

    public Group(){

    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
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

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}


