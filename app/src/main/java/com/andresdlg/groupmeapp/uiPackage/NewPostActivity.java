package com.andresdlg.groupmeapp.uiPackage;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    DatabaseReference postsRef;

    EditText postText;
    Button postBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String groupKey = ((FireApp) getApplication()).getGroupKey();
        postsRef = FirebaseDatabase
                .getInstance()
                .getReference("Groups")
                .child(groupKey)
                .child("posts");

        postText = findViewById(R.id.postText);

        postBtn = findViewById(R.id.postBtn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(postText.getText().toString().isEmpty()){
                    Toast.makeText(NewPostActivity.this, "Publicación vacía", Toast.LENGTH_SHORT).show();
                }else{

                    Calendar timeNow = Calendar.getInstance();

                    String postId = postsRef.push().getKey();

                    Map<String,Object> newPost = new HashMap<>();
                    newPost.put("postId",postId);
                    newPost.put("userId", StaticFirebaseSettings.currentUserId);
                    newPost.put("time",timeNow.getTimeInMillis());
                    newPost.put("text",postText.getText().toString());

                    postsRef.child(postId).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(NewPostActivity.this, "Publicación realizada!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewPostActivity.this, "Hubo un error al guardar la publicación", Toast.LENGTH_SHORT).show();
                        }
                    });

                    finish();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                if(postText.getText().toString().isEmpty()){
                    onBackPressed();
                }else {
                    new AlertDialog.Builder(NewPostActivity.this,R.style.MyDialogTheme)
                            .setTitle("¿Desea descartar la publicación?")
                            //.setMessage("Ya no estará disponib")
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
