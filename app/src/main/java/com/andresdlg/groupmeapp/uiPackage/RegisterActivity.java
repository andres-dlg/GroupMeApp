package com.andresdlg.groupmeapp.uiPackage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.andresdlg.groupmeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //DECLARE FIELDS
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    //FIREBASE AUTHENTICATION ID
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    //PROGRESS DIALOG
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mSignUpButton = (Button) findViewById(R.id.create_account_button);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle(R.string.create_account);
                mProgressDialog.setMessage("Espere mientras se crea su cuenta");
                mProgressDialog.show();
                createUserAccount();
            }
        });

        //PROGRESS DIALOG INSTANCE
        mProgressDialog = new ProgressDialog(this);


        //FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //CHECK USER
                FirebaseUser user = firebaseAuth.getCurrentUser();
                /*if(user!=null){

                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    intent.putExtra("isLoggedIn", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }*/
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    //LOGIC TO CREATE USER ACCOUNT
    private void createUserAccount() {
        String emailUser, passUser;
        emailUser = mEmailView.getText().toString().trim();
        passUser = mPasswordView.getText().toString().trim();
        View focusView = null;
        boolean cancel = false;

        if (!TextUtils.isEmpty(passUser) && !isPasswordValid(passUser)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(emailUser)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(emailUser)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else {
            mAuth.createUserWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, R.string.create_account_success, Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("isLoggedIn", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        FirebaseAuthException e = (FirebaseAuthException) task.getException();
                        Toast.makeText(RegisterActivity.this, R.string.create_account_failed, Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", "Failed Registration", e);
                        mProgressDialog.dismiss();
                    }
                }
            });
        }

        if (cancel) {
            focusView.requestFocus();
        }
    }

    private boolean isEmailValid(String email) {
        boolean response = false;
        if (email.contains("@") && email.contains(".com")) {
            response = true;
        }
        return response;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
