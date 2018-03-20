package com.andresdlg.groupmeapp.uiPackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String conversationKey;
    private ArrayList<String> contactIds;
    private Conversation conversation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    private Users userTo;
    private Users currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chats);
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

        conversation = new Conversation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(contactIds.get(0));
        userToRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userTo = dataSnapshot.getValue(Users.class);
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
                tv.setText(userTo.getName());
                Picasso.with(ChatActivity.this).load(userTo.getImageURL()).into(civ);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(Users.class);
                        if (userTo != null && currentUser != null) {
                            linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
                            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
                            recyclerChat.setLayoutManager(linearLayoutManager);
                            adapter = new ListMessageAdapter(getBaseContext(), conversation, userTo.getImageURL(), currentUser.getImageURL());
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
        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
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
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversationKey).child("messages").push();
                Message newMessage = new Message();
                newMessage.setText(content);
                newMessage.setIdSender(StaticFirebaseSettings.currentUserId);
                //Tocar esto cuando la conversación sea grupal
                newMessage.setIdReceiver(contactIds.get(0));
                newMessage.setTimestamp(System.currentTimeMillis());
                newMessage.setId(dbRef.getKey());
                dbRef.setValue(newMessage);
            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Conversation conversation;
    private String userToUrl;
    private String currentUserUrl;

    public ListMessageAdapter(Context context, Conversation conversation, String userToUrl, String currentUserUrl) {
        this.context = context;
        this.conversation = conversation;
        this.userToUrl = userToUrl;
        this.currentUserUrl = currentUserUrl;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
            ((ItemMessageFriendHolder) holder).setAvatar(context,userToUrl);
        } else if (holder instanceof ItemMessageUserHolder) {
            ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
            ((ItemMessageUserHolder) holder).setAvatar(context,currentUserUrl);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).getIdSender().equals(StaticFirebaseSettings.currentUserId) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
    }

    public void setAvatar(final Context context, final String currentUserUrl) {
        Picasso.with(context).load(currentUserUrl).into(avata, new Callback() {
            @Override
            public void onSuccess() {

            }
            @Override
            public void onError() {
                Picasso.with(context)
                        .load(currentUserUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(avata, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Log.v("Picasso","No se ha podido cargar la foto");
                            }
                        });
            }
        });
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    ;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
    }

    public void setAvatar(final Context context, final String userToUrl) {
        Picasso.with(context).load(userToUrl).into(avata, new Callback() {
            @Override
            public void onSuccess() {

            }
            @Override
            public void onError() {
                Picasso.with(context)
                        .load(userToUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(avata, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Log.v("Picasso","No se ha podido cargar la foto");
                            }
                        });
            }
        });
    }
}
