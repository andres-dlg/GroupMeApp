package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.PhotoFullPopupWindow;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileSetupActivity extends AppCompatActivity {

    //FIELDS DECLARATION
    CircleImageView mBack;
    CircleImageView mCircleImageView;
    AutoCompleteTextView mAlias;
    EditText mName;
    AutoCompleteTextView mJob;
    Button mSaveButton;
    TextView mLater;
    Uri mCropImageUri;
    TextInputLayout mTextInputAlias;
    TextInputLayout mTextInputJob;
    LinearLayout mMetricsLinearLayout;
    TextView mGroupQuantity;
    TextView mSubGroupQuantity;

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUserDatabase;
    StorageReference mStorageReference;

    //FIREBASE STORAGE FIELDS
    StorageReference mChildStorage;

    //private static final int REQUEST_CAMERA = 3;
    //private static final int SELECT_FILE = 2;

    //IMAGE HOLD URI
    Uri imageHoldUri;

    //PROGRESS DIALOG
    ProgressDialog mProgress;

    boolean pass;
    boolean exists = false;
    boolean imageSetted = false;
    boolean yaPasoPorAca = false;

    String iduser;
    Users u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        iduser = getIntent().getStringExtra("iduser");

        //ASSIGN ID'S
        mCircleImageView = findViewById(R.id.user_profile_photo);
        if(iduser!=null){

            mBack = findViewById(R.id.back);
            mBack.setVisibility(View.VISIBLE);
            mBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        mTextInputAlias = findViewById(R.id.til);

        mAlias = findViewById(R.id.alias);

        mName =  findViewById(R.id.user_profile_name);

        mTextInputJob = findViewById(R.id.til2);

        mJob =  findViewById(R.id.job);

        mMetricsLinearLayout = findViewById(R.id.metricsLlo);

        mGroupQuantity = findViewById(R.id.groupQuantityNumber);

        mSubGroupQuantity = findViewById(R.id.subgroupQuantityNumber);

        mSaveButton = findViewById(R.id.save);
        mLater = findViewById(R.id.later);
        mLater.setPaintFlags(mLater.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        //ASSIGN INSTANCE TO FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();

        //PROGRESS DIALOG
        mProgress = new ProgressDialog(this);

        //ASSIGN INSTANCE TO FIREBASE DATABASE
        if(iduser==null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }else {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(iduser);
        }

        mStorageReference = FirebaseStorage.getInstance().getReference();

        //ONCLICK LISTENER PROFILE SAVE BUTTON
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
            }
        });

        mLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserProfile();
            }
        });

        if(iduser != null){

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            if(!iduser.equals(StaticFirebaseSettings.currentUserId)){
                mSaveButton.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                mTextInputAlias.setEnabled(false);
                mTextInputJob.setEnabled(false);
                mJob.setEnabled(false);
                mName.setEnabled(false);
            }
            setUserData();
        }else{
            mMetricsLinearLayout.setVisibility(View.GONE);
        }

    }

    private void setUserData() {

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int groupQuantity = (int)dataSnapshot.child("groups").getChildrenCount();
                mGroupQuantity.setText(String.valueOf(groupQuantity));

                int subgroupQuantity = 0;
                for(DataSnapshot data : dataSnapshot.child("groups").getChildren()){
                    subgroupQuantity += (int)data.child("subgroups").getChildrenCount();
                }
                mSubGroupQuantity.setText(String.valueOf(subgroupQuantity));


                u = dataSnapshot.getValue(Users.class);
                mName.setText(u.getName());
                mAlias.setText(u.getAlias());
                mJob.setText(u.getJob());
                supportPostponeEnterTransition();
                RequestOptions requestOptions = new RequestOptions().dontAnimate();
                Glide.with(UserProfileSetupActivity.this)
                        .load(u.getImageURL())
                        .apply(requestOptions)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                //supportStartPostponedEnterTransition();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                //supportStartPostponedEnterTransition();
                                return false;
                            }
                        })
                        .into(mCircleImageView);

                mCircleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new PhotoFullPopupWindow(UserProfileSetupActivity.this, R.layout.popup_photo_full, mCircleImageView, u.getImageURL(), null);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            if (resultCode == RESULT_OK) {
                mCircleImageView.setPadding(10,10,10,10);
                imageHoldUri = result.getUri();
                mCircleImageView.setImageURI(imageHoldUri);
                imageSetted = true;
            } /*else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }*/
        }
    }

    private void saveUserProfile() {
        final String alias, userName, job;
        alias = mAlias.getText().toString().trim();
        userName = mName.getText().toString().trim();
        job = mJob.getText().toString().trim();

        pass = false;
        View focusView;

        if (TextUtils.isEmpty(alias)) {
            mAlias.setError("Este campo es necesario");
            focusView = mAlias;
            focusView.requestFocus();
            pass = true;
        }if (TextUtils.isEmpty(userName)) {
            mName.setError("Este campo es necesario");
            focusView = mName;
            focusView.requestFocus();
            pass = true;
        }else if(!isAliasValid(alias)){
            mAlias.setError("Sin espacios ni caracteres especiales");
            focusView = mAlias;
            focusView.requestFocus();
            pass = true;
        }

        if(!pass){
            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
            usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    yaPasoPorAca = true;
                    exists = false;
                    if(iduser == null){
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            if(data.child("alias").exists()){
                                if (data.child("alias").getValue().equals(alias)) {
                                    exists = true;
                                    break;
                                }
                            }
                        }
                    }else{
                        if(!u.getAlias().equals(alias)){
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                if(data.child("alias").exists()){
                                    if (data.child("alias").getValue().equals(alias)) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if(!exists){
                        mProgress.setTitle(getString(R.string.progress_save_profile_title));
                        mProgress.setMessage(getString(R.string.progress_save_profile_message));
                        mProgress.show();

                        if(imageSetted){

                            if(iduser == null){
                                mProgress.setMessage("Subiendo foto de perfil");
                            }else{
                                mProgress.setMessage("Actualizando perfil");
                            }
                            mProgress.show();

                            mChildStorage = mStorageReference.child("User_Profile").child(mAuth.getCurrentUser().getUid()).child(imageHoldUri.getLastPathSegment());
                            mChildStorage.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imageHoldUri = taskSnapshot.getDownloadUrl();
                                    mProgress.dismiss();
                                    createUserData(alias,userName,job);
                                }
                            });
                        }else{
                            if(iduser == null){
                                imageHoldUri = Uri.parse("android.resource://com.andresdlg.groupmeapp/"+R.drawable.new_user);
                            }else{
                                imageHoldUri = Uri.parse(u.getImageURL());
                            }
                            mProgress.dismiss();
                            createUserData(alias,userName,job);
                        }
                    }else{
                        View focusView;
                        mAlias.setError("Alias en uso");
                        focusView = mAlias;
                        focusView.requestFocus();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void createUserData(String alias,String userName,String job) {
        mUserDatabase.child("alias").setValue(alias);
        mUserDatabase.child("name").setValue(userName);
        mUserDatabase.child("job").setValue(job);
        mUserDatabase.child("userid").setValue(mAuth.getCurrentUser().getUid());
        mUserDatabase.child("imageUrl").setValue(imageHoldUri.toString());

        //ESTO LO HAGO PORQUE SI VENGO DE EDITAR MI PERFIL (ELSE) ME DA ERROR EL CONTEXT DE GLIDE
        if(iduser == null){
            Intent intent = new Intent(UserProfileSetupActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            onBackPressed();
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .start(this);
    }

    private boolean isAliasValid(String alias) {
        return alias.matches("[A-Za-z]*");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgress.dismiss();
    }
}
