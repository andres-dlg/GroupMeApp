package com.andresdlg.groupmeapp.uiPackage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.andresdlg.groupmeapp.Adapters.RVGroupDetailAdapter;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by andresdlg on 16/03/18.
 */

public class GroupDetailActivity extends AppCompatActivity {

    String groupKey;
    DatabaseReference groupRef;
    DatabaseReference usersRef;
    List<Users> usersList;
    Map<String, String> usersRoles;
    RVGroupDetailAdapter adapter;
    Map<String, String> members;

    EditText objetive;
    TextView tv;
    private boolean editMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_group_details);

        final Animation myFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fadein);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();
        toolbar.setTitle(" ");
        toolbar.setBackground(getResources().getDrawable(R.drawable.gradient_black_rotated));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.startAnimation(myFadeInAnimation);

        usersList = new ArrayList<>();
        usersRoles = new HashMap<>();

        groupKey = getIntent().getStringExtra("groupKey");
        final String groupName = getIntent().getStringExtra("groupName");
        final String groupPhotoUrl = getIntent().getStringExtra("groupPhotoUrl");

        tv = findViewById(R.id.group_name);
        tv.setSelected(true);

        final ImageButton editObjetiveBtn = findViewById(R.id.editObjetiveBtn);
        editObjetiveBtn.startAnimation(myFadeInAnimation);

        final ImageButton editGroupNameBtn = findViewById(R.id.editGroupNameBtn);

        objetive = findViewById(R.id.objetive);
        final ImageView iv = findViewById(R.id.add_group_photo);

        final ImageButton addContact = findViewById(R.id.addContact);
        addContact.startAnimation(myFadeInAnimation);

        final FloatingActionButton fab = findViewById(R.id.fab);

        final RecyclerView rv = findViewById(R.id.rvMembers);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        //adapter = new RVGroupDetailAdapter(usersList,usersRoles,groupKey,this);
        //rv.setAdapter(adapter);

        tv.setText(groupName);

        Target target = new Target() {
            @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                iv.setImageBitmap(bitmap);
            }
            @Override public void onBitmapFailed(Drawable errorDrawable) {}
            @Override public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        Picasso.with(this)
                .load(groupPhotoUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(target);

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

                if(amIadmin()){

                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(GroupDetailActivity.this, "IMPLEMENTAR", Toast.LENGTH_SHORT).show();
                        }
                    });

                    editGroupNameBtn.setVisibility(View.VISIBLE);
                    editGroupNameBtn.startAnimation(myFadeInAnimation);
                    editGroupNameBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MaterialDialog.Builder(GroupDetailActivity.this)
                                    .title("Nombre del grupo")
                                    .content("Nombre")
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input("Ingrese el nombre", null, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                            if(!TextUtils.isEmpty(input)){
                                                saveGroupName(input.toString());
                                            }else{
                                                dialog.getInputEditText().setError("Este campo es necesario");
                                            }
                                        }
                                    })
                                    .show();
                        }
                    });

                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(amIadmin()){
                                new AlertDialog.Builder(GroupDetailActivity.this,R.style.MyDialogTheme)
                                        .setTitle("¿Desea cambiar el nombre del grupo?")
                                        //.setMessage("Ya no estará disponib")
                                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                tv.setEnabled(true);
                                            }
                                        })
                                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        }
                    });


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

    private void saveGroupName(final String newName) {
        groupRef.child("name").setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                tv.setText(newName);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
