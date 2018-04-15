package com.andresdlg.groupmeapp.firebasePackage;

import android.app.Application;

import com.alamkanak.weekview.WeekViewEvent;
import com.andresdlg.groupmeapp.Entities.Users;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class FireApp extends Application {

    private String groupKey;
    private List<WeekViewEvent> events;

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

    public List<WeekViewEvent> getEvents() {
        return events;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(this).isEmpty()) {
            //Permite la persistencia offline
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        //PICASSO OFFLINE
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true); // red: image comes from network, blue: disk, green: memory
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }


}
