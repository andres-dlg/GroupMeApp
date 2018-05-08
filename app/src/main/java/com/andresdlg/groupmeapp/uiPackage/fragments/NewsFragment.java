package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.Adapters.RVNewsAdapter;
import com.andresdlg.groupmeapp.Entities.Post;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class NewsFragment extends Fragment {

    DatabaseReference postsRef;
    DatabaseReference groupsRef;

    FloatingActionButton fab;
    ProgressBar progressBar;
    TextView tvNoNews;

    List<Post> posts;
    RecyclerView rvPosts;
    RVNewsAdapter rvNewsAdapter;
    LinearLayoutManager llm;
    SwipeRefreshLayout swipeContainer;

    List<String> groupKeys;

    List<String> groupNames;

    int cantidadDeGrupos;

    int cantidadDePosts;

    Integer selectedItems[] = {};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = new ArrayList<>();
        groupKeys = new ArrayList<>();
        groupNames = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news,container,false);

        progressBar = view.findViewById(R.id.progressBar);

        tvNoNews = view.findViewById(R.id.tvNoNews);

        fab = view.findViewById(R.id.fabFilter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getContext())
                        .title("Elije un grupo")
                        .items(groupNames)
                        .itemsCallbackMultiChoice(selectedItems, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                List<String> groupKeysFiltered = new ArrayList<>();
                                for(int i : which){
                                    groupKeysFiltered.add(groupKeys.get(i));
                                }
                                posts.clear();
                                rvNewsAdapter.notifyDataSetChanged();
                                if(which.length != 0){
                                    fetchPosts(groupKeysFiltered);
                                }else{
                                    fetchPosts(groupKeys);
                                }
                                selectedItems = which;
                                return true;
                            }
                        })
                        .positiveText("Elegir")
                        .show();
            }
        });

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                posts.clear();
                fetchPosts(groupKeys);
                selectedItems = new Integer[]{};
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        rvNewsAdapter = new RVNewsAdapter(getContext(),posts,false,null);

        //rvPosts.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(llm);
        rvPosts.setAdapter(rvNewsAdapter);

        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        posts.clear();
        rvNewsAdapter.notifyDataSetChanged();
        getUserGroups();
    }

    private void getUserGroups() {
        DatabaseReference userGroupsRef = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(StaticFirebaseSettings.currentUserId)
                .child("groups");

        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    groupNames.clear();
                    groupKeys.clear();
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.child("status").getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                            groupKeys.add(data.getKey());
                            fetchGroupName(data.getKey());
                        }
                    }
                    fetchPosts(groupKeys);
                }else{
                    rvPosts.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeContainer.setRefreshing(false);

                    if(!dataSnapshot.hasChildren()){
                        tvNoNews.setVisibility(View.VISIBLE);
                    }else {
                        tvNoNews.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchGroupName(String groupKey) {
        groupsRef.child(groupKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = false;
                String name = dataSnapshot.getValue().toString();
                for(String s : groupNames){
                    if(s.equals(name)){
                        exists = true;
                    }
                }
                if(!exists){
                    groupNames.add(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchPosts(List<String> groupKeys) {
        cantidadDeGrupos = groupKeys.size();
        for(int i = 0; i< cantidadDeGrupos ; i++){
            postsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKeys.get(i)).child("posts");
            final int finalI1 = i;
            postsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post post = dataSnapshot.getValue(Post.class);
                    updatePosts(post);
                    Collections.sort(posts, new Comparator<Post>() {
                        @Override
                        public int compare(Post post, Post t1) {
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTimeInMillis(post.getTime());

                            Calendar calendar2 = Calendar.getInstance();
                            calendar2.setTimeInMillis(t1.getTime());

                            return calendar2.compareTo(calendar1);
                        }
                    });
                    if(finalI1 == cantidadDeGrupos-1){
                        rvNewsAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    rvPosts.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeContainer.setRefreshing(false);
                }
            });

            final int finalI = i;
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    rvPosts.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeContainer.setRefreshing(false);

                    if(finalI == cantidadDeGrupos-1){
                        hideOrNotTextViewNoNews();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private void hideOrNotTextViewNoNews() {
        if(posts.size()==0){
            tvNoNews.setVisibility(View.VISIBLE);
        }else {
            tvNoNews.setVisibility(View.GONE);
        }
    }

    private void updatePosts(Post post) {
        boolean exists = false;
        for(int i=0; i < posts.size(); i++){
            if(posts.get(i).getPostId().equals(post.getPostId())){
                exists = true;
                posts.remove(i);
                posts.add(i,post);
            }
        }
        if(!exists){
            posts.add(0,post);
        }
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
}
