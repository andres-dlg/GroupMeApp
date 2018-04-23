package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.ConversationFirebase;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by andresdlg on 21/01/18.
 */

public class RVMessageAdapter extends RecyclerView.Adapter<RVMessageAdapter.MessageViewHolder>{


    DatabaseReference conversationRef;
    DatabaseReference userRef;

    ValueEventListener conversationEventListener;
    ValueEventListener userValueEventListener;

    private List<ConversationFirebase> conversations;
    private Context context;

    public RVMessageAdapter(List<ConversationFirebase> conversations, Context context){
        this.conversations = conversations;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_messages_list, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") final int position) {

        Collections.sort(conversations, new Comparator<ConversationFirebase>() {
            @Override
            public int compare(ConversationFirebase c1, ConversationFirebase c2) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(c1.getMessage().getTimestamp());
                Calendar cl2 = Calendar.getInstance();
                cl2.setTimeInMillis(c2.getMessage().getTimestamp());
                return cl2.compareTo(c);
            }
        });

        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"hola",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(view.getContext(), ChatActivity.class);
                ArrayList<String> arrayList = new ArrayList<>();
                if(conversations.get(position).getUser1().equals(StaticFirebaseSettings.currentUserId)){
                    arrayList.add(conversations.get(position).getUser2());
                }else{
                    arrayList.add(conversations.get(position).getUser1());
                }
                i.putExtra("conversationKey",conversations.get(position).getId());
                i.putExtra("contactIds",arrayList);
                context.startActivity(i);
            }
        });

        conversationRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversations.get(position).getId());
        conversationEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String idUser = null;
                if(dataSnapshot.child("user1").getValue().toString().equals(StaticFirebaseSettings.currentUserId)){
                    idUser = dataSnapshot.child("user2").getValue().toString();
                }else{
                    idUser = dataSnapshot.child("user1").getValue().toString();
                }

                userRef = FirebaseDatabase.getInstance().getReference("Users").child(idUser);
                userValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Users u = dataSnapshot.getValue(Users.class);
                        messageViewHolder.userAlias.setText(new StringBuilder().append("@").append(u.getAlias()).toString());
                        messageViewHolder.setPhoto(context,u.getImageURL());//Picasso.with(context).load(u.getImageURL()).into(messageViewHolder.userPhoto);
                        messageViewHolder.messageText.setText(conversations.get(position).getMessage().getText());
                        messageViewHolder.messageDate.setText(dateDifference(conversations.get(position).getMessage().getTimestamp()));

                        //removeUsersListener();
                        //removeConversationListener();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                userRef.addValueEventListener(userValueEventListener);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        conversationRef.addListenerForSingleValueEvent(conversationEventListener);
    }

    private void removeConversationListener() {
        conversationRef.removeEventListener(conversationEventListener);
    }

    private void removeUsersListener(){
        userRef.removeEventListener(userValueEventListener);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private String dateDifference(Long d) {

        Calendar today = Calendar.getInstance();
        long diff = today.getTimeInMillis() - d;

        if(diff/1000 <= 60){
            return ("Hace "+ Math.round(diff/1000)+ " segundos");
        }else if(diff/1000/60 < 60){
            return ("Hace "+ Math.round(diff/1000/60)+ " minutos");
        }else if(diff/1000/60/60 < 24){
            return ("Hace "+ Math.round(diff/1000/60/60)+ " horas");
        }else{
            return ("Hace "+ Math.round(diff/1000/60/60/24)+ " dias");
        }
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userAlias;
        ImageView userPhoto;
        TextView messageText;
        TextView messageDate;
        View mView;

        MessageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            messageText = itemView.findViewById(R.id.messageText);
            messageDate = itemView.findViewById(R.id.date);
        }

        public void setPhoto(final Context context, final String imageURL) {
            Picasso.with(context).load(imageURL).into(userPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                }
                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(imageURL)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(userPhoto, new Callback() {
                                @Override
                                public void onSuccess() {
                                    itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
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
}
