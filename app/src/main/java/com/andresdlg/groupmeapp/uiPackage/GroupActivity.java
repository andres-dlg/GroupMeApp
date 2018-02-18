package com.andresdlg.groupmeapp.uiPackage;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.SubGroupsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

public class GroupActivity extends AppCompatActivity {

    ViewPager viewPager;
    String groupKey;
    List<Users> groupUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        groupKey = getIntent().getStringExtra("groupKey");
        String groupName = getIntent().getStringExtra("groupName");
        String groupPhotoUrl = getIntent().getStringExtra("groupImage");
        getSupportActionBar().setTitle(groupName);

        Typeface customFont = Typeface.createFromAsset(this.getAssets(),"fonts/Simplifica.ttf");


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        StaticFirebaseSettings.currentUserId = FirebaseAuth.getInstance().getUid();

        ((FireApp) this.getApplication()).setGroupKey(groupKey);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.sub_groups_fragment, SubGroupsFragment.class)
                /*.add(R.string.notifications_fragment, NotificationFragment.class)
                .add(R.string.messages_fragment, MessagesFragment.class)*/
                .create());

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.newspaper),
                        Color.parseColor(colors[2])
                ).title("Noticias")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.account_multiple),
                        Color.parseColor(colors[2])
                ).title("SubGrupos")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.message),
                        Color.parseColor(colors[2])
                ).title("Chat")
                        .badgeTitle("icon")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setInactiveColor(getResources().getColor(R.color.cardview_dark_background));
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setIsTitled(true);
        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        navigationTabBar.setTypeface(customFont);
        navigationTabBar.setTitleSize(35);
        navigationTabBar.setIconSizeFraction((float) 0.5);

        groupUsers = new ArrayList<>();

        //((FireApp) getApplicationContext()).setGroupKey(groupKey);

        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("members");
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String s = data.getValue().toString();
                    getUser(data.getValue().toString());
                }
                //((FireApp) getApplicationContext()).setGroupUsers(groupUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUser(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("groups").child(groupKey).exists()){
                    if(dataSnapshot.child("groups").child(groupKey).child("status").getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                        Users u = dataSnapshot.getValue(Users.class);
                        groupUsers.add(u);
                        ((FireApp) getApplicationContext()).setGroupUsers(groupUsers);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((FireApp) this.getApplication()).setGroupKey(null);
        ((FireApp) this.getApplication()).setGroupUsers(null);
    }
}
