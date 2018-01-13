package com.andresdlg.groupmeapp.Entities;

import java.util.Date;

/**
 * Created by andresdlg on 13/01/18.
 */

public class Notification {

    private String userFromID;
    private String message;
    private int type;
    private Date date;

    private Notification(String userFromID, String message, int type, Date date){

        this.userFromID = userFromID;
        this.message = message;
        this.type = type;
        this.date = date;
    }

    private Notification(){

    }

    public String getUserFromID() {
        return userFromID;
    }

    public void setUserFromID(String userFromID) {
        this.userFromID = userFromID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
