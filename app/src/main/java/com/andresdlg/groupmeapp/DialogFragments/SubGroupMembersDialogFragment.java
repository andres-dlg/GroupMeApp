package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.andresdlg.groupmeapp.Adapters.RVSubGroupDetailAdapter;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupMembersDialogFragment extends DialogFragment {

    RVSubGroupDetailAdapter adapter;
    List<Users> users = new ArrayList<>();
    Map<String, String> usersRoles = new HashMap<>();
    Map<String, String> members;
    DatabaseReference firebaseContacts;
    //TextView tvFriends;

    DatabaseReference usersRef;

    private String subGroupKey;
    private String groupKey;

    public SubGroupMembersDialogFragment(String subGroupName, String subGroupUrlPhoto, String subGroupKey, String groupKey) {
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subgroup_members, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar_chats);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        toolbar.setTitle("Miembros");

        final RecyclerView rv = v.findViewById(R.id.rvMembers);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        firebaseContacts = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);
        firebaseContacts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //users.clear();
                SubGroup sg = new SubGroup(dataSnapshot.child("name").getValue().toString(),null,null,null,null);
                sg.setName(dataSnapshot.child("name").getValue().toString());
                sg.setImageUrl(dataSnapshot.child("imageUrl").getValue().toString());
                sg.setMembers((Map<String,String>) dataSnapshot.child("members").getValue());
                sg.setSubGroupKey(dataSnapshot.child("subGroupKey").getValue().toString());
                getMembers(sg);

                adapter = new RVSubGroupDetailAdapter(users,usersRoles,groupKey,subGroupKey,getContext());
                if(rv.getAdapter() == null){
                    rv.setAdapter(adapter);
                }else{
                    rv.swapAdapter(adapter,false);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void getMembers(final SubGroup sg) {
        members = sg.getMembers();
        ValueEventListener listener;
        for(Map.Entry<String, String> entry : members.entrySet()) {
            String memberId = entry.getKey();
            //String memberRol = entry.getValue();
            usersRoles = sg.getMembers();
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(memberId);
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    filterUsers(u);
                    //users.add(u);
                    //adapter.notifyDataSetChanged();
                    usersRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            usersRef.addListenerForSingleValueEvent(listener);
        }
    }

    private void filterUsers(Users u) {
        boolean exists = false;
        for(int i = 0; i<users.size();i++){
            if(users.get(i).getUserid().equals(u.getUserid())){
                exists = true;
                users.set(i,u);
            }
        }
        if(!exists){
            users.add(u);
        }
        adapter.notifyDataSetChanged();
    }

}