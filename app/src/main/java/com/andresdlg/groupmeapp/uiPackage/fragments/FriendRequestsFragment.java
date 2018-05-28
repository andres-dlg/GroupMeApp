package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVContactRequestAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class FriendRequestsFragment extends Fragment {

    RVContactRequestAdapter adapter;
    List<Users> users = new ArrayList<>();
    DatabaseReference firebaseContacts;
    TextView tvRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_requests,container,false);

        RecyclerView rv = v.findViewById(R.id.rvContactRequestsList);
        rv.setHasFixedSize(true);

        tvRequests = v.findViewById(R.id.tvRequests);

        RelativeLayout.LayoutParams parameter = (RelativeLayout.LayoutParams) rv.getLayoutParams();
        int config = getResources().getConfiguration().orientation;

        int dpValue1 = 55;
        int dpValue2 = 48;
        float d = getResources().getDisplayMetrics().density;
        int margin1 = (int)(dpValue1*d);
        int margin2 = (int)(dpValue2*d);

        if(config == Configuration.ORIENTATION_LANDSCAPE){
            parameter.setMargins(parameter.leftMargin,margin2,parameter.rightMargin,parameter.bottomMargin);
        }else if(config == Configuration.ORIENTATION_PORTRAIT){
            parameter.setMargins(parameter.leftMargin,margin1,parameter.rightMargin,parameter.bottomMargin);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        adapter = new RVContactRequestAdapter(users, getContext());
        rv.setAdapter(adapter);


        firebaseContacts = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
        firebaseContacts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                adapter.notifyDataSetChanged();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    if(postSnapshot.child("status").getValue().equals(FriendshipStatus.PENDING.toString())){
                        getUser(postSnapshot.getKey());
                        tvRequests.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return v;
    }

    private void getUser(String key) {

        DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users").child(key);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if(!users.contains(u)){
                    users.add(u);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser){
                for(Users u : users){
                    firebaseContacts.child(u.getUserid()).child("seen").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue().toString().equals(NotificationStatus.UNREAD.toString())){
                                dataSnapshot.getRef().setValue(NotificationStatus.READ);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //firebaseContacts.child(u.getUserid()).child("seen").setValue(NotificationStatus.READ);
                }
            }
        }
    }
}
