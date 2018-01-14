package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.fragments.NotificationFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by andresdlg on 13/01/18.
 */

public class RVNotificationAdapter extends RecyclerView.Adapter<RVNotificationAdapter.NotificationViewHolder>{

    private List<Notification> notifications;
    private Context context;
    DatabaseReference userRef;

    public RVNotificationAdapter(List<Notification> notifications, Context context){
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder notificationViewHolder, final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(notifications.get(position).getFrom());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                notificationViewHolder.userAlias.setText(u.getAlias());
                Picasso.with(context).load(u.getImageURL()).into(notificationViewHolder.userPhoto);
                notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userAlias;
        ImageView userPhoto;
        TextView notificationMessage;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            notificationMessage = itemView.findViewById(R.id.notificationText);
            notificationMessage.setSelected(true);
        }
    }
}
