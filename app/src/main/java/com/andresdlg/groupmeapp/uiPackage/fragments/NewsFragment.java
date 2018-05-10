package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    FloatingActionButton fabFilter;
    FloatingActionButton fabClear;
    ProgressBar progressBar;
    TextView tvNoNews;

    List<Post> posts;
    RecyclerView rvPosts;
    RVNewsAdapter rvNewsAdapter;
    LinearLayoutManager llm;

    List<String> groupKeys;

    List<String> groupNames;

    int cantidadDeGrupos;

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

        fabClear = view.findViewById(R.id.fabClear);
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabClear.hide();
                selectedItems = new Integer[]{};
                filterPosts(groupKeys);
            }
        });

        fabFilter = view.findViewById(R.id.fabFilter);
        fabFilter.setOnClickListener(new View.OnClickListener() {
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
                                if(which.length != 0){
                                    fabClear.show();
                                    filterPosts(groupKeysFiltered);
                                }else{
                                    fabClear.hide();
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

        rvNewsAdapter = new RVNewsAdapter(getContext(),posts,false,null);

        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(llm);
        rvPosts.setAdapter(rvNewsAdapter);

        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

        getUserGroups();

        return view;
    }

    private void filterPosts(List<String> groupKeysFiltered) {
        cantidadDeGrupos = groupKeysFiltered.size();
        posts.clear();
        rvNewsAdapter.notifyDataSetChanged();
        for(int i = 0; i< cantidadDeGrupos ; i++){
            postsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKeysFiltered.get(i)).child("posts");
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        Post post = d.getValue(Post.class);
                        updatePosts(post,0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
                groupNames.clear();
                groupKeys.clear();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.child("status").getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                        groupKeys.add(data.getKey());
                        fetchGroupName(data.getKey());
                    }
                }
                progressBar.setVisibility(View.GONE);
                tvNoNews.setVisibility(View.VISIBLE);
                if(groupKeys.size()>0){
                    fetchPosts(groupKeys);
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

    private void fetchPosts(final List<String> groupKeys) {
        cantidadDeGrupos = groupKeys.size();
        for(int i = 0; i< cantidadDeGrupos ; i++){
            postsRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKeys.get(i)).child("posts");
            postsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post post = dataSnapshot.getValue(Post.class);
                    updatePosts(post,0);

                    //SI HAY ALGUN POST SE VA A MOSTRAR LA LISTA Y EL TEXTVIEW
                    rvPosts.setVisibility(View.VISIBLE);
                    tvNoNews.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    updatePosts(post,1);
                    if(posts.size() == 0){
                        rvPosts.setVisibility(View.GONE);
                        tvNoNews.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void updatePosts(Post post, int mode) {
        boolean exists = false;
        for(int i=0; i < posts.size(); i++){
            if(posts.get(i).getPostId().equals(post.getPostId())){
                exists = true;
                posts.remove(i);
                if(mode == 0){
                    posts.add(i,post);
                    rvNewsAdapter.notifyItemChanged(i);
                }else{
                    rvNewsAdapter.notifyItemRemoved(i);
                    rvNewsAdapter.notifyItemRangeChanged(i,posts.size());
                }
            }
        }
        if(!exists && mode == 0){
            //int position = posts.size();
            //posts.add(position,post);
            //rvNewsAdapter.notifyDataSetChanged();
            //rvNewsAdapter.notifyItemInserted(position);
            //rvNewsAdapter.notifyItemRangeChanged(position,posts.size());
            posts.add(post);
            rvNewsAdapter.notifyDataSetChanged();
        }
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
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser){
                fabFilter.show();
            }else{
                selectedItems = new Integer[]{};
                filterPosts(groupKeys);
                fabClear.hide();
                fabFilter.hide();
            }
        }
    }
}
