package com.andresdlg.groupmeapp.uiPackage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.ListMessageAdapter;
import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Entities.Message;
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

    DatabaseReference conversationRef;
    ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Toolbar toolbar = findViewById(R.id.toolbar_chats);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Toolbar","Clicked");
            }
        });

        final TextView tv = toolbar.findViewById(R.id.action_bar_title_1);
        final CircleImageView civ = toolbar.findViewById(R.id.conversation_contact_photo);
        contactIds = getIntent().getStringArrayListExtra("contactIds");
        conversationKey = getIntent().getStringExtra("conversationKey");

        conversationRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversationKey);
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
        ImageButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(contactIds.get(0));
        userToRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userTo = dataSnapshot.getValue(Users.class);
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
                tv.setText(userTo.getName());
                Glide.with(ChatActivity.this)
                        .load(userTo.getImageURL())
                        .into(civ);
                //Picasso.with(ChatActivity.this).load(userTo.getImageURL()).into(civ);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(Users.class);
                        if (userTo != null && currentUser != null) {
                            linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
                            recyclerChat = findViewById(R.id.recyclerChat);
                            recyclerChat.setLayoutManager(linearLayoutManager);
                            adapter = new ListMessageAdapter(getBaseContext(), conversation, userTo.getImageURL(), currentUser.getImageURL(),"User", conversationKey);
                            FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversationKey).child("messages").addChildEventListener(new ChildEventListener() {
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
        editWriteMessage = findViewById(R.id.editWriteMessage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
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
                dbRef.setValue(newMessage);
            }
        }
    }
}
