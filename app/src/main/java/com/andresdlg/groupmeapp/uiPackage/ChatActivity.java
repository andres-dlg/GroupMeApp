package com.andresdlg.groupmeapp.uiPackage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.ListMessageAdapter;
import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String conversationKey;
    private ArrayList<String> contactIds;
    private Conversation conversation;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    private Users userTo;
    private Users currentUser;
    private LinearLayout toolbarContainer;
    private TextView tv;
    private CircleImageView civ;
    private View dummyView;

    DatabaseReference conversationRef;
    DatabaseReference userToRef;
    DatabaseReference currentUserRef;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Toolbar toolbar = findViewById(R.id.toolbar_chats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarContainer = findViewById(R.id.toolbar_container);

        tv = toolbar.findViewById(R.id.action_bar_title_1);
        civ = toolbar.findViewById(R.id.conversation_contact_photo);
        contactIds = getIntent().getStringArrayListExtra("contactIds");
        conversationKey = getIntent().getStringExtra("conversationKey");

        editWriteMessage = findViewById(R.id.editWriteMessage);

        dummyView = findViewById(R.id.dummyView);

        //conversationRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversationKey);
        conversationRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("conversation").child(conversationKey);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    for(DataSnapshot messageRef : dataSnapshot.getChildren()){
                        Message m = messageRef.getValue(Message.class);
                        boolean existo = false;
                        for(String s : m.getSeenBy()){
                            if(s.equals(StaticFirebaseSettings.currentUserId)){
                                existo = true;
                                break;
                            }
                        }
                        if(!existo){
                            List<String> seenBy = m.getSeenBy();
                            seenBy.add(StaticFirebaseSettings.currentUserId);
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference("Conversations")
                                    .child(conversationKey)
                                    .child("messages")
                                    .child(m.getId())
                                    .child("seenBy")
                                    .setValue(seenBy);
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference("Users")
                                    .child(StaticFirebaseSettings.currentUserId)
                                    .child("conversation")
                                    .child(conversationKey)
                                    .child("messages")
                                    .child(m.getId())
                                    .child("seenBy")
                                    .setValue(seenBy);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        conversationRef.child("messages").addValueEventListener(valueEventListener);

        conversation = new Conversation();
        final ImageButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        userToRef = FirebaseDatabase.getInstance().getReference("Users").child(contactIds.get(0));
        userToRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userTo = dataSnapshot.getValue(Users.class);
                currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
                tv.setText(userTo.getName());
                Glide.with(ChatActivity.this)
                        .load(userTo.getImageURL())
                        .into(civ);

                toolbarContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userProfileIntent = new Intent(ChatActivity.this, UserProfileSetupActivity.class);
                        userProfileIntent.putExtra("iduser",userTo.getUserid());
                        startActivity(userProfileIntent);
                    }
                });

                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(Users.class);

                        if (userTo != null && currentUser != null) {
                            if(!dataSnapshot.child("friends").child(userTo.getUserid()).exists() || !dataSnapshot.child("friends").child(userTo.getUserid()).child("status").getValue().toString().equals(FriendshipStatus.ACCEPTED.toString())){
                                editWriteMessage.setClickable(false);
                                editWriteMessage.setFocusable(false);
                                btnSend.setClickable(false);
                                btnSend.setFocusable(false);
                                dummyView.setVisibility(View.VISIBLE);
                                dummyView.bringToFront();
                                dummyView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(ChatActivity.this, "Este usuario ya no es tu contacto. Búscalo y agregalo para chatear con él", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
                            recyclerChat = findViewById(R.id.recyclerChat);
                            recyclerChat.setLayoutManager(linearLayoutManager);
                            adapter = new ListMessageAdapter(getBaseContext(), conversation, userTo.getImageURL(), currentUser.getImageURL(),"User", conversationKey);
                            //FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversationKey).child("messages").addChildEventListener(new ChildEventListener() {
                            FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("conversation").child(conversationKey).child("messages").addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if (dataSnapshot.getValue() != null) {
                                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                                        Message newMessage = new Message();
                                        newMessage.setIdSender((String) mapMessage.get("idSender"));
                                        newMessage.setIdReceiver((String) mapMessage.get("idReceiver"));
                                        newMessage.setText ((String) mapMessage.get("text"));
                                        newMessage.setTimestamp((long) mapMessage.get("timestamp"));
                                        newMessage.setId((String) mapMessage.get("id"));
                                        conversation.getListMessageData().add(newMessage);
                                        adapter.notifyDataSetChanged();
                                        linearLayoutManager.scrollToPosition(conversation.getListMessageData().size() - 1);
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            recyclerChat.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.delete:
                deleteConversation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteConversation() {
        new AlertDialog.Builder(this,R.style.MyDialogTheme)
                .setTitle("¿Desea eliminar esta conversación?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase
                                .getInstance()
                                .getReference("Users")
                                .child(currentUser.getUserid())
                                .child("conversation")
                                .child(conversationKey)
                                .removeValue();

                        FirebaseDatabase
                                .getInstance()
                                .getReference("Users")
                                .child(userTo.getUserid())
                                .child("conversation")
                                .child(conversationKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.hasChildren()){
                                    conversationRef.removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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


    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", contactIds.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversationRef.removeEventListener(valueEventListener);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                List<String> seenBy = new ArrayList<>();
                seenBy.add(StaticFirebaseSettings.currentUserId);
                editWriteMessage.setText("");
                DatabaseReference dbRef = conversationRef.child("messages").push();
                Message newMessage = new Message();
                newMessage.setText(content);
                newMessage.setIdSender(StaticFirebaseSettings.currentUserId);
                newMessage.setSeenBy(seenBy);
                //Tocar esto cuando la conversación sea grupal
                newMessage.setIdReceiver(contactIds.get(0));
                newMessage.setTimestamp(System.currentTimeMillis());
                newMessage.setId(dbRef.getKey());
                FirebaseDatabase.getInstance().getReference("Conversations").child(conversationKey).child("messages").child(newMessage.getId()).setValue(newMessage);
                dbRef.setValue(newMessage);
                userToRef.child("conversation").child(conversationKey).child("messages").child(newMessage.getId()).setValue(newMessage);
                //currentUserRef.child("conversation").child(conversationKey).child("messages").child(newMessage.getId()).setValue(newMessage);
            }
        }
    }
}
