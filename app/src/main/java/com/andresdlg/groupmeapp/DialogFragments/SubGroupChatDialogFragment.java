package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.ListMessageAdapter;
import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupChatDialogFragment extends DialogFragment {

    private String subGroupName;
    private String subGroupUrlPhoto;
    private String subGroupKey;

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

    public SubGroupChatDialogFragment(String subGroupName, String subGroupUrlPhoto, String subGroupKey) {

        this.subGroupName = subGroupName;
        this.subGroupUrlPhoto = subGroupUrlPhoto;
        this.subGroupKey = subGroupKey;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subgroup_chat, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar_chats);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView tv = toolbar.findViewById(R.id.action_bar_title_1);
        tv.setText(subGroupName);

        CircleImageView civ = toolbar.findViewById(R.id.conversation_contact_photo);
        Glide.with(getContext())
                .load(subGroupUrlPhoto)
                .into(civ);

        conversationKey = subGroupKey;

        conversation = new Conversation();

        ImageButton btnSend =  v.findViewById(R.id.btnSend);
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
        RecyclerView recyclerChat = (RecyclerView) v.findViewById(R.id.recyclerChat);
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

        editWriteMessage =  v.findViewById(R.id.editWriteMessage);

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}