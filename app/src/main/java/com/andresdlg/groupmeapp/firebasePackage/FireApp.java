package com.andresdlg.groupmeapp.firebasePackage;

import android.app.Application;

import com.alamkanak.weekview.WeekViewEvent;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.Entities.WeekViewEventGroupMeApp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 02/05/17.
 */

public class FireApp extends Application {

    private String groupKey;
    private List<WeekViewEvent> events;
    private List<WeekViewEventGroupMeApp> eventsGroupMeApp;
    private String groupName;
    private String downloadUrl;
    private Map<String, String> members;

    //PARA NOTIFICACIONES
    private Map<String, Integer> groupsIds;
    private Map<String, Integer> contactsIds;

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    private List<Users> groupUsers;

    public List<Users> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(List<Users> groupUsers) {
        this.groupUsers = groupUsers;
    }

    public void setEvents(List<WeekViewEvent> events) {
        this.events = events;
    }

    public void setEventsGroupMeApp(List<WeekViewEventGroupMeApp> events) {
        this.eventsGroupMeApp = events;
    }

    public List<WeekViewEvent> getEvents() {
        return events;
    }

    public List<WeekViewEventGroupMeApp> getEventsGroupMeApp() {
        return eventsGroupMeApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(this).isEmpty()) {
            //Permite la persistencia offline
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        groupsIds = new HashMap<>();
        contactsIds = new HashMap<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupPhoto(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    public Map<String, Integer> getGroupsIds() {
        return groupsIds;
    }

    public Map<String, Integer> getContactsIds() {
        return contactsIds;
    }
}
