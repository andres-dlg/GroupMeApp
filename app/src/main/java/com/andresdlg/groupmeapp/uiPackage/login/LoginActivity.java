package com.andresdlg.groupmeapp.uiPackage.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Manifest;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // STATICS
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final String TAG = "ADLG";
    private static final int RC_SIGN_IN = 9001;

    // FIREBASE ATHENTICATION FIELDS
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    // UI REFERENCES.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Bitmap bmp;
    ProgressBar mProgressBar;

    boolean logout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null){

            logout = true;

        }

        // BACKGROUND AUTO ADAPTABLE
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if(LoginActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            // PORTRAIT MODE
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background_vertical),size.x,size.y,true);
        } else {
            // LANDSCAPE MODE
            bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                    getResources(),R.drawable.login_background),size.x,size.y,true);
        }
        ImageView iv_background = findViewById(R.id.ivLogin);
        iv_background.setImageBitmap(bmp);


        //IF ALL PERMISSIONS GRANTED...
        if(checkAndRequestPermissions()) {
            mProgressBar = findViewById(R.id.progressBar);

            // SET UP THE LOGIN FORM WITH COLORS.
            mEmailView = findViewById(R.id.email);
            ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.colorAccent));
            mEmailView.setBackgroundTintList(colorStateList);
            mEmailView.setTextColor(colorStateList);

            mPasswordView = findViewById(R.id.password);
            mPasswordView.setBackgroundTintList(colorStateList);
            mPasswordView.setTextColor(colorStateList);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            Button mGoogleSignInButton = findViewById(R.id.google_sign_in_button);
            mGoogleSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.google_sign_in_button:
                            signInWithGoogle();
                            break;
                    }
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            //mProgressView = findViewById(R.id.login_progress);

            TextView mSignUpLink = findViewById(R.id.sign_up_link);
            mSignUpLink.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isConnected()){
                        Toast.makeText(LoginActivity.this,"No tiene conexion a internet",Toast.LENGTH_SHORT).show();
                    }else{
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    }
                }
            });

            TextView mForgottenPasswordLink = findViewById(R.id.forgotten_password_link);
            mForgottenPasswordLink.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isConnected()){
                        Toast.makeText(LoginActivity.this,"No tiene conexion a internet",Toast.LENGTH_SHORT).show();
                    }else{
                        showInputDialog();
                    }
                }
            });

            //FIREBASE AUTHENTICATION INSTANCE
            mAuth = FirebaseAuth.getInstance();

            // [START config_signin]
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END config_signin]
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private boolean checkAndRequestPermissions() {

        //Es marshmallow o + ?
        if(Build.VERSION.SDK_INT >= 23) {
            int contactsPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
            int internetPermission = ContextCompat.checkSelfPermission(this, CAMERA);
            List<String> listPermissionsNeeded = new ArrayList<>();

            //ya se concedio el permiso de contactos?
            //Si no se concedio lo agrego a la lista de los permisos necesarios
            if (contactsPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(READ_CONTACTS);
            }

            //ya se concedio el permiso de internet?
            //Si no se concedio lo agrego a la lista de los permisos necesarios
            if (internetPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(CAMERA);
            }

            if (internetPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(READ_EXTERNAL_STORAGE);
            }

            if (internetPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE);
            }

            //La lista de permisos esta vacia?
            //Si esta vacia es porque no necesito otorgar ningun permiso, ya se concedieron todos.
            if (listPermissionsNeeded.isEmpty()) {
                return true;
            }
            //Si no pido los permisos necesarios
            else{
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthStateListener);
        //CHECK IF USER IS LOGGED IN
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null && !logout){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else if(logout){
            mGoogleSignInClient.signOut();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this,"Fallo de autenticación con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgress(true);
        // [END_EXCLUDE]

        mProgressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveTokenAndLogin();
                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this,"Fallo de autenticación con Google", Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "READ CONTACTS & INTERNET services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Todos los permisos son necesarios para esta aplicación",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Sorry you can't use the application", Toast.LENGTH_LONG)
                                    .show();
                            finish();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            mProgressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        saveTokenAndLogin();
                    } else {
                        Toast.makeText(LoginActivity.this, "No se ha podido loguear", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    protected void showInputDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_login_forgotten_password_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.fgEditText);

        ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.cardview_dark_background));
        edt.setBackgroundTintList(colorStateList);
        edt.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.tab_icons_not_pressed)));

        dialogBuilder.setTitle("Correo de recuperación");
        dialogBuilder.setMessage("Recibirá un enlace de recuperación");
        dialogBuilder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(!TextUtils.isEmpty(edt.getText().toString().trim()) && isEmailValid(edt.getText().toString().trim())){
                    mAuth.sendPasswordResetEmail(edt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this,getString(R.string.recover_password_email_sent) + " " +edt.getText().toString(),Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(LoginActivity.this, R.string.recover_password_email_failed,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "Por favor, rellene el campo correctamente",Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    private void saveTokenAndLogin(){

        //El token me servir para identificar el dispositivo con el cual me logueo

        String token_id = FirebaseInstanceId.getInstance().getToken();
        String current_id = mAuth.getCurrentUser().getUid();

        Map<String,Object> tokenMap = new HashMap<>();
        tokenMap.put("token_id",token_id);

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(current_id);
        mUserRef.updateChildren(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mProgressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}

