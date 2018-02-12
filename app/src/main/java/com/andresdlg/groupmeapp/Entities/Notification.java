package com.andresdlg.groupmeapp.Entities;

import java.util.Date;

/**
 * Created by andresdlg on 13/01/18.
 */

public class Notification {

    private String notificationKey;
    private Date date;
    private String from;
    private String message;
    private String state;
    private String title;
    private String type;

    private Notification(String notificationKey, Date date, String from, String message, String state, String title, String type ){
        this.notificationKey = notificationKey;
        this.date = date;
        this.from = from;
        this.message = message;
        this.state = state;
        this.title = title;
        this.type = type;
    }

    private Notification(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }
}
