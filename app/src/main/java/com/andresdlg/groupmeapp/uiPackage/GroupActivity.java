package com.andresdlg.groupmeapp.uiPackage;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alamkanak.weekview.WeekViewEvent;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.Entities.WeekViewEventGroupMeApp;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ABShape;
import com.andresdlg.groupmeapp.Utils.ABTextUtil;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.Utils.RoundRectangle;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupChatFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupNewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.SubGroupsFragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.target.SimpleTarget;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import devlight.io.library.ntb.NavigationTabBar;

public class GroupActivity extends AppCompatActivity implements GroupChatFragment.OnNewMessageListener, GroupNewsFragment.OnNewPostSetListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener{

    DatabaseReference groupRef;
    DatabaseReference userRef;
    DatabaseReference subGroupsRef;
    ValueEventListener groupsEventListener;
    ValueEventListener userEventListener;
    ValueEventListener subGroupsValueEventListener;

    AdView mAdView;

    ViewPager viewPager;
    String groupKey;
    List<Users> groupUsers;
    boolean clicked;
    NavigationTabBar navigationTabBar;
    View dummyView;
    TextView tv;
    CircleImageView civ;
    ArrayList<NavigationTabBar.Model> models;
    Toolbar toolbar;
    View view;
    View groupDetailsToolBarView;

    //FLOATING ACTION BUTTON MENU
    private RapidFloatingActionHelper rfabHelper;
    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaBtn;
    Animation fadeIn;

    String groupName;
    String groupPhotoUrl;

    boolean viewPagerWasInChatPage;
    private boolean presentationStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        MobileAds.initialize(this,"ca-app-pub-6164739277423889~7593283366");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Toast.makeText(GroupActivity.this, "Fallo al cargar anuncio", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clicked = false;

        viewPagerWasInChatPage = false;

        presentationStarted = false;

        dummyView = findViewById(R.id.dummyView);

        view = findViewById(R.id.view);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        groupKey = getIntent().getStringExtra("groupKey");
        groupName = getIntent().getStringExtra("groupName");
        groupPhotoUrl = getIntent().getStringExtra("groupImage");

        ((FireApp) this.getApplication()).setGroupKey(groupKey);

        tv = toolbar.findViewById(R.id.action_bar_title_1);
        civ = toolbar.findViewById(R.id.conversation_contact_photo);

        tv.setText(groupName);
        Glide.with(this)
                .load(groupPhotoUrl)
                .into(civ);

        groupDetailsToolBarView = toolbar.findViewById(R.id.toolbar_container);
        setGroupDetailsToolBarViewListener();

        //Typeface customFont = Typeface.createFromAsset(this.getAssets(),"fonts/Simplifica.ttf");

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        //StaticFirebaseSettings.currentUserId = FirebaseAuth.getInstance().getUid();


        Bundle bundle = new Bundle();
        bundle.putString("groupKey",groupKey);
        bundle.putBoolean("fromNotificationSubGroupInvitation",getIntent().getBooleanExtra("fromNotificationSubGroupInvitation",false));
        bundle.putBoolean("fromNotificationNewPost",getIntent().getBooleanExtra("fromNotificationNewPost",false));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, GroupNewsFragment.class,bundle)
                .add(R.string.sub_groups_fragment, SubGroupsFragment.class,bundle)
                .add("Chat", GroupChatFragment.class,bundle)
                .create());

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        navigationTabBar = findViewById(R.id.ntb);
        models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        ContextCompat.getDrawable(this,R.drawable.newspaper),
                        Color.parseColor(colors[2])
                ).title("Noticias")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        ContextCompat.getDrawable(this,R.drawable.account_multiple),
                        Color.parseColor(colors[2])
                ).title("SubGrupos")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        ContextCompat.getDrawable(this,R.drawable.message),
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
        //navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        //navigationTabBar.setTypeface(customFont);
        navigationTabBar.setTitleSize(10 * getResources().getDisplayMetrics().density);
        navigationTabBar.setIconSizeFraction((float) 0.5);

        navigationTabBar.setBadgePosition(NavigationTabBar.BadgePosition.RIGHT);
        navigationTabBar.setIsBadged(true);
        navigationTabBar.setBadgeBgColor(Color.RED);
        navigationTabBar.setBadgeTitleColor(Color.RED);
        navigationTabBar.setBadgeSize(20);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Toast.makeText(GroupActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 2){

                    /*Animation myAnim = AnimationUtils.loadAnimation(GroupActivity.this, R.anim.fade_out);
                    rfaLayout.startAnimation(myAnim);*/
                    rfaLayout.setVisibility(View.GONE);

                    viewPagerWasInChatPage = true;

                    final int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, metrics));

                    navigationTabBar.animate().translationY(px);
                    dummyView.animate().translationY(px);
                    mAdView.animate().translationY(px);

                    int newMarginDp = 4;

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

                }else if(position == 1 || position == 0){

                    if(rfaLayout.getVisibility() == View.GONE){
                        rfaLayout.setVisibility(View.VISIBLE);
                        rfaBtn.startAnimation(fadeIn);
                    }

                    if(viewPagerWasInChatPage){

                        /*Animation myAnim = AnimationUtils.loadAnimation(GroupActivity.this, R.anim.fade_in);
                        rfaLayout.startAnimation(myAnim);*/
                        rfaLayout.setVisibility(View.VISIBLE);

                        navigationTabBar.animate().translationY(0);
                        dummyView.animate().translationY(0);
                        mAdView.animate().translationY(0);

                        int newMarginDp = 96;
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

                        viewPagerWasInChatPage = false;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        boolean setSubGroupTab = getIntent().getBooleanExtra("setSubGroupTab",false);
        boolean setChatTab = getIntent().getBooleanExtra("setChatTab",false);
        boolean setNewsTab = getIntent().getBooleanExtra("setNewsTab",false);


        if(setSubGroupTab){
            viewPager.setCurrentItem(0);
            viewPager.setCurrentItem(1);
        }else if(setChatTab){
            viewPager.setCurrentItem(1);
            viewPager.setCurrentItem(2);
        }else if(setNewsTab){
            viewPager.setCurrentItem(0);
        }

        groupUsers = new ArrayList<>();

        ((FireApp) this.getApplication()).setGroupName(groupName);


        //FLOATING ACTION BUTTON MENU
        rfaLayout = findViewById(R.id.activity_main_rfal);
        rfaBtn = findViewById(R.id.activity_main_rfab);

        rfaLayout.setFrameAlpha(0.8f);
        rfaLayout.setFrameColor(0xff000000);

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Nueva publicación")
                .setResId(R.drawable.ic_pen_white_24dp)
                .setIconNormalColor(0xff3F51B5)
                .setIconPressedColor(0xff303F9F)
                //.setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(this, 4)))
                //.setLabelColor(Color.WHITE)
                .setLabelColor(0xff303F9F)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Crear subgrupo")
                .setResId(R.drawable.ic_account_multiple_white_24dp)
                .setIconNormalColor(0xff3F51B5)
                .setIconPressedColor(0xff303F9F)
                .setLabelSizeSp(14)
                //.setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(this, 4)))
                //.setLabelColor(Color.WHITE)
                .setLabelColor(0xff303F9F)
                .setWrapper(1)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Ver archivos compartidos")
                .setResId(R.drawable.ic_file_document_white_24dp)
                .setIconNormalColor(0xff3F51B5)
                .setIconPressedColor(0xff303F9F)
                //.setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(this, 4)))
                //.setLabelColor(Color.WHITE)
                .setLabelColor(0xff303F9F)
                .setWrapper(2)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Vista global de tareas")
                .setResId(R.drawable.ic_calendar_range_white_24dp)
                .setIconNormalColor(0xff3F51B5)
                .setIconPressedColor(0xff303F9F)
                //.setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(this, 4)))
                //.setLabelColor(Color.WHITE)
                .setLabelColor(0xff303F9F)
                .setWrapper(3)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Agenda")
                .setResId(R.drawable.ic_calendar_white_24dp)
                .setIconNormalColor(0xff3F51B5)
                .setIconPressedColor(0xff303F9F)
                //.setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(this, 4)))
                //.setLabelColor(Color.WHITE)
                .setLabelColor(0xff303F9F)
                .setWrapper(4)
        );
        rfaContent
                .setItems(items)
                //.setIconShadowRadius(ABTextUtil.dip2px(this, 5))
                //.setIconShadowColor(0xff888888)
                //.setIconShadowDy(ABTextUtil.dip2px(this, 5));
                .setIconShadowColor(0x00888888)
                .setIconShadowRadius(0)
                .setIconShadowDy(0);
        rfabHelper = new RapidFloatingActionHelper(
                this,
                rfaLayout,
                rfaBtn,
                rfaContent
        ).build();

        setAnimations();

        fetchContacts();
    }

    private void setGroupDetailsToolBarViewListener() {
        groupDetailsToolBarView.setOnClickListener(new View.OnClickListener() {
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
    }

    private void fetchContacts(){
        groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("members");
        groupsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
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
        if(!presentationStarted) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    return true;
                case R.id.help:
                    if (viewPager.getCurrentItem() == 2) {
                        viewPager.setCurrentItem(1);
                        viewPager.setCurrentItem(0);
                    } else {
                        viewPager.setCurrentItem(0);
                    }
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            letTheFirstPartOfTheShowBegin();
                        }
                    }, 1000);
                    return true;
                /*case R.id.dates:
                    clicked = true;
                    final Intent intent = new Intent(GroupActivity.this, TaskWeekViewActivity.class);
                    intent.putExtra("groupKey", groupKey);

                    ((FireApp) getApplicationContext()).setEvents(null);

                    final List<WeekViewEvent> events = new ArrayList<>();
                    final List<WeekViewEventGroupMeApp> eventsGroupMeApp = new ArrayList<>();

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
                            for (DataSnapshot subgroupRef : dataSnapshot.getChildren()) {
                                for (DataSnapshot taskRef : subgroupRef.child("tasks").getChildren()) {
                                    Task task = taskRef.getValue(Task.class);

                                    if (task.getStartDate() != 0 && task.getEndDate() != 0) {

                                        Calendar taskStartDateTime = Calendar.getInstance();
                                        taskStartDateTime.setTimeInMillis(task.getStartDate());

                                        Calendar taskEndDateTime = Calendar.getInstance();
                                        taskEndDateTime.setTimeInMillis(task.getEndDate());

                                        WeekViewEventGroupMeApp eventGroupMeApp = new WeekViewEventGroupMeApp(i, task.getName(), groupKey, subgroupRef.child("subGroupKey").getValue().toString(), task.getTaskKey(), task.getTaskDescription(), task.getFinished(), taskStartDateTime, taskEndDateTime);
                                        WeekViewEvent event = new WeekViewEvent(i, subgroupRef.child("name").getValue() + "-" + task.getName(), groupKey + "RQYg6ybUaE|sep" + subgroupRef.child("subGroupKey").getValue() + "RQYg6ybUaE|sep" + task.getTaskKey() + "RQYg6ybUaE|sep" + task.getTaskDescription() + "RQYg6ybUaE|sep" + task.getFinished(), taskStartDateTime, taskEndDateTime);
                                        event.setColor(getResources().getColor(R.color.colorPrimary));
                                        eventsGroupMeApp.add(eventGroupMeApp);
                                        events.add(event);
                                        i++;
                                    }
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            ((FireApp) getApplicationContext()).setEvents(events);
                            ((FireApp) getApplicationContext()).setEventsGroupMeApp(eventsGroupMeApp);
                            if (clicked) {
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

                case R.id.files:
                    Intent i = new Intent(this, GroupFilesActivity.class);
                    i.putExtra("groupKey", groupKey);
                    i.putExtra("groupName", groupName);
                    startActivity(i);
                    return true;*/
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        return false;
    }

    private void letTheFirstPartOfTheShowBegin() {

        int[] oneLocation;
        float oneX;
        float oneY;

        // 1) TARGET PANEL DE NAVEGACIÓN
        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setAnimation(new DecelerateInterpolator(2f))
                .setShape(new RoundRectangle(0,oneLocation[1],navigationTabBar.getWidth(),navigationTabBar.getHeight()))
                .setTitle("Panel de navegación")
                .setDescription("Aquí podrás navegar por las secciones de Noticias de Grupo, Subgrupos y el Chat grupal")
                .build();

        // 2) TARGET PESTAÑA NOTICIAS
        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarNewsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setAnimation(new DecelerateInterpolator(2f))
                .setShape(new RoundRectangle(0,oneLocation[1],navigationTabBar.getWidth()/3,navigationTabBar.getHeight()))
                .setTitle("Sección de noticias")
                .setDescription("Aquí podrás visualizar todas las noticias publicadas en este grupo")
                .build();

        // 3) TARGET FAB NUEVO POST
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        View fabNewPost = fragments.get(1).getView().findViewById(R.id.fabNewPost);
        oneLocation = new int[2];
        fabNewPost.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + fabNewPost.getWidth() / 2f;
        oneY = oneLocation[1] + fabNewPost.getHeight() / 2f;
        SimpleTarget fabNewPostTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(200f))
                .setTitle("Nueva publicación")
                .setDescription("Aquí podrás escribir una publicación y guardarla para que los demas miembros puedan verla")
                .build();*/

        //EMPIEZA LA PRIMER PARTE DEL SHOW
        Spotlight spotlight = Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                //.setTargets(tabBarTarget,tabBarNewsTarget,fabNewPostTarget)
                .setTargets(tabBarTarget,tabBarNewsTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                        presentationStarted = true;
                        view.setVisibility(View.VISIBLE);
                        groupDetailsToolBarView.setOnClickListener(null);
                    }

                    @Override
                    public void onEnded() {
                        viewPager.setCurrentItem(1);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                letTheSecondPartOfTheShowBegin();
                            }
                        }, 1000);
                    }
                });

        spotlight.start();

    }

    private void letTheSecondPartOfTheShowBegin() {

        int[] oneLocation;
        float oneX;
        float oneY;

        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarSubGroupsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setAnimation(new DecelerateInterpolator(2f))
                .setShape(new RoundRectangle(navigationTabBar.getWidth()/3,oneLocation[1],navigationTabBar.getWidth() - navigationTabBar.getWidth()/3,navigationTabBar.getHeight()))
                .setTitle("Sección de subgrupos")
                .setDescription("Aquí podrás visualizar los subgrupos existentes y acceder a todos sus detalles (si eres miembro) incluyendo la creación de tareas")
                .build();

        // 3) TARGET FAB NUEVO POST
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        View fabNewPost = fragments.get(2).getView().findViewById(R.id.fabSubGroups);
        oneLocation = new int[2];
        fabNewPost.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + fabNewPost.getWidth() / 2f;
        oneY = oneLocation[1] + fabNewPost.getHeight() / 2f;
        SimpleTarget fabSubGroupsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(200f))
                .setTitle("Nuevo subgrupo")
                .setDescription("Desde aquí podrás crear un nuevo subgrupo y añadir a los miembros del grupo que tu quieras")
                .build();*/


        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarChatTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setAnimation(new DecelerateInterpolator(2f))
                .setShape(new RoundRectangle(navigationTabBar.getWidth() - navigationTabBar.getWidth()/3,oneLocation[1],navigationTabBar.getWidth(),navigationTabBar.getHeight()))
                .setTitle("Sección de chat")
                .setDescription("Aquí podrás chatear con tus compañeros de grupo sin importar a que subgrupo pertenezcan")
                .build();

        // 3) TARGET DATES
        /*View dates = toolbar.findViewById(R.id.dates);
        oneLocation = new int[2];
        dates.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + dates.getWidth() / 2f;
        oneY = oneLocation[1] + dates.getHeight() / 2f;
        SimpleTarget datesTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(100f))
                .setTitle("Vista global de tareas")
                .setDescription("Una vez que hayas definido algunas tareas, podrás ver aquí la duración de las mismas de forma mas amigable y tambien como se superponen en el calendario. Podrás hacer click en cualquier tarea para ver sus detalles")
                .build();*/

        //float datesX = oneX;

        // 4) TARGET FILES
        /*View files = toolbar.findViewById(R.id.files);
        oneLocation = new int[2];
        files.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + files.getWidth() / 2f;
        oneY = oneLocation[1] + files.getHeight() / 2f;
        SimpleTarget filesTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(100f))
                .setTitle("Archivos compartidos")
                .setDescription("Cuando el administrador del grupo o los miembros de algun subgrupo compartan archivos podrás descargarlos, compartirlos o, si tienes permisos, eliminarlos desde esta pantalla")
                .build();*/


        // 5) TARGET GROUP
        /*oneLocation = new int[2];
        toolbar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + dates.getWidth() / 2f;
        oneY = oneLocation[1] + dates.getHeight() / 2f;
        SimpleTarget toolbarTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle(150,oneLocation[1]-10,datesX-100,toolbar.getHeight()))
                .setTitle("Detalles de grupo")
                .setDescription("Si tocas en el nombre del grupo podrás ver todo el detalle de este grupo incluyendo miembros y objetivos")
                .build();*/

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                //.setTargets(tabBarSubGroupsTarget,fabSubGroupsTarget,tabBarChatTarget,datesTarget,filesTarget,toolbarTarget)
                .setTargets(tabBarSubGroupsTarget,tabBarChatTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onEnded() {
                        view.setVisibility(View.GONE);
                        setGroupDetailsToolBarViewListener();
                        presentationStarted = false;
                    }
                })
                .start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_group_menu, menu);
        return true;
    }

    @Override
    public void onNewMessage(int messageQuantity) {
        NavigationTabBar.Model model = models.get(2);
        if(messageQuantity > 0){
            model.showBadge();
            model.setBadgeTitle(String.valueOf(messageQuantity));
        }else{
            model.hideBadge();
        }
    }

    @Override
    public void onNewPostSet(int postQuantity) {
        NavigationTabBar.Model model = models.get(0);
        if(postQuantity > 0){
            model.showBadge();
            model.setBadgeTitle(String.valueOf(postQuantity));
        }else{
            model.hideBadge();
        }
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {

        switch (position){
            case 0:
                Intent newPostIntent = new Intent(this, NewPostActivity.class);
                startActivity(newPostIntent);
                viewPager.setCurrentItem(0);
                break;
            case 1:
                showHeaderDialogFragment();
                break;
            case 2:
                Intent i = new Intent(this, GroupFilesActivity.class);
                i.putExtra("groupKey", groupKey);
                i.putExtra("groupName", groupName);
                startActivity(i);
                break;
            case 3:
                clicked = true;
                final Intent intent = new Intent(GroupActivity.this, TaskWeekViewActivity.class);
                intent.putExtra("groupKey", groupKey);

                ((FireApp) getApplicationContext()).setEvents(null);

                final List<WeekViewEvent> events = new ArrayList<>();
                final List<WeekViewEventGroupMeApp> eventsGroupMeApp = new ArrayList<>();

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
                        for (DataSnapshot subgroupRef : dataSnapshot.getChildren()) {
                            for (DataSnapshot taskRef : subgroupRef.child("tasks").getChildren()) {
                                Task task = taskRef.getValue(Task.class);

                                if (task.getStartDate() != 0 && task.getEndDate() != 0) {

                                    Calendar taskStartDateTime = Calendar.getInstance();
                                    taskStartDateTime.setTimeInMillis(task.getStartDate());

                                    Calendar taskEndDateTime = Calendar.getInstance();
                                    taskEndDateTime.setTimeInMillis(task.getEndDate());

                                    WeekViewEventGroupMeApp eventGroupMeApp = new WeekViewEventGroupMeApp(i, task.getName(), groupKey, subgroupRef.child("subGroupKey").getValue().toString(), task.getTaskKey(), task.getTaskDescription(), task.getFinished(), taskStartDateTime, taskEndDateTime);
                                    WeekViewEvent event = new WeekViewEvent(i, subgroupRef.child("name").getValue() + "-" + task.getName(), groupKey + "RQYg6ybUaE|sep" + subgroupRef.child("subGroupKey").getValue() + "RQYg6ybUaE|sep" + task.getTaskKey() + "RQYg6ybUaE|sep" + task.getTaskDescription() + "RQYg6ybUaE|sep" + task.getFinished(), taskStartDateTime, taskEndDateTime);
                                    event.setColor(getResources().getColor(R.color.colorPrimary));
                                    eventsGroupMeApp.add(eventGroupMeApp);
                                    events.add(event);
                                    i++;
                                }
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        ((FireApp) getApplicationContext()).setEvents(events);
                        ((FireApp) getApplicationContext()).setEventsGroupMeApp(eventsGroupMeApp);
                        if (clicked) {
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
                break;
            case 4:
                Intent meetingsIntent = new Intent(this,MeetingsActivity.class);
                meetingsIntent.putExtra("groupKey",groupKey);
                meetingsIntent.putExtra("groupName",groupName);
                startActivity(meetingsIntent);
        }

        //Toast.makeText(this, "clicked label: " + position, Toast.LENGTH_SHORT).show();
        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        onRFACItemLabelClick(position,item);
        //Toast.makeText(this, "clicked icon: " + position, Toast.LENGTH_SHORT).show();
        //rfabHelper.toggleContent();
    }

    private void setAnimations(){
        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(300);
        fadeIn.setFillEnabled(true);
        fadeIn.setFillAfter(true);
    }

    private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        HeaderDialogFragment newFragment = new HeaderDialogFragment(GroupType.SUBGROUP,groupKey);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }
}
