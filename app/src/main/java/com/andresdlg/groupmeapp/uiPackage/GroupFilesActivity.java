package com.andresdlg.groupmeapp.uiPackage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.andresdlg.groupmeapp.Adapters.RVGroupFilesSubgroupsAdapter;
import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupFilesActivity extends AppCompatActivity {

    public RVGroupFilesSubgroupsAdapter adapter;
    RecyclerView recyclerView;

    String groupKey;
    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_files);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Archvos compartidos");

        groupKey = getIntent().getStringExtra("groupKey");
        groupName = getIntent().getStringExtra("groupName");

        recyclerView = findViewById(R.id.rvSubGroupFiles);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        // RecyclerView has some built in animations to it, using the DefaultItemAnimator.
        // Specifically when you call notifyItemChanged() it does a fade animation for the changing
        // of the data in the ViewHolder. If you would like to disable this you can use the following:
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        getSubgroupsAndFiles();



    }

    private void getSubgroupsAndFiles() {
        FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<SubGroup> subGroups = new ArrayList<>();

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    SubGroup sgf = new SubGroup(data.child("name").getValue().toString(),null,data.child("imageUrl").getValue().toString());
                    List<File> files = new ArrayList<>();
                    for(DataSnapshot d : data.child("files").getChildren()){
                        File file = d.getValue(File.class);
                        files.add(file);
                    }
                    sgf.setFiles(files);
                    if(sgf.getFiles().size() > 0){
                        SubGroup sg = new SubGroup(sgf.getName(),sgf.getFiles(),sgf.getImageUrl());
                        subGroups.add(sg);
                    }
                }
                adapter = new RVGroupFilesSubgroupsAdapter(subGroups,GroupFilesActivity.this,groupName);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        adapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.onRestoreInstanceState(savedInstanceState);
    }

}
