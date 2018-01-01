package com.andresdlg.groupmeapp.uiPackage.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    //FIREBASE AUTHENTICATION ID
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    //PROGRESS DIALOG
    ProgressDialog mProgressDialog;
    //DECLARE FIELDS
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    boolean cancel;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Background code

        /* adapt the image to the size of the display */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if(RegisterActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background_vertical),size.x,size.y,true);
            // Portrait Mode
        } else {
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background),size.x,size.y,true);
            // Landscape Mode
        }

        /* fill the background ImageView with the resized image */
        ImageView iv_background = findViewById(R.id.ivLogin);
        iv_background.setImageBitmap(bmp);


        //Textview code
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.colorAccent));
        mEmailView.setBackgroundTintList(colorStateList);
        mEmailView.setTextColor(colorStateList);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setBackgroundTintList(colorStateList);
        mPasswordView.setTextColor(colorStateList);

        mConfirmPasswordView = findViewById(R.id.confirmPassword);
        mConfirmPasswordView.setBackgroundTintList(colorStateList);
        mConfirmPasswordView.setTextColor(colorStateList);


        Button mSignUpButton = findViewById(R.id.create_account_button);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount();
                if(!cancel){
                    mProgressDialog.setTitle(R.string.create_account);
                    mProgressDialog.setMessage("Espere mientras se crea su cuenta");
                    mProgressDialog.show();
                }
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
                user = firebaseAuth.getCurrentUser();
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
        String emailUser, passUser, confirmPassUser;
        emailUser = mEmailView.getText().toString().trim();
        passUser = mPasswordView.getText().toString().trim();
        confirmPassUser = mConfirmPasswordView.getText().toString().trim();
        View focusView = null;
        cancel = false;

        if (TextUtils.isEmpty(emailUser)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(emailUser)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(passUser)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(passUser) && !TextUtils.isEmpty(passUser)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            Toast.makeText(RegisterActivity.this, "La constraseña debe tener 6 caracteres como mínimo",Toast.LENGTH_SHORT).show();
            cancel = true;
        }
        else if (TextUtils.isEmpty(confirmPassUser)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        else if (!isConfirmPasswordValid(passUser,confirmPassUser) && !TextUtils.isEmpty(confirmPassUser)) {
            mConfirmPasswordView.setError(getString(R.string.error_incorrect_confirm_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(emailUser, passUser)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    getString(R.string.verification_email_sent) + user.getEmail(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    R.string.verification_email_failed,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                mProgressDialog.dismiss();
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
    }

    private boolean isConfirmPasswordValid(String passUser, String confirmPassUser) {
        return Objects.equals(passUser, confirmPassUser);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        boolean response = false;
        if (email.contains("@") && email.contains(".com")) {
            response = true;
        }
        return response;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}
