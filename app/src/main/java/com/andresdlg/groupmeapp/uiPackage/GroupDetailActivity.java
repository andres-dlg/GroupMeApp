package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.Adapters.RVGroupDetailAdapter;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.Helper;
import com.andresdlg.groupmeapp.Utils.PhotoFullPopupWindow;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 16/03/18.
 */

public class GroupDetailActivity extends AppCompatActivity {

    final int REQUEST_CODE = 200;

    String groupKey;
    DatabaseReference groupRef;
    DatabaseReference usersRef;
    List<Users> usersList;
    Map<String, String> usersRoles;
    RVGroupDetailAdapter adapter;
    Map<String, String> members;

    EditText objetive;
    ImageView iv;
    Uri mCropImageUri;
    Uri imageHoldUri;

    private boolean editMode;

    CollapsingToolbarLayout collapsingToolbar;
    AppBarLayout appBarLayout;
    Toolbar toolbar;
    FloatingActionButton fab;
    ImageButton editObjetiveBtn;
    ImageButton addContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_group_details);

        final Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fadein);

        appBarLayout = findViewById(R.id.appBarLayout);

        ViewCompat.setTransitionName(appBarLayout,"transition");

        usersList = new ArrayList<>();
        usersRoles = new HashMap<>();

        groupKey = getIntent().getStringExtra("groupKey");
        //final String groupName = getIntent().getStringExtra("groupName");
        final String groupPhotoUrl = getIntent().getStringExtra("groupPhotoUrl");

        setToolbar(((FireApp) getApplication()).getGroupName(),myFadeInAnimation);

        editObjetiveBtn = findViewById(R.id.editObjetiveBtn);
        editObjetiveBtn.startAnimation(myFadeInAnimation);

        objetive = findViewById(R.id.objetive);

        iv = findViewById(R.id.add_group_photo);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoFullPopupWindow(GroupDetailActivity.this, R.layout.popup_photo_full, iv, groupPhotoUrl, null);
            }
        });

        addContact = findViewById(R.id.addContact);
        addContact.startAnimation(myFadeInAnimation);

        fab = findViewById(R.id.fab);

        final RecyclerView rv = findViewById(R.id.rvMembers);
        //rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        adapter = new RVGroupDetailAdapter(usersList,usersRoles,groupKey,GroupDetailActivity.this);
        rv.setAdapter(adapter);

        supportPostponeEnterTransition();
        RequestOptions requestOptions = new RequestOptions().dontAnimate();

        Glide.with(this)
                .load(groupPhotoUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(iv);

        groupRef = FirebaseDatabase.getInstance().getReference("Groups");
        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.child("groupKey").getValue().toString().equals(groupKey)){
                    Group g = dataSnapshot.getValue(Group.class);

                    getMembers(g);

                    //adapter = new RVGroupDetailAdapter(usersList,usersRoles,groupKey,GroupDetailActivity.this);
                    //rv.setAdapter(adapter);

                    if(TextUtils.isEmpty(g.getObjetive())){
                        objetive.setText("Sin objetivo");
                    }else{
                        objetive.setText(g.getObjetive());
                    }

                    fab.startAnimation(myFadeInAnimation);
                    fab.bringToFront();
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(amIadmin()){
                                onSelectImageClick();
                            }else{
                                Toast.makeText(GroupDetailActivity.this, "Debes ser administrador para actualizar la foto del grupo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    if(amIadmin()){

                        //appBarLayout.setExpanded(true);

                        editObjetiveBtn.setVisibility(View.VISIBLE);
                        editObjetiveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(!editMode){

                                    Drawable i = getResources().getDrawable(R.drawable.check);
                                    i.setTint(getResources().getColor(R.color.green_file_download));
                                    editObjetiveBtn.setImageDrawable(i);

                                    editMode = true;
                                    objetive.setEnabled(true);
                                    objetive.setSelection(objetive.getText().length());
                                    objetive.requestFocus();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(objetive, InputMethodManager.SHOW_IMPLICIT);
                                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                }
                                else{
                                    Drawable i = getResources().getDrawable(R.drawable.pen);
                                    i.setTint(getResources().getColor(R.color.mdtp_light_gray));
                                    editObjetiveBtn.setImageDrawable(i);

                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(objetive.getWindowToken(),0);

                                    editMode = false;
                                    objetive.setEnabled(false);
                                    saveObjetive();
                                }
                            }
                        });

                        addContact.setVisibility(View.VISIBLE);
                        addContact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(GroupDetailActivity.this, SearchContactActivity.class);
                                i.putExtra("groupKey",groupKey);
                                startActivity(i);
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.child("groupKey").getValue().toString().equals(groupKey)){
                    if(Helper.flag){
                        Map<String, String> members = (Map<String, String>) dataSnapshot.child("members").getValue();
                        if(members.get(StaticFirebaseSettings.currentUserId) == null){
                            Toast.makeText(GroupDetailActivity.this, "Fuiste eliminado de este grupo", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GroupDetailActivity.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            usersList.clear();
                            usersRoles.clear();
                            Group g = dataSnapshot.getValue(Group.class);
                            getMembers(g);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setToolbar(String groupName, Animation myFadeInAnimation) {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setBackground(getResources().getDrawable(R.drawable.gradient_black_rotated));
        toolbar.startAnimation(myFadeInAnimation);

        collapsingToolbar = findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbar.setTitle(groupName);
        //collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_group_detail_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.editGroupNameBtn:
                if(amIadmin()){
                    new MaterialDialog.Builder(GroupDetailActivity.this)
                            .title("Nombre del grupo")
                            .content("Nombre")
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("Ingrese el nombre", ((FireApp) getApplication()).getGroupName(), new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if(!TextUtils.isEmpty(input)){
                                        saveGroupName(input.toString());
                                    }else{
                                        dialog.getInputEditText().setError("Este campo es necesario");
                                    }
                                }
                            })
                            .inputRange(0,20,getResources().getColor(android.R.color.holo_red_dark))
                            .show();
                }else{
                    Toast.makeText(this, "Debes ser administrador para actualizar el nombre del grupo", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.help:
                appBarLayout.setExpanded(true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        letTheFirstPartOfTheShowBegin();
                    }
                }, 1000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //NEW IMAGE SELECTION
    private void onSelectImageClick() {
        Intent i = CropImage.getPickImageChooserIntent(this);
        startActivityForResult(i,REQUEST_CODE);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(15,9)
                .setFixAspectRatio(true)
                //.setMaxCropResultSize(480,270)
                .setRequestedSize(1080,607, CropImageView.RequestSizeOptions.RESIZE_FIT)
                .start(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageHoldUri = result.getUri();
                saveGroupPhoto(imageHoldUri,iv);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error;
                error = result.getError();
                Log.e("CROP IMAGE ERROR",error.getMessage());
            }
        }
    }

    private void saveGroupPhoto(final Uri imageHoldUri, final ImageView iv) {

        //Recupero las referencias
        final StorageReference groupStorageRef = FirebaseStorage.getInstance().getReference("Groups").child(groupKey);
        final DatabaseReference groupImageRef = groupRef.child("imageUrl");

        //Obtengo la url dela imagen que esta en firebase storage
        groupImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Obtengo la url dela imagen que esta en firebase storage
                String imageUrlFirebase = dataSnapshot.getValue().toString();
                StorageReference groupImageStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrlFirebase);
                //borro la imagen existente
                groupImageStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("FIREBASE STORAGE","¡Imagen borrada en el Storage!");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        StorageReference newFileRef = groupStorageRef.child(imageHoldUri.getLastPathSegment());
        newFileRef.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                groupImageRef.setValue(downloadUrl);
                Glide.with(GroupDetailActivity.this)
                        .load(imageHoldUri)
                        .into(iv);
                Toast.makeText(GroupDetailActivity.this, "¡Imagen actualizada en el Storage!", Toast.LENGTH_SHORT).show();
                ((FireApp) getApplication()).setGroupPhoto(downloadUrl);
            }
        });
    }

    private void saveGroupName(final String newName) {
        groupRef.child("name").setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                collapsingToolbar.setTitle(newName);

                ((FireApp) getApplication()).setGroupName(newName);
                Toast.makeText(GroupDetailActivity.this, "¡Nombre del grupo actualizado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupDetailActivity.this, "Error al actualizar nombre del grupo", Toast.LENGTH_SHORT).show();
            }
        });

        groupRef.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    data.child("groupName").getRef().setValue(newName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean amIadmin() {
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            String memberId = entry.getKey();
            String memberRol = entry.getValue();
            if(memberId.equals(StaticFirebaseSettings.currentUserId) && memberRol.equals(Roles.ADMIN.toString())){
                return true;
            }
        }
        return false;
    }

    private void saveObjetive() {
        groupRef.child("objetive").setValue(objetive.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupDetailActivity.this, "¡Objetivo actualizado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupDetailActivity.this, "Error al actualizar objetivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMembers(final Group g) {
        usersRoles = g.getMembers();
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            String memberId = entry.getKey();
            //usersRoles = g.getMembers();
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(memberId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    filterUsers(u);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void filterUsers(final Users u) {
        DatabaseReference statusRef = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Users")
                                        .child(u.getUserid())
                                        .child("groups")
                                        .child(groupKey)
                                        .child("status");
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    if(dataSnapshot.getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                        //usersList.add(u);
                        updateUsers(u);
                        //adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUsers(Users user) {
        boolean exists = false;
        adapter.setRoles(usersRoles);
        //appBarLayout.setExpanded(false);
        for(int i=0; i < usersList.size(); i++){
            if(usersList.get(i).getUserid().equals(user.getUserid())){
                exists = true;
                usersList.remove(i);
                usersList.add(i,user);
                adapter.notifyItemChanged(i);
            }
        }
        if(!exists){
            usersList.add(user);
            adapter.notifyDataSetChanged();
        }
    }

    private void letTheFirstPartOfTheShowBegin() {

        int[] oneLocation;
        float oneX;
        float oneY;

        SimpleTarget mainTarget = new SimpleTarget.Builder(this)
                .setShape(new Circle(0f))
                .setTitle("Detalles del grupo")
                .setDescription("En esta pantalla podrás ver la foto, el objetivo y los miembros del grupo junto con el rol de cada uno")
                .build();

        if(amIadmin()){
            oneLocation = new int[2];
            View edit = toolbar.findViewById(R.id.editGroupNameBtn);
            edit.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + edit.getWidth() / 2f;
            oneY = oneLocation[1] + edit.getHeight() / 2f;
            SimpleTarget editTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Edición de nombre")
                    .setDescription("Desde aquí podrás editar el nombre del grupo")
                    .build();

            oneLocation = new int[2];
            fab.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + fab.getWidth() / 2f;
            oneY = oneLocation[1] + fab.getHeight() / 2f;
            SimpleTarget fabTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Edición de foto")
                    .setDescription("Desde aquí podrás editar la foto del grupo")
                    .build();

            oneLocation = new int[2];
            editObjetiveBtn.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + editObjetiveBtn.getWidth() / 2f;
            oneY = oneLocation[1] + editObjetiveBtn.getHeight() / 2f;
            SimpleTarget editObjetiveBtnTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Edición de objetivo")
                    .setDescription("El objetivo de un grupo puede cambiar segun las necesidades\n Desde aquí podrás editar el objetivo")
                    .build();

            oneLocation = new int[2];
            addContact.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + addContact.getWidth() / 2f;
            oneY = oneLocation[1] + addContact.getHeight() / 2f;
            SimpleTarget addContactTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Agregar miembros")
                    .setDescription("Desde aquí podrás invitar a tus contactos a unirse al grupo")
                    .build();

            Spotlight.with(this)
                    .setOverlayColor(R.color.background)
                    .setDuration(1000L)
                    .setAnimation(new DecelerateInterpolator(2f))
                    //Agrego los targets
                    .setTargets(mainTarget,editTarget,fabTarget,editObjetiveBtnTarget,addContactTarget)
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

        if(!amIadmin()){
            Spotlight.with(this)
                    .setOverlayColor(R.color.background)
                    .setDuration(1000L)
                    .setAnimation(new DecelerateInterpolator(2f))
                    //Agrego los targets
                    .setTargets(mainTarget)
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
    }
}
