package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.DialogFragments.AddFriendsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.FriendsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.LibrariesDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.TermsAndConditionsDialogFragment;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.RoundRectangle;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.MessagesFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NewsFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.NotificationFragment;
import com.andresdlg.groupmeapp.uiPackage.login.LoginActivity;
import com.bumptech.glide.Glide;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import devlight.io.library.ntb.NavigationTabBar;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        NotificationFragment.OnNewNotificationSetListener,
        NewsFragment.OnNewPostSetListener,
        MessagesFragment.OnNewMessageListener{

    //private static final String AD_UNIT_ID = "ca-app-pub-6164739277423889/8658953023";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";

    AdView mAdView;

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
    CircleImageView nav_photo;

    ArrayList<NavigationTabBar.Model> models;
    NavigationTabBar navigationTabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //MobileAds.initialize(this,"ca-app-pub-3940256099942544~3347511713");
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
                Toast.makeText(MainActivity.this, "Fallo al cargar anuncio", Toast.LENGTH_SHORT).show();
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

        //createBanner();

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

        navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb);
        models = new ArrayList<>();
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
        //navigationTabBar.setTypeface(customFont);
        navigationTabBar.setTitleSize(25);
        navigationTabBar.setIconSizeFraction((float) 0.5);

        navigationTabBar.setBadgePosition(NavigationTabBar.BadgePosition.RIGHT);
        navigationTabBar.setIsBadged(true);
        navigationTabBar.setBadgeBgColor(Color.RED);
        navigationTabBar.setBadgeTitleColor(Color.RED);
        navigationTabBar.setBadgeSize(20);

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
        nav_photo = hView.findViewById(R.id.nav_photo);

        if(!this.isDestroyed()){
            Glide.with(MainActivity.this)
                    .load(users.getImageURL().trim())
                    .into(nav_photo);
        }

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
        //MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.activity_main_menu, menu);
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete:

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
            case R.id.config:

                Intent userProfileIntent = new Intent(this, UserProfileSetupActivity.class);
                userProfileIntent.putExtra("iduser",StaticFirebaseSettings.currentUserId);
                Pair<View, String> p1 = Pair.create((View)nav_photo, "userPhoto");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this, p1);
                startActivity(userProfileIntent, options.toBundle());
                break;

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
                viewPager.setCurrentItem(0);
                drawer.closeDrawer(Gravity.LEFT);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        letTheFirstPartOfTheShowStart();
                    }
                }, 1000);
                //Toast.makeText(MainActivity.this, "Ayuda!", Toast.LENGTH_SHORT).show();
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
            case R.id.libraries:
                showLibrariesDialogFragment();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.about:
                new MaterialDialog.Builder(this)
                        .customView(R.layout.about_dialog,true)
                        .title("Acerca de GroupMeApp")
                        .titleGravity(GravityEnum.CENTER)
                        .limitIconToDefaultSize()
                        //.content("GroupMeApp para Android\n2018\n\nVersión 1.0")
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



    private void letTheFirstPartOfTheShowStart() {

        int[] oneLocation;
        float oneX = 0;
        float oneY = 0;

        // 1) TARGET PANEL DE NAVEGACIÓN
        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle(0,oneLocation[1],navigationTabBar.getWidth(),navigationTabBar.getHeight()))
                .setTitle("Panel de navegación")
                .setDescription("Aquí podrás navegar por las secciones de Noticias, Grupos, Notificaciones y Mensajes")
                .build();

        // 2) TARGET PESTAÑA NOTICIAS
        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarNewsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle(0,oneLocation[1],navigationTabBar.getWidth()/4,navigationTabBar.getHeight()))
                .setTitle("Sección de noticias")
                .setDescription("Aquí podrás visualizar todas las noticias publicadas en tus grupos")
                .build();

        // 3) TARGET CONTACTOS
        View contacts = toolbar.findViewById(R.id.contacts);
        oneLocation = new int[2];
        contacts.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + contacts.getWidth() / 2f;
        oneY = oneLocation[1] + contacts.getHeight() / 2f;
        SimpleTarget contactsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(100f))
                .setTitle("Contactos")
                .setDescription("Aqui tendrás la lista de contactos y las solicitudes pendientes")
                .build();

        // 4) TARGET AGREGAR CONTACTOS
        View addcontacts = toolbar.findViewById(R.id.add_contact);
        oneLocation = new int[2];
        addcontacts.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + addcontacts.getWidth() / 2f;
        oneY = oneLocation[1] + addcontacts.getHeight() / 2f;
        SimpleTarget addContactsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(100f))
                .setTitle("Agregar contactos")
                .setDescription("Aqui tendrás la lista de contactos y las solicitudes pendientes")
                .build();


        // TARGET BOTON DE FILTRO EN FRAGMENT DE NOTICIAS
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        View fabFilter = fragments.get(0).getView().findViewById(R.id.fabFilter);
        oneLocation = new int[2];
        fabFilter.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + fabFilter.getWidth() / 2f;
        oneY = oneLocation[1] + fabFilter.getHeight() / 2f;
        SimpleTarget simpleTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(200f))
                .setTitle("Filtro")
                .setDescription("Aquí podrás elegir que publicaciones ver")
                .build();


        //EMPIEZA LA PRIMER PARTE DEL SHOW
        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                .setTargets(tabBarTarget,tabBarNewsTarget,contactsTarget,addContactsTarget,simpleTarget)
        .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onEnded() {
                        viewPager.setCurrentItem(1);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                letTheSecondPartOfTheShowStart();
                            }
                        }, 1000);
                    }
                })
        .start();

    }

    private void letTheSecondPartOfTheShowStart() {

        int[] oneLocation;
        float oneX = 0;
        float oneY = 0;

        // 1) TARGET PESTAÑA GRUPOS
        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarNewsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle(navigationTabBar.getWidth()/4,oneLocation[1],navigationTabBar.getWidth()/2,navigationTabBar.getHeight()))
                .setTitle("Sección de grupos")
                .setDescription("Aquí podrás visualizar todos los grupos a los que perteneces")
                .build();

        // 2) TARGET AGREGAR GRUPOS
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        View fabAddGroup = fragments.get(1).getView().findViewById(R.id.fabGroups);
        oneLocation = new int[2];
        fabAddGroup.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + fabAddGroup.getWidth() / 2f;
        oneY = oneLocation[1] + fabAddGroup.getHeight() / 2f;
        SimpleTarget addGroupTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new Circle(200f))
                .setTitle("Agregar grupos")
                .setDescription("Aquí puedes crear un nuevo grupo dandole un nombre, una foto, objetivo e invitar a tus contactos a unirse ")
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                .setTargets(tabBarNewsTarget,addGroupTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onEnded() {
                        viewPager.setCurrentItem(2);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                letTheThirdPartOfTheShowStart();
                            }
                        }, 1000);

                    }
                })
                .start();

    }

    private void letTheThirdPartOfTheShowStart() {
        int[] oneLocation;
        float oneX = 0;
        float oneY = 0;

        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarNotificationsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle(navigationTabBar.getWidth()/2,oneLocation[1],(navigationTabBar.getWidth() -  navigationTabBar.getWidth()/4),navigationTabBar.getHeight()))
                .setTitle("Sección de notificaciones")
                .setDescription("Aquí podrás visualizar todas las notificaciones que hayas recibido. Se te avisará cuando recibas alguna, no te preocupes")
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                .setTargets(tabBarNotificationsTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onEnded() {
                        viewPager.setCurrentItem(3);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                letTheForthPartOfTheShowStart();
                            }
                        }, 1000);

                    }
                })
                .start();
    }

    private void letTheForthPartOfTheShowStart() {

        int[] oneLocation;
        float oneX = 0;
        float oneY = 0;

        oneLocation = new int[2];
        navigationTabBar.getLocationInWindow(oneLocation);
        oneX = oneLocation[0] + navigationTabBar.getWidth() / 2f;
        oneY = oneLocation[1] + navigationTabBar.getHeight() / 2f;
        SimpleTarget tabBarNotificationsTarget = new SimpleTarget.Builder(this)
                .setPoint(oneX, oneY)
                .setShape(new RoundRectangle((navigationTabBar.getWidth()/2 + navigationTabBar.getWidth()/4),oneLocation[1],navigationTabBar.getWidth(),navigationTabBar.getHeight()))
                .setTitle("Sección de mensajes")
                .setDescription("Aquí podrás ver todos los mensajes que has enviado a tus contactos de forma individual")
                .build();

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                //Agrego los targets
                .setTargets(tabBarNotificationsTarget)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onEnded() {
                    }
                })
                .start();

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

    private void showLibrariesDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        LibrariesDialogFragment newFragment = new LibrariesDialogFragment();
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    @Override
    public void onNewNotificationSet(int notificationQuantity) {
        NavigationTabBar.Model model = models.get(2);
        if(notificationQuantity > 0){
            model.showBadge();
            model.setBadgeTitle(String.valueOf(notificationQuantity));
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
    public void onNewMessage(int messageQuantity) {
        NavigationTabBar.Model model = models.get(3);
        if(messageQuantity > 0){
            model.showBadge();
            model.setBadgeTitle(String.valueOf(messageQuantity));
        }else{
            model.hideBadge();
        }
    }
}
