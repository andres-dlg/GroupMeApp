package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVSubGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.SubGroup;

import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.google.firebase.database.ChildEventListener;
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
    ProgressBar progressBar;
    TextView tvNoSubGroups;

    RVSubGroupAdapter rvSubGroupsAdapter;
    List<SubGroup> subGroups;
    LinearLayoutManager llm;

    View vista;

    DatabaseReference subGroupsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        subGroups = new ArrayList<>();

        groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups");

        return inflater.inflate(R.layout.fragment_sub_groups,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        vista = view;

        progressBar = view.findViewById(R.id.progressBar);

        tvNoSubGroups = view.findViewById(R.id.tvNoSubGroups);

        fab = view.findViewById(R.id.fabSubGroups);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHeaderDialogFragment();
            }
        });

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

        fillSubGroups();

        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0);
                //fillSubGroups(view);
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //swipeContainer.setRefreshing(true);
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

    private void fillSubGroups() {
        subGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot data, String s) {
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
                subGroups.add(sgf);
                rvSubGroupsAdapter.setCantidadTasks(tasks.size());

                rvSubGroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot data, String s) {
                if(data.child("members").getValue()!=null){
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
                    int i = findPosition(sgf.getSubGroupKey());
                    if(i != -1){
                        subGroups.remove(i);
                        subGroups.add(i,sgf);

                    }

                    RVSubGroupAdapter.taskTypes type = rvSubGroupsAdapter.checkTasksSize(tasks.size());

                    if(type == RVSubGroupAdapter.taskTypes.NEW_TASK){
                        rvSubGroupsAdapter.setNewTaskFlag();
                    }else if(type == RVSubGroupAdapter.taskTypes.UPDATED_TASK){
                        rvSubGroupsAdapter.setUpdatedTaskFlag();
                    }else{
                        rvSubGroupsAdapter.setDeletedTaskFlag();
                    }

                    rvSubGroupsAdapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot data) {
                int i = findPosition(data.child("subGroupKey").getValue().toString());
                if(i != -1){
                    subGroups.remove(i);
                    rvSubGroupsAdapter.notifyItemRemoved(i);
                    rvSubGroupsAdapter.notifyItemRangeChanged(i,subGroups.size());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        subGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    tvNoSubGroups.setVisibility(View.GONE);
                    rvSubGroups.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }else {
                    tvNoSubGroups.setVisibility(View.VISIBLE);
                    rvSubGroups.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findPosition(String subGroupKey) {
        for(int i = 0; i < subGroups.size(); i++){
            if (subGroupKey.equals(subGroups.get(i).getSubGroupKey())){
                return i;
            }
        }
        return -1;
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
