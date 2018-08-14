package com.andresdlg.groupmeapp.uiPackage;

import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVMeetingsAdapter;
import com.andresdlg.groupmeapp.DialogFragments.NewMeetingDialogFragment;
import com.andresdlg.groupmeapp.Entities.Meeting;
import com.andresdlg.groupmeapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MeetingsActivity extends AppCompatActivity {

    TextView tvNoMeetings;
    ProgressBar progressBar;
    RVMeetingsAdapter meetingsAdapter;
    String groupKey;
    String groupName;
    List<Meeting> meetings;
    private boolean estabaEnElUltimoElemento;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        estabaEnElUltimoElemento = false;
        handler = new Handler();

        groupKey = getIntent().getStringExtra("groupKey");
        groupName = getIntent().getStringExtra("groupName");

        tvNoMeetings = findViewById(R.id.tvNoMeetings);

        progressBar = findViewById(R.id.progressBar);

        final FloatingActionButton fabAddMeeting = findViewById(R.id.fabAddMeeting);
        fabAddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewMeetingDialog();
            }
        });

        meetings = new ArrayList<>();
        meetingsAdapter = new RVMeetingsAdapter(this,meetings,groupKey,groupName);
        RecyclerView rvMeetings = findViewById(R.id.rvMeetings);
        final LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMeetings.setLayoutManager(llm);
        rvMeetings.setAdapter(meetingsAdapter);
        rvMeetings.setItemViewCacheSize(100);
        rvMeetings.setDrawingCacheEnabled(true);
        rvMeetings.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvMeetings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if(llm.findLastCompletelyVisibleItemPosition() != meetings.size()-1){
                        fabAddMeeting.show();
                        estabaEnElUltimoElemento = false;
                    }else{
                        if(recyclerView.canScrollVertically(-1) || recyclerView.canScrollVertically(1)){
                            if (!recyclerView.canScrollVertically(1)) {
                                fabAddMeeting.hide();
                            }
                        }
                    }
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0 || dx < 0 && fabAddMeeting.isShown()){
                    fabAddMeeting.hide();
                }
            }
        });

        getMeetings();
    }

    private void getMeetings() {

        Query query = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("meetings").orderByChild("startTime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if(!dataSnapshot.hasChildren()){
                    tvNoMeetings.setVisibility(View.VISIBLE);
                }else{
                    tvNoMeetings.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Meeting meeting = dataSnapshot.getValue(Meeting.class);
                meetings.add(meeting);
                meetingsAdapter.notifyItemInserted(meetings.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Meeting changedMeeting = dataSnapshot.getValue(Meeting.class);
                for(int i = 0; i<meetings.size();i++){
                    if(meetings.get(i).getMeetingKey().equals(changedMeeting.getMeetingKey())){
                        meetings.remove(i);
                        meetings.add(i,changedMeeting);
                        //meetingsAdapter.notifyItemChanged(i);
                        meetingsAdapter.notifyDataSetChanged();
                        meetingsAdapter.sortMeetings();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Meeting changedMeeting = dataSnapshot.getValue(Meeting.class);
                for(int i = 0; i<meetings.size();i++){
                    if(meetings.get(i).getMeetingKey().equals(changedMeeting.getMeetingKey())){
                        meetings.remove(i);
                        meetingsAdapter.notifyItemRemoved(i);
                        meetingsAdapter.notifyItemRangeChanged(i,meetings.size());
                        if(meetings.size() == 0){
                            tvNoMeetings.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
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

    private void showNewMeetingDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        NewMeetingDialogFragment newFragment = new NewMeetingDialogFragment(groupKey, groupName);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       /* MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_group_menu, menu);*/
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
