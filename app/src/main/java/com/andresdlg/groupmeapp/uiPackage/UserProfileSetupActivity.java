package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.R;
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
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileSetupActivity extends AppCompatActivity {

    //FIELDS DECLARATION
    CircleImageView mCircleImageView;
    AutoCompleteTextView mAlias;
    EditText mName;
    AutoCompleteTextView mJob;
    Button mSaveButton;
    TextView mLater;
    Uri mCropImageUri;

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUserDatabase;
    StorageReference mStorageReference;

    //FIREBASE STORAGE FIELDS
    StorageReference mChildStorage;

    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;

    //IMAGE HOLD URI
    Uri imageHoldUri;// = Uri.parse("android.resource://" + "com.andresdlg.groupmeapp" +"/"+R.drawable.new_user);

    //PROGRESS DIALOG
    ProgressDialog mProgress;

    boolean pass;
    boolean exists = false;
    boolean imageSetted = false;
    boolean yaPasoPorAca = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        //ASSIGN ID'S
        mCircleImageView = findViewById(R.id.user_profile_photo);

        mAlias = findViewById(R.id.alias);

        mName =  findViewById(R.id.user_profile_name);

        mJob =  findViewById(R.id.job);

        mSaveButton = findViewById(R.id.save);
        mLater = findViewById(R.id.later);
        mLater.setPaintFlags(mLater.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        //ASSIGN INSTANCE TO FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();

        //PROGRESS DIALOG
        mProgress = new ProgressDialog(this);

        //ASSIGN INSTANCE TO FIREBASE DATABASE
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mStorageReference = FirebaseStorage.getInstance().getReference();

        //ONCLICK LISTENER PROFILE SAVE BUTTON
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        /*mStorageReference.child("new_user.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageHoldUri = uri;
            }
        });*/

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
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
            mAlias.setError("Este campo es necesario");
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
            usersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    yaPasoPorAca = true;
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.child("alias").exists()){
                            if (data.child("alias").getValue().equals(alias)) {
                                exists = true;
                                break;
                            }
                        }
                    }
                    if(!exists){
                        mProgress.setTitle(getString(R.string.progress_save_profile_title));
                        mProgress.setMessage(getString(R.string.progress_save_profile_message));
                        mProgress.show();

                        if(imageSetted){
                            mProgress.setMessage("Subiendo foto de perfil");
                            mProgress.show();

                            mChildStorage = mStorageReference.child("User_Profile").child(mAuth.getUid()).child(imageHoldUri.getLastPathSegment());
                            mChildStorage.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imageHoldUri = taskSnapshot.getDownloadUrl();
                                    mProgress.dismiss();
                                    createUserData(alias,userName,job);
                                }
                            });
                        }else{
                            mStorageReference.child("new_user.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageHoldUri = uri;
                                    mProgress.dismiss();
                                    createUserData(alias,userName,job);
                                }
                            });

                        }
                    }else{
                        if(!yaPasoPorAca){
                            View focusView;
                            mAlias.setError("Alias en uso");
                            focusView = mAlias;
                            focusView.requestFocus();
                        }
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

        Intent intent = new Intent(UserProfileSetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
