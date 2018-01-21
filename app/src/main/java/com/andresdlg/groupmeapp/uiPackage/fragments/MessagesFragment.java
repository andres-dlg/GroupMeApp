package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVMessageAdapter;
import com.andresdlg.groupmeapp.Entities.ConversationFirebase;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class MessagesFragment extends Fragment {

    RVMessageAdapter adapter;
    TextView tvNoMessages;
    DatabaseReference firebaseConversations;
    List<ConversationFirebase> conversations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages,container,false);

        RecyclerView rv = v.findViewById(R.id.rvMessages);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        adapter = new RVMessageAdapter(conversations, getContext());
        rv.setAdapter(adapter);

        tvNoMessages = v.findViewById(R.id.tvNoMessages);



        firebaseConversations = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("conversation");
        firebaseConversations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                conversations.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    String idConversation = postSnapshot.getKey();

                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Conversations").child(idConversation);
                    dbRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            conversations.clear();
                            ConversationFirebase c = new ConversationFirebase();
                            c.setId(dataSnapshot.child("id").getValue().toString());
                            ArrayList<Message> messages = new ArrayList<>();
                            for(DataSnapshot data : dataSnapshot.child("messages").getChildren()){
                                Message m = data.getValue(Message.class);
                                messages.add(0,m);
                            }
                            c.setMessage(messages.get(0));
                            c.setUser1(dataSnapshot.child("user1").getValue().toString());
                            c.setUser2(dataSnapshot.child("user2").getValue().toString());
                            conversations.add(c);
                            adapter.notifyDataSetChanged();
                            tvNoMessages.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
