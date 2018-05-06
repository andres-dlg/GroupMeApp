package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.NewPostActivity;

/**
 * Created by andresdlg on 02/05/17.
 */

public class NewsFragment extends Fragment {

    FloatingActionButton fab;
    RecyclerView rvPosts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news,container,false);

        fab = view.findViewById(R.id.fabNewPost);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), NewPostActivity.class);
                getContext().startActivity(i);
            }
        });

        rvPosts = view.findViewById(R.id.rvPosts);
        //rvPosts.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(llm);



        return view;
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
