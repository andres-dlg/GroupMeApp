package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.ConversationFirebase;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

/**
 * Created by andresdlg on 21/01/18.
 */

public class RVMessageAdapter extends RecyclerView.Adapter<RVMessageAdapter.MessageViewHolder>{

    private List<ConversationFirebase> conversations;
    private Context context;

    public RVMessageAdapter(List<ConversationFirebase> conversations, Context context){
        this.conversations = conversations;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversations.get(position).getId());
        conversationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String idUser = null;
                if(dataSnapshot.child("user1").getValue().toString().equals(StaticFirebaseSettings.currentUserId)){
                    idUser = dataSnapshot.child("user2").getValue().toString();
                }else{
                    idUser = dataSnapshot.child("user1").getValue().toString();
                }

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(idUser);
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Users u = dataSnapshot.getValue(Users.class);
                        messageViewHolder.userAlias.setText(new StringBuilder().append("@").append(u.getAlias()).toString());
                        Picasso.with(context).load(u.getImageURL()).into(messageViewHolder.userPhoto);
                        messageViewHolder.messageText.setText(conversations.get(position).getMessage().getText());
                        messageViewHolder.messageDate.setText(dateDifference(conversations.get(position).getMessage().getTimestamp()));
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
            messageText = itemView.findViewById(R.id.notificationText);
            messageDate = itemView.findViewById(R.id.date);
        }
    }
}
