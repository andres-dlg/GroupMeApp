package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class GroupNewsFragment extends Fragment {

    DatabaseReference postsRef;

    FloatingActionButton fab;
    ProgressBar progressBar;
    TextView tvNoNews;

    List<Post> posts;
    RecyclerView rvPosts;
    RVNewsAdapter rvNewsAdapter;
    LinearLayoutManager llm;

    List<String> postRevised;
    int postQuantity ;

    OnNewPostSetListener mOnNewPostSetListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getActivity());

        postRevised = new ArrayList<>();

        postQuantity = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_group,container,false);

        Bundle bundle = getArguments();
        String groupKey = bundle.getString("groupKey");

        progressBar = view.findViewById(R.id.progressBar);

        tvNoNews = view.findViewById(R.id.tvNoNews);

        fab = view.findViewById(R.id.fabNewPost);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), NewPostActivity.class);
                getContext().startActivity(i);
            }
        });

        //String groupKey = ((FireApp) getContext().getApplicationContext()).getGroupKey();

        postsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("posts");
        postsRef.keepSynced(true);

        posts = new ArrayList<>();

        rvNewsAdapter = new RVNewsAdapter(getContext(),posts, true,groupKey);

        //rvPosts.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(llm);
        rvPosts.setAdapter(rvNewsAdapter);
        rvPosts.setItemViewCacheSize(100);
        rvPosts.setDrawingCacheEnabled(true);
        rvPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)rvPosts.getLayoutParams();

        if(bundle.getBoolean("fromNotificationSubGroupInvitation")){
            int newMarginDp = 8;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        if(bundle.getBoolean("fromNotificationNewPost")){
            int newMarginDp = 32;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        if(!bundle.getBoolean("fromNotificationSubGroupInvitation") && !bundle.getBoolean("fromNotificationNewPost")){
            int newMarginDp = 32;
            params.topMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));
        }

        fetchPosts();

        return view;
    }

    private void fetchPosts() {

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                updatePosts(post);

                if(post.getSeenBy()!=null){
                    boolean wasRevised = false;
                    for(String p : postRevised){
                        if(p.equals(post.getPostId())){
                            wasRevised = true;
                            break;
                        }
                    }
                    if(!wasRevised){
                        boolean iHaveSeenPost = false;
                        for(String id : post.getSeenBy()){
                            if(id.equals(StaticFirebaseSettings.currentUserId)){
                                iHaveSeenPost = true;
                                break;
                            }
                        }
                        if(!iHaveSeenPost){
                            postQuantity += 1;
                        }
                        postRevised.add(post.getPostId());
                    }
                }else{
                    postRevised.add(post.getPostId());
                    postQuantity += 1;
                }

                mOnNewPostSetListener.onNewPostSet(postQuantity);

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
            }
        });

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rvPosts.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                if(!dataSnapshot.hasChildren()){
                    tvNoNews.setVisibility(View.VISIBLE);
                }else{
                    tvNoNews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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

                for(Post p : posts){
                    List<String> seenBy = p.getSeenBy();
                    boolean existo = false;
                    for(String u : seenBy){
                        if(u.equals(StaticFirebaseSettings.currentUserId)){
                            existo = true;
                        }
                    }
                    if(!existo){
                        seenBy.add(StaticFirebaseSettings.currentUserId);
                        FirebaseDatabase.getInstance().getReference("Groups").child(p.getGroupKey()).child("posts").child(p.getPostId()).child("seenBy").setValue(seenBy);
                    }
                }
                rvNewsAdapter.setPostsAsSeen();
                mOnNewPostSetListener.onNewPostSet(0);

                postQuantity = 0;

                fab.hide();
            }
        }
    }

    public interface OnNewPostSetListener{
        void onNewPostSet(int postQuantity);
    }

    public void onAttachToParentFragment(FragmentActivity activity){
        try {
            mOnNewPostSetListener = (OnNewPostSetListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnUserSelectionSetListener");
        }
    }
}
