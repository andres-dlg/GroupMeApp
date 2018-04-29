package com.andresdlg.groupmeapp.uiPackage;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alamkanak.weekview.WeekViewEvent;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupChatFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.SubGroupsFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import devlight.io.library.ntb.NavigationTabBar;

public class GroupActivity extends AppCompatActivity{

    DatabaseReference groupRef;
    DatabaseReference userRef;
    DatabaseReference subGroupsRef;
    ValueEventListener groupsEventListener;
    ValueEventListener userEventListener;
    ValueEventListener subGroupsValueEventListener;

    ViewPager viewPager;
    String groupKey;
    List<Users> groupUsers;
    boolean clicked;
    NavigationTabBar navigationTabBar;
    View dummyView;
    TextView tv;
    CircleImageView civ;

    String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clicked = false;

        dummyView = findViewById(R.id.dummyView);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        groupKey = getIntent().getStringExtra("groupKey");
        groupName = getIntent().getStringExtra("groupName");
        final String groupPhotoUrl = getIntent().getStringExtra("groupImage");

        tv = toolbar.findViewById(R.id.action_bar_title_1);
        civ = toolbar.findViewById(R.id.conversation_contact_photo);

        tv.setText(groupName);
        Glide.with(this)
                .load(groupPhotoUrl)
                .into(civ);

        View v = toolbar.findViewById(R.id.toolbar_container);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, GroupDetailActivity.class);
                // Pass data object in the bundle and populate details activity.


                intent.putExtra("groupName", groupName);
                intent.putExtra("groupPhotoUrl", groupPhotoUrl);
                intent.putExtra("groupKey", groupKey);
                Pair<View, String> p1 = Pair.create((View)civ, "photo");
                Pair<View, String> p2 = Pair.create((View)tv, "text");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(GroupActivity.this, p1, p2);
                startActivity(intent, options.toBundle());
            }
        });

        Typeface customFont = Typeface.createFromAsset(this.getAssets(),"fonts/Simplifica.ttf");


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        StaticFirebaseSettings.currentUserId = FirebaseAuth.getInstance().getUid();

        ((FireApp) this.getApplication()).setGroupKey(groupKey);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.sub_groups_fragment, SubGroupsFragment.class)
                .add("Chat", GroupChatFragment.class)
                //.add(R.string.messages_fragment, MessagesFragment.class)
                .create());

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        navigationTabBar = findViewById(R.id.ntb);
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


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Toast.makeText(GroupActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 2){
                    ((View)navigationTabBar).animate().translationY(140);
                    dummyView.animate().translationY(140);

                    int newMarginDp = 4;
                    //final int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));

                    final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)viewPager.getLayoutParams();
                    ValueAnimator valueAnimator = ValueAnimator.ofInt(params.bottomMargin,newMarginDp);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            params.bottomMargin = (Integer)valueAnimator.getAnimatedValue();
                            viewPager.requestLayout();
                        }
                    });
                    valueAnimator.setDuration(300);
                    valueAnimator.start();

                }else{
                    ((View)navigationTabBar).animate().translationY(0);
                    dummyView.animate().translationY(0);

                    int newMarginDp = 50;
                    final int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newMarginDp, metrics));

                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)viewPager.getLayoutParams();
                            params.bottomMargin = (int)(px * interpolatedTime);
                            viewPager.setLayoutParams(params);
                        }
                    };
                    a.setDuration(500);
                    viewPager.startAnimation(a);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        groupUsers = new ArrayList<>();

        ((FireApp) this.getApplication()).setGroupName(groupName);

        fetchContacts();

    }

    private void fetchContacts(){
        groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("members");
        groupsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    String s = data.getValue().toString();
                    getUser(data.getKey());
                }
                //((FireApp) getApplicationContext()).setGroupUsers(groupUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        groupRef.addValueEventListener(groupsEventListener);
    }

    private void getUser(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userEventListener  = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //groupUsers.clear();
                if(dataSnapshot.child("groups").child(groupKey).exists()){
                    if(dataSnapshot.child("groups").child(groupKey).child("status").getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                        Users u = dataSnapshot.getValue(Users.class);
                        if(!validateExistingMembers(u)){
                            groupUsers.add(u);
                        }
                        ((FireApp) getApplicationContext()).setGroupUsers(groupUsers);
                        /*Bundle objectId = new Bundle();
                        objectId.putBoolean("loaded", true);*/
                        GroupChatFragment.setGroupUsers(groupUsers);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addListenerForSingleValueEvent(userEventListener);
    }

    private boolean validateExistingMembers(Users u) {
        for(Users user : groupUsers){
            if(u.getUserid().equals(user.getUserid())){
                return true;
            }
        }
        return false;
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        groupRef.removeEventListener(groupsEventListener);
        userRef.removeEventListener(userEventListener);
        if(subGroupsValueEventListener != null){
            subGroupsRef.removeEventListener(subGroupsValueEventListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        tv.setText(((FireApp) this.getApplication()).getGroupName());

        String url = ((FireApp) this.getApplication()).getDownloadUrl();
        if(!TextUtils.isEmpty(url)){
            Glide.with(this)
                    .load(url)
                    .into(civ);
            //Picasso.with(this).load(url).into(civ);
        }

        if(groupUsers == null){
            groupUsers = new ArrayList<>();
        }else{
            groupUsers.clear();
        }
        fetchContacts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((FireApp) this.getApplication()).setGroupKey(null);
        ((FireApp) this.getApplication()).setGroupUsers(null);
        ((FireApp) this.getApplication()).setEvents(null);
        ((FireApp) this.getApplication()).setGroupName(null);
        ((FireApp) this.getApplication()).setGroupPhoto(null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.dates:
                clicked = true;
                final Intent intent = new Intent(GroupActivity.this, TaskWeekViewActivity.class);
                intent.putExtra("groupKey",groupKey);

                ((FireApp) getApplicationContext()).setEvents(null);

                final List<WeekViewEvent> events = new ArrayList<>();

                final ProgressBar progressBar = new ProgressBar(this);
                progressBar.setVisibility(View.VISIBLE);

                subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups")
                        .child(groupKey)
                        .child("subgroups");

                subGroupsValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ((FireApp) getApplicationContext()).setEvents(null);
                        int i = 0;
                        for(DataSnapshot subgroupRef: dataSnapshot.getChildren()){
                            for(DataSnapshot taskRef: subgroupRef.child("tasks").getChildren()){
                                Task task = taskRef.getValue(Task.class);

                                if(!TextUtils.isEmpty(task.getStartDate()) && !TextUtils.isEmpty(task.getEndDate())){

                                    Calendar taskStartDateTime = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                                    try {
                                        taskStartDateTime.setTime(dateFormat.parse(task.getStartDate()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    Calendar taskEndDateTime = Calendar.getInstance();
                                    try {
                                        taskEndDateTime.setTime(dateFormat.parse(task.getEndDate()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                /*Calendar startTime = Calendar.getInstance();
                                startTime.set(Calendar.HOUR_OF_DAY, taskStartDateTime.get(Calendar.HOUR_OF_DAY));
                                startTime.set(Calendar.MINUTE, taskStartDateTime.get(Calendar.MINUTE));
                                startTime.set(Calendar.MONTH, taskStartDateTime.get(Calendar.MONTH));
                                startTime.set(Calendar.YEAR, taskStartDateTime.get(Calendar.YEAR));

                                Calendar endTime = Calendar.getInstance();
                                endTime.set(Calendar.HOUR_OF_DAY, taskEndDateTime.get(Calendar.HOUR_OF_DAY));
                                endTime.set(Calendar.MINUTE, taskEndDateTime.get(Calendar.MINUTE));
                                endTime.set(Calendar.MONTH, taskEndDateTime.get(Calendar.MONTH));
                                endTime.set(Calendar.YEAR, taskEndDateTime.get(Calendar.YEAR));*/

                                    //WeekViewEvent event = new WeekViewEvent(i, task.getName(), startTime, endTime);
                                    WeekViewEvent event = new WeekViewEvent(i, task.getName(), taskStartDateTime, taskEndDateTime);
                                    event.setColor(getResources().getColor(R.color.colorPrimary));
                                    events.add(event);
                                    i++;
                                }
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        ((FireApp) getApplicationContext()).setEvents(events);
                        if (clicked){
                            startActivity(intent);
                            clicked = false;
                        }
                        clicked = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

                subGroupsRef.addValueEventListener(subGroupsValueEventListener);

                //startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_group_menu, menu);
        return true;
    }

}
