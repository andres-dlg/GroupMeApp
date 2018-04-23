package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.andresdlg.groupmeapp.Adapters.ListMessageAdapter;
import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Message;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class GroupChatFragment extends Fragment {

    private ListMessageAdapter adapter;
    private String conversationKey;
    private Conversation conversation;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;

    static List<Users> groupUsers;

    public static void setGroupUsers(List<Users> users){
        groupUsers = new ArrayList<>();
        //groupUsers.clear();
        users.addAll(groupUsers);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);

        Toolbar toolbar =  view.findViewById(R.id.toolbar_chats);
        toolbar.setVisibility(View.GONE);

        conversationKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        conversation = new Conversation();

        ImageButton btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
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
                        newMessage.setIdReceiver(null);
                        newMessage.setTimestamp(System.currentTimeMillis());
                        newMessage.setId(dbRef.getKey());
                        dbRef.setValue(newMessage);
                    }
                }
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerChat = view.findViewById(R.id.recyclerChat);
        recyclerChat.setLayoutManager(linearLayoutManager);
        adapter = new ListMessageAdapter(getContext(), conversation, null, null,"Group");

        FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversationKey).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                    Message newMessage = new Message();
                    newMessage.setIdSender((String) mapMessage.get("idSender"));
                    newMessage.setIdReceiver((String) mapMessage.get("idReceiver"));
                    newMessage.setText((String) mapMessage.get("text"));
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

        editWriteMessage = view.findViewById(R.id.editWriteMessage);
    }
}