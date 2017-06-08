package com.andresdlg.groupmeapp.uiPackage;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andresdlg.groupmeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileSetupActivity extends AppCompatActivity {

    //FIELDS DECLARATION
    CircleImageView mCircleImageView;
    EditText mAlias;
    EditText mStatus;
    Button mSaveButton;

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUserDatabase;

    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;

    //IMAGE HOLD URI
    Uri imageHoldUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        //ASSIGN ID'S
        mCircleImageView = (CircleImageView) findViewById(R.id.profile_image);
        mAlias = (EditText) findViewById(R.id.alias);
        mStatus = (EditText) findViewById(R.id.status);
        mSaveButton = (Button) findViewById(R.id.save);

        //ASSIGN INSTANCE TO FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //LOGIC TO CHECK USER
                FirebaseUser user = firebaseAuth.getCurrentUser();
                /*if(user!=null){
                    finish();
                    Intent moveToHome = new Intent(UserProfileSetupActivity.this,MainActivity.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(moveToHome);
                }*/
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);

        //ASSIGN INSTANCE TO FIREBASE DATABASE
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //ONCLICK LISTENER PROFILE SAVE BUTTON
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIC FOR SAVING USER PROFILE
            }
        });

        //ONCLICK LISTENER PROFILE IMAGE
        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIC FOR PICKING IMAGE
                pickTheProfilePicture();
            }
        });
    }

    private void pickTheProfilePicture() {

        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileSetupActivity.this);
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        //CHOOSE CAMERA
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //SAVE URI FROM GALLERY
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }else if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ){
            //SAVE URI FROM CAMERA
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageHoldUri = result.getUri();
                mCircleImageView.setImageURI(imageHoldUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
