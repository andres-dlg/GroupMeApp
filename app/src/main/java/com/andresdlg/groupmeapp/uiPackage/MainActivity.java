package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.DialogFragments.AddFriendsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.FriendsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.TermsAndConditionsDialogFragment;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.MessagesFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NotificationFragment;
import com.andresdlg.groupmeapp.uiPackage.login.LoginActivity;
import com.bumptech.glide.Glide;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseUser user;

    //FIREBASE DATABASE REFERENCE
    DatabaseReference mDatabaseRef;

    //FIREBASE STORAGE REFERENCE
    StorageReference mStorageReference;

    NavigationView navigationView;

    ValueEventListener valueEventListener;

    ViewPager viewPager;
    DrawerLayout drawer;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.activity_main_menu);
        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showContactsDialogFragment();
                return false;
            }
        });
        toolbar.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showHeaderDialogFragment();
                return false;
            }
        });

        TextView tv = findViewById(R.id.custom_title);
        Typeface customFont = Typeface.createFromAsset(this.getAssets(),"fonts/Simplifica.ttf");
        tv.setTypeface(customFont);
        tv.setTextColor(getResources().getColor(R.color.colorAccent));
        tv.setTextSize(30);
        //setSupportActionBar(toolbar);


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        //StaticFirebaseSettings.currentUserId = FirebaseAuth.getInstance().getUid();

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.news_fragment, NewsFragment.class)
                .add(R.string.groups_fragment, GroupsFragment.class)
                .add(R.string.notifications_fragment, NotificationFragment.class)
                .add(R.string.messages_fragment, MessagesFragment.class)
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
                ).title("Grupos")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.bell),
                        Color.parseColor(colors[2])
                ).title("Notificaciones")
                        .badgeTitle("state")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.message),
                        Color.parseColor(colors[2])
                ).title("Mensajes")
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
        navigationTabBar.setTitleSize(30);
        navigationTabBar.setIconSizeFraction((float) 0.5);

        //Posiciono mi activity en el fragment
        String fragment = getIntent().getStringExtra("fragment");
        if(fragment!=null){
            if(fragment.equals("NotificationFragment")){
                viewPager.setCurrentItem(2);
            }
            if (fragment.equals("MessagesFragment")){
                viewPager.setCurrentItem(3);
            }
        }

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //FIREBASE DATABASE REFERENCE
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //FIREBASE INSTANCE INITIALIZATION
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        StaticFirebaseSettings.setCurrentUserId(user.getUid());
        mStorageReference = FirebaseStorage.getInstance().getReference();

        //NAVIGATION DRAWER LISTENERS
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void fillDrawer(Users users) {
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.nav_user);
        TextView nav_name = hView.findViewById(R.id.nav_name);
        CircleImageView nav_photo = hView.findViewById(R.id.nav_photo);

        Glide.with(MainActivity.this)
                .load(users.getImageURL().trim())
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
                    if(!dataSnapshot.child(user.getUid()).child("alias").exists()){
                        Intent moveToSetupProfile = new Intent(MainActivity.this,UserProfileSetupActivity.class);
                        moveToSetupProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(moveToSetupProfile);
                        finish();
                    }else{
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
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
        //MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.activity_main_menu, menu);
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
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

        switch(id){
            case R.id.rate_review:
                RatingDialog ratingDialog = new RatingDialog.Builder(MainActivity.this)
                        .title("¿Cómo fue su experiencia con GroupMeApp?")
                        .threshold(6f)
                        .formTitle("Deja tu comentario o sugerencia")
                        .positiveButtonText("Quizas luego")
                        //.negativeButtonText("Nunca")
                        .positiveButtonTextColor(R.color.colorPrimary)
                        //.negativeButtonTextColor(R.color.grey_500)
                        .formHint("¿Cómo podemos mejorar?")
                        .formSubmitText("Enviar")
                        .formCancelText("Cancelar")
                        //.playstoreUrl("YOUR_URL")
                        .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                            @Override
                            public void onFormSubmitted(String feedback) {

                            }
                        }).build();
                ratingDialog.show();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.help:
                //Toast.makeText(MainActivity.this, "Ayuda!", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(Gravity.LEFT);
                letTheShowStart();
                break;
            case R.id.share:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "GroupMeApp");
                    String sAux = "\n¡Recomienda esta aplicación a tus conocidos!\n\n";
                    //sAux = sAux + "https://play.google.com/store/apps/details?id=the.package.id \n\n";
                    sAux = sAux + "https://futurolink.com \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Elije una opción"));
                } catch(Exception e) {
                    //e.toString();
                }
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.about:
                new MaterialDialog.Builder(this)
                        .title("Acerca de GroupMeApp")
                        .titleGravity(GravityEnum.CENTER)
                        .icon(getResources().getDrawable(R.drawable.ic_launcher))
                        .limitIconToDefaultSize()
                        .content("GroupMeApp para Android\n2018")
                        .contentGravity(GravityEnum.CENTER)
                        .positiveText("OK")
                        .show();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.privacy:
                showTermsAndConditionDialogFragment();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.logout:
                DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
                mUserRef.child("token_id").setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
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
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fallo al intentar cerrar sesión", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        return false;
    }

    private void letTheShowStart() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        /*new TapTargetSequence(this)
                .targets(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.contacts,"Test","Test")
                                .outerCircleColor(R.color.colorPrimary)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(30)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(20)
                                .descriptionTextColor(R.color.colorPrimaryDark)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetRadius(60),
                        TapTarget.forToolbarMenuItem(toolbar, R.id.add_contact, "Test2","Test2")
                                .outerCircleColor(R.color.colorPrimary)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(30)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(20)
                                .descriptionTextColor(R.color.colorPrimaryDark)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetRadius(60) // SI QUIERO MAS TARGETS IR SEPARANDO POR COMAS DESDE ACA
                                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });*/


        // EJEMPLO DE TARGET EN UN FRAGMENT
        View v = fragments.get(0).getView().findViewById(R.id.fabFilter);
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(v, "Filtro", "Con este filtro podrás elegir que posts ver ;)")
                        // All options below are optional
                        .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(30)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(20)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.colorPrimaryDark)  // Specify the color of the description text
                        .textColor(R.color.white)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(false)                   // Whether to tint the target view's color
                        .transparentTarget(false)                 // Specify a custom drawable to draw as the target
                        .targetRadius(60),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                    }
                });
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

    private void showTermsAndConditionDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TermsAndConditionsDialogFragment newFragment = new TermsAndConditionsDialogFragment();
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

}
