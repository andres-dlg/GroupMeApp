package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.MessagesFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NotificationFragment;
import com.andresdlg.groupmeapp.uiPackage.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    //FIREBASE DATABASE REFERENCE
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isLoggedIn = getIntent().getBooleanExtra("isLoggedIn", false);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.groups_fragment, GroupsFragment.class)
                .add(R.string.notifications_fragment, NotificationFragment.class)
                .add(R.string.messages_fragment, MessagesFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        final LayoutInflater inflater = LayoutInflater.from(this);
        final Resources res = getResources();

        final SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                View itemView = inflater.inflate(R.layout.tab_icon, container, false);
                ImageView icon = (ImageView) itemView.findViewById(R.id.custom_tab_icon);

                //Obtengo las metricas de la pantalla
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                //Divido por la cantidad de fragmentos y determino el ancho del imageview que va en
                // cada tab
                icon.getLayoutParams().width = metrics.widthPixels / 4;

                switch (position) {
                    case 0:
                        icon.setImageDrawable(res.getDrawable(R.drawable.newspaper));
                        break;
                    case 1:
                        icon.setImageDrawable(res.getDrawable(R.drawable.account_multiple));
                        break;
                    case 2:
                        icon.setImageDrawable(res.getDrawable(R.drawable.bell));
                        break;
                    case 3:
                        icon.setImageDrawable(res.getDrawable(R.drawable.message));
                        break;
                    default:
                        throw new IllegalStateException("Invalid position: " + position);
                }
                return itemView;
            }
        });

        viewPagerTab.setViewPager(viewPager);

        /*viewPagerTab.setOnTabClickListener(new SmartTabLayout.OnTabClickListener() {
            @Override
            public void onTabClicked(int position) {
                viewPagerTab.getTabAt(position).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        });*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //FIREBASE INSTANCE INITIALIZATION
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                final String userId = user.getUid();

                if(user!= null){
                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Si ya configuro su perfil(no hago nada y muestro la pantalla principal)
                            // sino (le abro la pantalla de configuración)
                            if(dataSnapshot.hasChild(userId)){
                            }else{
                                finish();
                                Intent moveToSetupProfile = new Intent(MainActivity.this,UserProfileSetupActivity.class);
                                moveToSetupProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(moveToSetupProfile);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    finish();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        //FIREBASE DATABASE REFERENCE
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
