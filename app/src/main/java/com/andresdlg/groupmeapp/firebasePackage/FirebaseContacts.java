package com.andresdlg.groupmeapp.firebasePackage;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FirebaseContacts {

    public static List<Users> users = new ArrayList<>();
    private static OnUserContactsListener adapterListener;
    private static boolean isAnyObjectListening = false;

    public static void setListener(Object listener){
        isAnyObjectListening = true;
        try {
            adapterListener = (OnUserContactsListener) listener;
        }
        catch (ClassCastException e){
            throw new ClassCastException(e.getMessage());
        }
    }

    public static void getUserContacts(String userId){

        Query query = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("friends");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                boolean noMoreUsers = true;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    if(postSnapshot.child("status").getValue().equals(FriendshipStatus.ACCEPTED.toString())){
                        noMoreUsers = false;
                        getUser(postSnapshot.getKey());
                    }
                }
                if(isAnyObjectListening && noMoreUsers ){
                    adapterListener.onUserContactsChange(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static void getUser(String key) {

        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users").child(key);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if(!users.contains(u)){
                    users.add(u);
                    Collections.sort(users, new Comparator<Users>() {
                        @Override
                        public int compare(Users o1, Users o2) {
                            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                        }
                    });
                    if(isAnyObjectListening) {
                        adapterListener.onUserContactsChange(users);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface OnUserContactsListener{
        void onUserContactsChange(List<Users> users);
    }
}
