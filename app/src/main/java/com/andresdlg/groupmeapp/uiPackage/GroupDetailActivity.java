package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVGroupDetailAdapter;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();
        toolbar.setTitle(" ");
        toolbar.setBackground(getResources().getDrawable(R.drawable.gradient_black_rotated));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersList = new ArrayList<>();
        usersRoles = new HashMap<>();

        groupKey = getIntent().getStringExtra("groupKey");
        final String groupName = getIntent().getStringExtra("groupName");
        final String groupPhotoUrl = getIntent().getStringExtra("groupPhotoUrl");

        TextView tv = findViewById(R.id.group_name);
        final ImageView iv = findViewById(R.id.add_group_photo);
        final FloatingActionButton fabAddContact = findViewById(R.id.fabAddContact);

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

                fabAddContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean isAdmin = false;
                        for(Map.Entry<String, String> entry : members.entrySet()) {
                            if(entry.getValue().equals(Roles.ADMIN.toString()) && entry.getKey().equals(StaticFirebaseSettings.currentUserId)){
                                isAdmin = true;
                            }
                        }
                        if(isAdmin){
                            Intent i = new Intent(GroupDetailActivity.this, SearchContactActivity.class);
                            i.putExtra("groupKey",groupKey);
                            startActivity(i);
                        }else{
                            Toast.makeText(GroupDetailActivity.this,"Debe ser administrador para agregar nuevos miembros",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
