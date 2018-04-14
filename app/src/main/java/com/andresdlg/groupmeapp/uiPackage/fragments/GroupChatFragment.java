package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.ListMessageAdapter;
import com.andresdlg.groupmeapp.Adapters.RVSubGroupAdapter;
import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Entities.SubGroup;

import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 02/05/17.
 */

public class GroupChatFragment extends Fragment {


    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String conversationKey;
    private Conversation conversation;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    private Users userTo;
    private Users currentUser;

    OnGroupFragmentVisibilityListener mOnGroupFragmentVisibilityListener;

    static List<Users> groupUsers;
    private boolean isVisibleToUser;

    public static void setGroupUsers(List<Users> users){
        groupUsers = new ArrayList<>();
        //groupUsers.clear();
        users.addAll(groupUsers);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_chat, container, false);
        return v;
    }

    /*private List<String> getContactIds() {
        List<String> ids = new ArrayList<>();
        List<Users> usersList = ((FireApp) getActivity().getApplication()).getGroupUsers();
        for (Users u : usersList) {
            ids.add(u.getUserid());
        }
        return ids;
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getActivity());
        mOnGroupFragmentVisibilityListener.onGroupFragmentSet(isVisibleToUser);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_chats);
        toolbar.setVisibility(View.GONE);

        conversationKey = ((FireApp) getActivity().getApplication()).getGroupKey();
        //List<String> contactIds = getContactIds();

        conversation = new Conversation();

        ImageButton btnSend = (ImageButton) view.findViewById(R.id.btnSend);
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

        //DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference().child("Conversations").child(conversationKey)

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerChat = (RecyclerView) view.findViewById(R.id.recyclerChat);
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

        /*DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(contactIds.get(0));
        userToRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userTo = dataSnapshot.getValue(Users.class);
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);
                //tv.setText(userTo.getName());
                //Picasso.with(getContext()).load(userTo.getImageURL()).into(civ);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(Users.class);
                        if (userTo != null && currentUser != null) {
                            linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                            recyclerChat = (RecyclerView) view.findViewById(R.id.recyclerChat);
                            recyclerChat.setLayoutManager(linearLayoutManager);
                            adapter = new ListMessageAdapter(getContext(), conversation, userTo.getImageURL(), currentUser.getImageURL());
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
        });*/
        editWriteMessage = (EditText) view.findViewById(R.id.editWriteMessage);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        /*onAttachToParentFragment(getActivity());
        if (isVisibleToUser){
            this.isVisibleToUser = isVisibleToUser;
            mOnGroupFragmentVisibilityListener.onGroupFragmentSet(isVisibleToUser);
            Log.v("VISIBILIDAD: ","Visible al usuario");
        }else {
            this.isVisibleToUser = isVisibleToUser;
            mOnGroupFragmentVisibilityListener.onGroupFragmentSet(isVisibleToUser);
            Log.v("VISIBILIDAD: ","Invisible al usuario");
        }*/
    }


    public interface OnGroupFragmentVisibilityListener{
        public void onGroupFragmentSet(boolean isVisibleToUser);
    }

    public void onAttachToParentFragment(Context context){
        try {
            mOnGroupFragmentVisibilityListener = (OnGroupFragmentVisibilityListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnUserSelectionSetListener");
        }
    }
}