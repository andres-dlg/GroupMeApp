package com.andresdlg.groupmeapp.uiPackage;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileSetupActivity extends AppCompatActivity {

    //FIELDS DECLARATION
    ImageButton mBack;
    CircleImageView mCircleImageView;
    AutoCompleteTextView mAlias;
    EditText mName;
    AutoCompleteTextView mJob;
    AutoCompleteTextView mPhone;
    Button mSaveButton;
    TextView mLater;
    Uri mCropImageUri;
    TextInputLayout mTextInputAlias;
    TextInputLayout mTextInputJob;
    TextInputLayout mTextInputPhone;
    CardView mMetricsLinearLayout;
    TextView mGroupQuantity;
    TextView mSubGroupQuantity;
    TextView mTasksQuantity;
    TextView mCompletedTasksQuantity;
    ImageButton mEdit;
    ImageButton mCall;
    ImageButton mFriendship;

    //FIREBASE AUTHENTICATION FIELDS
    FirebaseAuth mAuth;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUserDatabase;
    DatabaseReference mGroupsRef;
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
    boolean editMode = false;

    int cantidadTareas;
    int cantidadTareasCompletadas;

    String iduser;
    Users u;

    ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_setup);

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        cantidadTareas = 0;
        cantidadTareasCompletadas = 0;

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

        mFriendship = findViewById(R.id.friendship);

        mTextInputAlias = findViewById(R.id.til);

        mAlias = findViewById(R.id.alias);

        mName =  findViewById(R.id.user_profile_name);

        mTextInputJob = findViewById(R.id.til2);

        mJob =  findViewById(R.id.job);

        mTextInputPhone = findViewById(R.id.til3);

        mPhone = findViewById(R.id.phone);

        mMetricsLinearLayout = findViewById(R.id.statsCv);

        mGroupQuantity = findViewById(R.id.groupQuantityNumber);

        mSubGroupQuantity = findViewById(R.id.subgroupQuantityNumber);

        mTasksQuantity = findViewById(R.id.taskQuantityNumber);

        mCompletedTasksQuantity = findViewById(R.id.completedTasksQuantityNumber);

        mEdit = findViewById(R.id.edit);

        mCall = findViewById(R.id.callBtn);
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialContactPhone(mPhone.getText().toString().trim());
            }
        });

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
            mGroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
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

        //Si iduser es null es porque entra a configurar el perfil por primera vez
        if(iduser != null){

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            mTextInputAlias.setEnabled(false);
            mTextInputJob.setEnabled(false);
            mTextInputPhone.setEnabled(false);
            //mJob.setEnabled(false);
            //mPhone.setEnabled(false);

            if(!iduser.equals(StaticFirebaseSettings.currentUserId)){
                mSaveButton.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                mName.setEnabled(false);
                setFriendshipIcon();
            }else{
                mFriendship.setVisibility(View.GONE);
                mEdit.setVisibility(View.VISIBLE);
                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!editMode){
                            mTextInputAlias.setEnabled(true);
                            mTextInputJob.setEnabled(true);
                            mTextInputPhone.setEnabled(true);
                            //mJob.setEnabled(true);
                            mEdit.setImageDrawable(ContextCompat.getDrawable(UserProfileSetupActivity.this,R.drawable.check_green));
                        }else{
                            mTextInputAlias.setEnabled(false);
                            mTextInputJob.setEnabled(false);
                            mTextInputPhone.setEnabled(false);
                            //mJob.setEnabled(false);
                            mEdit.setImageDrawable(ContextCompat.getDrawable(UserProfileSetupActivity.this,R.drawable.ic_pen_black_24dp));
                        }
                        editMode = !editMode;
                    }
                });
            }
            setUserData();
        }else{
            mMetricsLinearLayout.setVisibility(View.GONE);
        }

    }

    private void setFriendshipIcon() {
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null || dataSnapshot.child("status").getValue().toString().equals(FriendshipStatus.REJECTED.toString())){
                    mFriendship.setImageDrawable(ContextCompat.getDrawable(UserProfileSetupActivity.this,R.drawable.ic_account_plus_white_24dp));
                    mFriendship.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendInvitation();
                        }
                    });
                }else if(dataSnapshot.child("status").getValue().toString().equals(FriendshipStatus.ACCEPTED.toString())){
                    mFriendship.setImageDrawable(ContextCompat.getDrawable(UserProfileSetupActivity.this,R.drawable.ic_account_check_white_24dp));
                    mFriendship.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(UserProfileSetupActivity.this, "Este usuario ya es tu contacto", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if(dataSnapshot.child("status").getValue().toString().equals(FriendshipStatus.PENDING.toString())){
                    mFriendship.setImageDrawable(ContextCompat.getDrawable(UserProfileSetupActivity.this,R.drawable.ic_timer_sand_white_24dp));
                    mFriendship.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(UserProfileSetupActivity.this, "Ya le has enviado una solicitud de contacto", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUserDatabase.child("friends").child(StaticFirebaseSettings.currentUserId).addValueEventListener(eventListener);
    }

    private void sendInvitation() {
        final String userFrom = StaticFirebaseSettings.currentUserId;

        FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(userFrom).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users me = dataSnapshot.getValue(Users.class);

                DatabaseReference userTo = FirebaseDatabase
                        .getInstance()
                        .getReference("Users")
                        .child(iduser);

                //Envio y almacenamiento de notificación
                DatabaseReference userToNotifications = userTo.child("notifications");
                String notificationKey = userToNotifications.push().getKey();
                Map<String,Object> notification = new HashMap<>();
                notification.put("notificationKey",notificationKey);
                notification.put("title","Solicitud de amistad");
                notification.put("message","Has recibido una solicitud de amistad de " + me.getName());
                notification.put("from",userFrom);
                notification.put("state", NotificationStatus.UNREAD);
                notification.put("date", Calendar.getInstance().getTimeInMillis());
                notification.put("type", NotificationTypes.FRIENDSHIP);

                userToNotifications.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfileSetupActivity.this, "Solicitud enviada", Toast.LENGTH_SHORT).show();
                    }
                });

                //Almacenamiento de nodo friend
                DatabaseReference userToFriends = userTo.child("friends");
                Map<String,Object> friend = new HashMap<>();
                friend.put("status", FriendshipStatus.PENDING);
                friend.put("seen", NotificationStatus.UNREAD);
                userToFriends.child(userFrom).updateChildren(friend);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void dialContactPhone(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    private void setUserData() {

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //PARA LAS ESTADISTICAS
                int groupQuantity = (int)dataSnapshot.child("groups").getChildrenCount();
                mGroupQuantity.setText(String.valueOf(groupQuantity));

                int subgroupQuantity = 0;
                for(DataSnapshot groupDataSnapshot : dataSnapshot.child("groups").getChildren()){

                    for(DataSnapshot subGroupDataSnapshot : groupDataSnapshot.child("subgroups").getChildren()){
                        getTasksStats(groupDataSnapshot.getKey(),subGroupDataSnapshot.getKey());
                    }

                    subgroupQuantity += (int)groupDataSnapshot.child("subgroups").getChildrenCount();
                }
                mSubGroupQuantity.setText(String.valueOf(subgroupQuantity));

                u = dataSnapshot.getValue(Users.class);
                mName.setText(u.getName());
                mAlias.setText(u.getAlias());
                mJob.setText(u.getJob());
                String phone = u.getPhone();
                if(phone!=null && !phone.equals("")){
                    mPhone.setText(phone);
                }else{
                    mPhone.setText("Sin datos");
                }


                if((!u.getUserid().equals(StaticFirebaseSettings.currentUserId) && !mPhone.getText().toString().trim().equals("Sin datos"))){
                    mCall.setVisibility(View.VISIBLE);
                }
                //supportPostponeEnterTransition();
                //RequestOptions requestOptions = new RequestOptions().dontAnimate();
                Glide.with(UserProfileSetupActivity.this)
                        .load(u.getImageURL())
                        //.apply(requestOptions)
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

    private void getTasksStats(String groupKey, final String subGroupKey) {

        mGroupsRef.child(groupKey).child("subgroups").child(subGroupKey).child("tasks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cantidadTareas += (int)dataSnapshot.getChildrenCount();

                for(DataSnapshot taskDataSnapshot : dataSnapshot.getChildren()){
                    if((boolean)taskDataSnapshot.child("finished").getValue()){
                        cantidadTareasCompletadas += 1;
                    }
                }

                mCompletedTasksQuantity.setText(String.valueOf(cantidadTareasCompletadas));
                mTasksQuantity.setText(String.valueOf(cantidadTareas));
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
        final String alias, userName, job, phone;
        alias = mAlias.getText().toString().trim();
        userName = mName.getText().toString().trim();
        job = mJob.getText().toString().trim();
        phone = mPhone.getText().toString().trim();

        pass = false;
        View focusView;

        if (TextUtils.isEmpty(userName)) {
            mName.setError("Este campo es necesario");
            focusView = mName;
            focusView.requestFocus();
            pass = true;
        }else if (TextUtils.isEmpty(alias)) {
            mAlias.setError("Este campo es necesario");
            focusView = mAlias;
            focusView.requestFocus();
            pass = true;
        }else if(!isAliasValid(alias)){
            mAlias.setError("Sin espacios ni caracteres especiales");
            focusView = mAlias;
            focusView.requestFocus();
            pass = true;
        }else if(!isValidPhone(phone)){
            mPhone.setError("Formato de teléfono inválido");
            focusView = mPhone;
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
                                    createUserData(alias,userName,job,phone);
                                }
                            });
                        }else{
                            if(iduser == null){
                                imageHoldUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/groupmeapp-5aaf6.appspot.com/o/new_user.png?alt=media&token=8875556e-566a-4717-9654-6a2c27fa7cc6");
                            }else{
                                imageHoldUri = Uri.parse(u.getImageURL());
                            }
                            mProgress.dismiss();
                            createUserData(alias,userName,job,phone);
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

    private void createUserData(String alias,String userName,String job, String phone) {
        mUserDatabase.child("alias").setValue(alias.toLowerCase());
        mUserDatabase.child("name").setValue(userName);
        mUserDatabase.child("lowerCaseName").setValue(userName.toLowerCase()); //para busquedas
        mUserDatabase.child("job").setValue(job);
        mUserDatabase.child("phone").setValue(phone);
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
        return alias.matches("[A-Za-z0-9]*");
    }

    public boolean isValidPhone(CharSequence phone) {
        if (TextUtils.isEmpty(phone)) {
            //return false;
            return true;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgress.dismiss();
        if(eventListener!=null){
            mUserDatabase.removeEventListener(eventListener);
        }
    }
}
