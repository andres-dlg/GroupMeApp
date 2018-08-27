package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.Adapters.RVSubGroupDetailAdapter;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by andresdlg on 16/03/18.
 */

public class SubGroupDetailActivity extends AppCompatActivity {

    final int REQUEST_CODE = 200;

    String groupKey;
    String subGroupKey;
    String subGroupName;
    DatabaseReference subGroupRef;
    DatabaseReference usersRef;
    List<Users> usersList;
    Map<String, String> usersRoles;
    RVSubGroupDetailAdapter adapter;
    Map<String, String> members;

    EditText objetive;
    ImageView iv;
    Uri mCropImageUri;
    Uri imageHoldUri;
    FloatingActionButton fab;
    ImageButton editObjetiveBtn;
    ImageButton addContact;

    private boolean editMode;

    CollapsingToolbarLayout collapsingToolbar;
    AppBarLayout appBarLayout;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_group_subgroup_details);

        final Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fadein);

        appBarLayout = findViewById(R.id.appBarLayout);

        ViewCompat.setTransitionName(appBarLayout,"transition");

        usersList = new ArrayList<>();
        usersRoles = new HashMap<>();

        groupKey = getIntent().getStringExtra("groupKey");
        subGroupKey = getIntent().getStringExtra("subGroupKey");
        subGroupName = getIntent().getStringExtra("subGroupName");
        String groupName = ((FireApp) this.getApplication()).getGroupName();
        final String subGroupPhotoUrl = getIntent().getStringExtra("subGroupPhotoUrl");

        setToolbar(subGroupName,myFadeInAnimation);

        editObjetiveBtn = findViewById(R.id.editObjetiveBtn);
        editObjetiveBtn.startAnimation(myFadeInAnimation);

        objetive = findViewById(R.id.objetive);

        iv = findViewById(R.id.add_group_photo);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoFullPopupWindow(SubGroupDetailActivity.this, R.layout.popup_photo_full, iv, subGroupPhotoUrl, null);
            }
        });

        addContact = findViewById(R.id.addContact);
        addContact.startAnimation(myFadeInAnimation);

        fab = findViewById(R.id.fab);

        TextView groupNameTv = findViewById(R.id.groupNameTv);
        groupNameTv.setText(groupName);

        final RecyclerView rv = findViewById(R.id.rvMembers);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        supportPostponeEnterTransition();
        RequestOptions requestOptions = new RequestOptions().dontAnimate();
        Glide.with(this)
                .load(subGroupPhotoUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        fab.show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(iv);

        subGroupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);
        subGroupRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot data) {
                //SI ES NULL ES PORQUE FUE ELIMINADO
                if(data.child("members").getValue()!=null) {
                    Map<String, String> members = (Map<String, String>) data.child("members").getValue();
                    //EN CASO DE QUE ENTRE ACA SI FUE ELIMINADO EL USUARIO
                    if(members.get(StaticFirebaseSettings.currentUserId) == null){
                        Toast.makeText(SubGroupDetailActivity.this, "Fuiste eliminado de este subgrupo", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        finish();
                    }else{
                        final SubGroup sgf = new SubGroup(data.child("name").getValue().toString(),null,null,null,null);
                        sgf.setName(data.child("name").getValue().toString());
                        sgf.setImageUrl(data.child("imageUrl").getValue().toString());
                        sgf.setMembers((Map<String,String>) data.child("members").getValue());
                        sgf.setSubGroupKey(data.child("subGroupKey").getValue().toString());
                        if(data.child("objetive").getValue() == null){
                            sgf.setObjetive(null);
                        }else{
                            sgf.setObjetive(data.child("objetive").getValue().toString());
                        }
                        List<Task> tasks = new ArrayList();
                        for(DataSnapshot d : data.child("tasks").getChildren()){
                            Task task = d.getValue(Task.class);
                            tasks.add(task);
                        }
                        sgf.setTasks(tasks);

                        getMembers(sgf);

                        adapter = new RVSubGroupDetailAdapter(usersList,usersRoles,groupKey,subGroupKey,SubGroupDetailActivity.this);
                        rv.setAdapter(adapter);

                        if(TextUtils.isEmpty(sgf.getObjetive())){
                            objetive.setText("Sin objetivo");
                        }else{
                            objetive.setText(sgf.getObjetive());
                        }

                        //fab.startAnimation(myFadeInAnimation);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(amIadmin()){
                                    onSelectImageClick();
                                }else{
                                    Toast.makeText(SubGroupDetailActivity.this, "Debes ser administrador para actualizar la foto del grupo", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        if(amIadmin()){

                            //appBarLayout.setExpanded(true);

                            ((FireApp) getApplication()).setMembers(sgf.getMembers());

                        /*fab.setVisibility(View.VISIBLE);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onSelectImageClick(view);
                            }
                        });*/

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
                                    Intent i = new Intent(SubGroupDetailActivity.this, SearchContactActivity.class);
                                    i.putExtra("groupKey",groupKey);
                                    i.putExtra("subGroupKey",subGroupKey);
                                    i.putExtra("subGroupName",subGroupName);
                                    startActivity(i);
                                }
                            });
                        }
                    }

                }
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
        getMenuInflater().inflate(R.menu.activity_group_subgroup_detail_menu,menu);
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
                    new MaterialDialog.Builder(SubGroupDetailActivity.this)
                            .title("Nombre del subgrupo")
                            .content("Nombre")
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("Ingrese el nombre", subGroupName, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if(!TextUtils.isEmpty(input)){
                                        saveSubGroupName(input.toString());
                                    }else{
                                        dialog.getInputEditText().setError("Este campo es necesario");
                                    }
                                }
                            })
                            .inputRange(0,20,getResources().getColor(android.R.color.holo_red_dark))
                            .show();
                }else{
                    Toast.makeText(this, "Debes ser administrador para actualizar el nombre del subgrupo", Toast.LENGTH_SHORT).show();
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
                .setAspectRatio(16,9)
                .setFixAspectRatio(true)
                //.setMaxCropResultSize(480,270)
                //.setRequestedSize(1080,607, CropImageView.RequestSizeOptions.RESIZE_FIT)
                .setRequestedSize(1280,720, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
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
                Log.e("ERROR CROP IMAGE",error.getMessage());
            }
        }
    }

    private void saveGroupPhoto(final Uri imageHoldUri, final ImageView iv) {

        //Recupero las referencias
        final StorageReference groupStorageRef = FirebaseStorage.getInstance().getReference("Groups").child(groupKey).child(subGroupKey);
        final DatabaseReference groupImageRef = subGroupRef.child("imageUrl");

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

                Glide.with(SubGroupDetailActivity.this)
                        .load(imageHoldUri)
                        .into(iv);

                //Toast.makeText(SubGroupDetailActivity.this, "¡Imagen actualizada en el Storage!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveSubGroupName(final String newName) {
        subGroupRef.child("name").setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                collapsingToolbar.setTitle(newName);
                //tv.setText(newName);
                //((FireApp) getApplication()).setGroupName(newName);
                Toast.makeText(SubGroupDetailActivity.this, "¡Nombre del subgrupo actualizado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SubGroupDetailActivity.this, "Error al actualizar nombre del subgrupo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean amIadmin() {
        for(Map.Entry<String, String> entry : members.entrySet()) {
            String memberId = entry.getKey();
            String memberRol = entry.getValue();
            if(memberId.equals(StaticFirebaseSettings.currentUserId) && memberRol.equals(Roles.SUBGROUP_ADMIN.toString())){
                return true;
            }
        }
        return false;
    }

    private void saveObjetive() {
        subGroupRef.child("objetive").setValue(objetive.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SubGroupDetailActivity.this, "¡Objetivo actualizado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SubGroupDetailActivity.this, "Error al actualizar objetivo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMembers(final SubGroup g) {
        //usersList.clear();
        members = g.getMembers();
        for(Map.Entry<String, String> entry : members.entrySet()) {
            String memberId = entry.getKey();
            //String memberRol = entry.getValue();
            usersRoles = g.getMembers();
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
                //VALIDO QUE LA INVITACION AL SUBGRUPO ESTE ACEPTADA (POR DEFAULT Y POR AHORA ESTA EN ACCEPTED)
                if(dataSnapshot.getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                    boolean exists = false;
                    for(int i = 0; i<usersList.size(); i++){
                        if(usersList.get(i).getUserid().equals(u.getUserid())){
                            usersList.remove(i);
                            usersList.add(i,u);
                            adapter.notifyItemChanged(i);
                            exists = true;
                        }
                    }
                    if(!exists){
                        usersList.add(u);
                        adapter.notifyDataSetChanged();
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
        fab.hide();
        super.onBackPressed();
    }

    private void letTheFirstPartOfTheShowBegin() {

        int[] oneLocation;
        float oneX;
        float oneY;

        SimpleTarget mainTarget = new SimpleTarget.Builder(this)
                .setShape(new Circle(0f))
                .setTitle("Detalles del subgrupo")
                .setDescription("En esta pantalla podrás ver la foto, el objetivo y los miembros del subgrupo junto con el rol de cada uno")
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
                    .setDescription("Desde aquí podrás editar el nombre del subgrupo")
                    .build();

            oneLocation = new int[2];
            fab.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + fab.getWidth() / 2f;
            oneY = oneLocation[1] + fab.getHeight() / 2f;
            SimpleTarget fabTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Edición de foto")
                    .setDescription("Desde aquí podrás editar la foto del subgrupo")
                    .build();

            oneLocation = new int[2];
            editObjetiveBtn.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + editObjetiveBtn.getWidth() / 2f;
            oneY = oneLocation[1] + editObjetiveBtn.getHeight() / 2f;
            SimpleTarget editObjetiveBtnTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Edición de objetivo")
                    .setDescription("El objetivo de un subgrupo puede cambiar segun las necesidades\n Desde aquí podrás editar el objetivo")
                    .build();

            oneLocation = new int[2];
            addContact.getLocationInWindow(oneLocation);
            oneX = oneLocation[0] + addContact.getWidth() / 2f;
            oneY = oneLocation[1] + addContact.getHeight() / 2f;
            SimpleTarget addContactTarget = new SimpleTarget.Builder(this)
                    .setPoint(oneX, oneY)
                    .setShape(new Circle(100f))
                    .setTitle("Agregar miembros")
                    .setDescription("Desde aquí podrás agregar al subgrupo a los usuarios que tu quieras y que ya sean parte del grupo general")
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
