package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVSubGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.SubGroup;

import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 02/05/17.
 */

public class SubGroupsFragment extends Fragment {

    SwipeRefreshLayout swipeContainer;

    FloatingActionButton fab;
    String groupKey;
    RecyclerView rvSubGroups;
    RVSubGroupAdapter rvSubGroupsAdapter;
    List<SubGroup> subGroups;
    LinearLayoutManager llm;

    boolean onFocus;

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
                rvSubGroupsAdapter.clear();
                subGroups.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    SubGroup sgf = new SubGroup();
                    sgf.setName(data.child("name").getValue().toString());
                    sgf.setImageUrl(data.child("imageUrl").getValue().toString());
                    sgf.setMembers((Map<String,String>) data.child("members").getValue());
                    sgf.setSubGroupKey(data.child("subGroupKey").getValue().toString());
                    List<Task> tasks = new ArrayList();
                    for(DataSnapshot d : data.child("tasks").getChildren()){
                        Task task = d.getValue(Task.class);
                        tasks.add(task);
                    }
                    sgf.setTasks(tasks);
                    //SubGroup sgf = data.getValue(SubGroup.class);
                    subGroups.add(sgf);
                }
                v.findViewById(R.id.tvNoGroups).setVisibility(View.GONE);
                //rvSubGroupsAdapter.addAll(subGroups);
                swipeContainer.setRefreshing(false);
                //rvSubGroupsAdapter.notify(subGroups);
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


        groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        fab = view.findViewById(R.id.fabSubGroups);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHeaderDialogFragment();
            }
        });


        subGroups = new ArrayList<>();

        rvSubGroups = view.findViewById(R.id.rvSubGroups);
        rvSubGroups.setHasFixedSize(false); //El tamaño queda fijo, mejora el desempeño
        llm = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        rvSubGroups.setLayoutManager(llm);
        rvSubGroupsAdapter = new RVSubGroupAdapter(subGroups,groupKey,getContext());
        rvSubGroups.setAdapter(rvSubGroupsAdapter);
        rvSubGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(llm.findLastCompletelyVisibleItemPosition() != subGroups.size()-1){
                        fab.show();
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0 || dx < 0 && fab.isShown()){
                    fab.hide();
                }
            }
        });




        fillSubGroups(view);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0);
                fillSubGroups(view);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setRefreshing(true);


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser){
                fab.show();
            }else{
                fab.hide();
            }
        }
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
