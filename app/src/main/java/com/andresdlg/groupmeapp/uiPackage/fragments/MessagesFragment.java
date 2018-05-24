package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVMessageAdapter;
import com.andresdlg.groupmeapp.Entities.ConversationFirebase;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
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
    RecyclerView rv;

    OnNewMessageListener mOnNewMessageListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages,container,false);

        setRetainInstance(true);

        final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        rv = v.findViewById(R.id.rvMessages);
        rv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                try {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());

                    if (child != null && mGestureDetector.onTouchEvent(e)) {

                        int position = rv.getChildAdapterPosition(child);

                        Intent i = new Intent(getContext(), ChatActivity.class);
                        ArrayList<String> arrayList = new ArrayList<>();
                        if(conversations.get(position).getUser1().equals(StaticFirebaseSettings.currentUserId)){
                            arrayList.add(conversations.get(position).getUser2());
                        }else{
                            arrayList.add(conversations.get(position).getUser1());
                        }
                        i.putExtra("conversationKey",conversations.get(position).getId());
                        i.putExtra("contactIds",arrayList);
                        startActivity(i);
                        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);

                        return true;
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
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
                            if(dataSnapshot.child("id").getValue()!=null){
                                ConversationFirebase c = new ConversationFirebase();
                                c.setId(dataSnapshot.child("id").getValue().toString());
                                for(ConversationFirebase conversationFirebase : conversations){
                                    if(conversationFirebase.getId().equals(c.getId())){
                                        conversations.remove(conversationFirebase);
                                        break;
                                    }
                                }

                                ArrayList<Message> messages = new ArrayList<>();
                                int cantidadDeMensajesNoVistos = 0;
                                for(DataSnapshot data : dataSnapshot.child("messages").getChildren()){
                                    Message m = data.getValue(Message.class);
                                    messages.add(0,m);
                                    if(!checkIfIHaveSeenThisMessage(m)){
                                        cantidadDeMensajesNoVistos += 1;
                                    }
                                }
                                c.setMessages(messages);

                                mOnNewMessageListener.onNewMessage(cantidadDeMensajesNoVistos);

                                if(!messages.isEmpty()){
                                    c.setMessage(messages.get(0));
                                    c.setUser1(dataSnapshot.child("user1").getValue().toString());
                                    c.setUser2(dataSnapshot.child("user2").getValue().toString());
                                    conversations.add(c);
                                    adapter.notifyDataSetChanged();
                                    tvNoMessages.setVisibility(View.INVISIBLE);
                                }
                            /*if(cantidadDeMensajesNoVistos>0){
                                adapter.setNewMessagesIndicator(c.getId(),true);
                            }*/
                            }
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

    private boolean checkIfIHaveSeenThisMessage(Message m) {
        for(String s : m.getSeenBy()){
            if(s.equals(StaticFirebaseSettings.currentUserId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        onAttachToParentFragment(getActivity());
    }

    public interface OnNewMessageListener{
        void onNewMessage(int messageQuantity);
    }

    public void onAttachToParentFragment(FragmentActivity activity){
        try {
            mOnNewMessageListener = (OnNewMessageListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnUserSelectionSetListener");
        }
    }
}
