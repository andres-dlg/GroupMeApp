package com.andresdlg.groupmeapp.databasePackage;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by andresdlg on 02/05/17.
 */

public class DatabaseBinder {

    /*SEGUIR CON ESTO*/
    FirebaseDatabase database;
    DatabaseReference myRef;

    public DatabaseBinder(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        myRef.setValue("Hello, World!");
    }

}
