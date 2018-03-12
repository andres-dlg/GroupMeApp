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

import com.andresdlg.groupmeapp.Adapters.RVSubGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.SubGroup;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
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

public class SubGroupsFragment extends Fragment {

    FloatingActionButton fab;
    String groupKey;
    RecyclerView rvSubGroups;
    RVSubGroupAdapter rvSubGroupsAdapter;
    List<SubGroup> subGroups;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sub_groups,container,false);
    }

    private void fillSubGroups(final View v) {
        DatabaseReference subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups");
        subGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                subGroups.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    SubGroup sgf = data.getValue(SubGroup.class);
                    subGroups.add(sgf);
                }
                v.findViewById(R.id.tvNoGroups).setVisibility(View.GONE);
                rvSubGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        fab = view.findViewById(R.id.fabSubGroups);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHeaderDialogFragment();
            }
        });

        groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        subGroups = new ArrayList<>();

        rvSubGroups = view.findViewById(R.id.rvSubGroups);
        rvSubGroups.setHasFixedSize(true); //El tamaño queda fijo, mejora el desempeño
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvSubGroups.setLayoutManager(llm);
        rvSubGroupsAdapter = new RVSubGroupAdapter(subGroups, getContext());
        rvSubGroups.setAdapter(rvSubGroupsAdapter);

        fillSubGroups(view);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        HeaderDialogFragment newFragment = new HeaderDialogFragment(GroupType.SUBGROUP,groupKey);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

}
