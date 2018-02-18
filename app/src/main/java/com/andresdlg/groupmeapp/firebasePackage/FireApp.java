package com.andresdlg.groupmeapp.firebasePackage;

import android.app.Application;

import com.andresdlg.groupmeapp.Entities.Users;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class FireApp extends Application {

    private String groupKey;

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

    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(this).isEmpty()) {
            //Permite la persistencia offline
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }
}
