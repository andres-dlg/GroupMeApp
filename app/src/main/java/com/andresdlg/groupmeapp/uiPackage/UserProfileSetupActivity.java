package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andresdlg.groupmeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                if(user!=null){
                    finish();
                    Intent moveToHome = new Intent(UserProfileSetupActivity.this,MainActivity.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(moveToHome);
                }
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
    }
}
