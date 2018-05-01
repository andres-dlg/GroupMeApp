package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
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

        final ImageButton editObjetiveBtn = findViewById(R.id.editObjetiveBtn);
        editObjetiveBtn.startAnimation(myFadeInAnimation);

        objetive = findViewById(R.id.objetive);
        iv = findViewById(R.id.add_group_photo);

        final ImageButton addContact = findViewById(R.id.addContact);
        addContact.startAnimation(myFadeInAnimation);

        final FloatingActionButton fab = findViewById(R.id.fab);


        final RecyclerView rv = findViewById(R.id.rvMembers);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        Glide.with(this)
                .load(groupPhotoUrl)
                .into(iv);

        groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group g = dataSnapshot.getValue(Group.class);

                getMembers(g);

                adapter = new RVGroupDetailAdapter(usersList,usersRoles,groupKey,GroupDetailActivity.this);
                rv.setAdapter(adapter);

                if(TextUtils.isEmpty(g.getObjetive())){
                    objetive.setText("Sin objetivo");
                }else{
                    objetive.setText(g.getObjetive());
                }

                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(amIadmin()){
                            onSelectImageClick(view);
                        }else{
                            Toast.makeText(GroupDetailActivity.this, "Debes ser administrador para actualizar la foto del grupo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if(amIadmin()){

                    appBarLayout.setExpanded(true);

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
                            Intent i = new Intent(GroupDetailActivity.this, SearchContactActivity.class);
                            i.putExtra("groupKey",groupKey);
                            startActivity(i);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setToolbar(String groupName, Animation myFadeInAnimation) {

        Toolbar toolbar = findViewById(R.id.toolbar);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //NEW IMAGE SELECTION
    private void onSelectImageClick(View view) {
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
                Exception error = result.getError();
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
                //tv.setText(newName);
                ((FireApp) getApplication()).setGroupName(newName);
                Toast.makeText(GroupDetailActivity.this, "¡Nombre del grupo actualizado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupDetailActivity.this, "Error al actualizar nombre del grupo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean amIadmin() {
        for(Map.Entry<String, String> entry : members.entrySet()) {
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
        members = g.getMembers();
        for(Map.Entry<String, String> entry : members.entrySet()) {
            String memberId = entry.getKey();
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
                if(dataSnapshot.getValue().toString().equals(GroupStatus.ACCEPTED.toString())){
                    usersList.add(u);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
