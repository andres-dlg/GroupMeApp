package com.andresdlg.groupmeapp.uiPackage;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andresdlg.groupmeapp.DialogFragments.NewMeetingDialogFragment;
import com.andresdlg.groupmeapp.R;

public class MeetingsActivity extends AppCompatActivity {

    FloatingActionButton fabAddMeeting;
    String groupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupKey = getIntent().getStringExtra("groupKey");

        FloatingActionButton fabAddMeeting = findViewById(R.id.fabAddMeeting);
        fabAddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewMeetingDialog();
            }
        });
    }

    private void showNewMeetingDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        NewMeetingDialogFragment newFragment = new NewMeetingDialogFragment(groupKey);
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
