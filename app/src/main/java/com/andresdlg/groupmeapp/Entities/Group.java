package com.andresdlg.groupmeapp.Entities;

import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 11/07/17.
 */

public class Group {
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

    private int photoId;
    private String name;

    public Group(int photoId, String name){
        this.photoId = photoId;
        this.name = name;
    }
}


