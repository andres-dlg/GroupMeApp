package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.DialogFragments.AddFriendsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.FriendsDialogFragment;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.MessagesFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NotificationFragment;
import com.andresdlg.groupmeapp.uiPackage.login.LoginActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    //FIREBASE DATABASE REFERENCE
    DatabaseReference mDatabaseRef;

    //FIREBASE STORAGE REFERENCE
    StorageReference mStorageReference;

    NavigationView navigationView;

    ValueEventListener valueEventListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StaticFirebaseSettings.currentUserId = FirebaseAuth.getInstance().getUid();

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.groups_fragment, GroupsFragment.class)
                .add(R.string.notifications_fragment, NotificationFragment.class)
                .add(R.string.messages_fragment, MessagesFragment.class)
                .create());

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

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

        //Posiciono mi activity en el fragment
        String fragment = getIntent().getStringExtra("fragment");
        if(fragment!=null){
            if(fragment.equals("NotificationFragment")){
                viewPager.setCurrentItem(2);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //FIREBASE DATABASE REFERENCE
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //FIREBASE INSTANCE INITIALIZATION
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void fillDrawer(Users users) {
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.nav_user);
        TextView nav_name = hView.findViewById(R.id.nav_name);
        CircleImageView nav_photo = hView.findViewById(R.id.nav_photo);

        Picasso.with(MainActivity.this)
                .load(users.getImageURL().trim())
                .fit()
                .into(nav_photo);

        if(users !=null){
            nav_user.setText(String.format("@%s", users.getAlias()));
            nav_name.setText(users.getName());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()==null){
            sendToLogin();
        }else{
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Si no configuro su perfil le abro la pantalla de configuración
                    // sino no hago nada y muestro la pantalla principal
                    //if(!dataSnapshot.hasChild(user.getUid())){
                    if(!dataSnapshot.child(user.getUid()).child("alias").exists()){
                        Intent moveToSetupProfile = new Intent(MainActivity.this,UserProfileSetupActivity.class);
                        moveToSetupProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(moveToSetupProfile);
                        finish();
                    }else{
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        db.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Users u = dataSnapshot.getValue(Users.class);
                                fillDrawer(u);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDatabaseRef.addListenerForSingleValueEvent(valueEventListener);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_contact:
                showHeaderDialogFragment();
                return true;
            case R.id.contacts:
                showContactsDialogFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_log_out){

            Map<String,Object> tokenMap = new HashMap<>();
            tokenMap.put("token_id","");

            DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());
            mUserRef.updateChildren(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    mDatabaseRef.removeEventListener(valueEventListener);
                    mAuth.signOut();
                    FirebaseAuth.getInstance().signOut();
                    Intent moveToLogin = new Intent(MainActivity.this,LoginActivity.class);
                    moveToLogin.putExtra("logout",true);
                    moveToLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(moveToLogin);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();

                }
            });


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendToLogin(){
        Intent moveToLogin = new Intent(MainActivity.this,LoginActivity.class);
        moveToLogin.putExtra("logout",true);
        moveToLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(moveToLogin);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }


    private void showHeaderDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddFriendsDialogFragment newFragment = new AddFriendsDialogFragment();
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void showContactsDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FriendsDialogFragment newFragment = new FriendsDialogFragment();
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

}
