package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class GroupsFragment extends Fragment implements View.OnClickListener {

    TextView tvNoGroups;
    RVGroupAdapter adapter;
    DatabaseReference mUserGroupsRef;
    DatabaseReference groupsRef;
    List<Group> groups;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups,container,false);

        setRetainInstance(true);

        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groups = new ArrayList<>();

        //Recicler view
        RecyclerView rv = view.findViewById(R.id.rvGroups);
        rv.setHasFixedSize(true); //El tamaño queda fijo, mejora el desempeño
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        //Floating action button
        FloatingActionButton mFloatingActionButton = view.findViewById(R.id.fabGroups);
        mFloatingActionButton.setOnClickListener(this);

        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

        mUserGroupsRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("groups");
        mUserGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    getGroup(data.getKey());
                    view.findViewById(R.id.tvNoGroups).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new RVGroupAdapter(getContext(),groups);
        rv.setAdapter(adapter);

        tvNoGroups = view.findViewById(R.id.tvNoGroups);
        checkGroupsQuantity();
    }

    private void getGroup(String key) {
        DatabaseReference groupRef = groupsRef.child(key);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean contains = false;
                Group u = dataSnapshot.getValue(Group.class);
                for(Group g : groups){
                    if(g.getGroupKey().equals(u.getGroupKey())){
                        contains = true;
                    }
                }
                if(!contains){
                    groups.add(u);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkGroupsQuantity() {
        if(adapter.getItemCount() == 0){
            tvNoGroups.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabGroups:
                showHeaderDialogFragment();
                break;
        }
    }

    private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeaderDialogFragment newFragment = new HeaderDialogFragment();
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        }
    }
