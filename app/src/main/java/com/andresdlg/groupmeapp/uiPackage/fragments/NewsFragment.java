package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVNewsAdapter;
import com.andresdlg.groupmeapp.Entities.Post;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.NewPostActivity;
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
import java.util.Date;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class NewsFragment extends Fragment {

    DatabaseReference postsRef;

    FloatingActionButton fab;
    ProgressBar progressBar;
    TextView tvNoNews;

    List<Post> posts;
    RecyclerView rvPosts;
    RVNewsAdapter rvNewsAdapter;
    LinearLayoutManager llm;
    SwipeRefreshLayout swipeContainer;

    List<String> groupKeys;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Toast.makeText(getContext(), "Filter!", Toast.LENGTH_SHORT).show();
            }
        });

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                posts.clear();
                fetchPosts();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        posts = new ArrayList<>();
        groupKeys = new ArrayList<>();

        rvNewsAdapter = new RVNewsAdapter(getContext(),posts,false,null);

        //rvPosts.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(llm);
        rvPosts.setAdapter(rvNewsAdapter);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        posts.clear();
        getUserGroups();
    }

    private void getUserGroups() {
        DatabaseReference userGroupsRef = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(StaticFirebaseSettings.currentUserId)
                .child("groups");

        userGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        groupKeys.add(data.getKey());
                    }
                    fetchPosts();
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

    private void fetchPosts() {
        for(String groupKey : groupKeys){
            postsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("posts");

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
                    rvNewsAdapter.notifyDataSetChanged();
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

            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    rvPosts.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeContainer.setRefreshing(false);

                    if(!dataSnapshot.hasChildren()){
                        tvNoNews.setVisibility(View.VISIBLE);
                    }else {
                        tvNoNews.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
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
