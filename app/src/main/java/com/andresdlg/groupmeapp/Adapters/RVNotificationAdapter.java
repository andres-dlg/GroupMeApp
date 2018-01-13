package com.andresdlg.groupmeapp.Adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.R;

import java.util.List;

/**
 * Created by andresdlg on 13/01/18.
 */

public class RVNotificationAdapter extends RecyclerView.Adapter<RVNotificationAdapter.NotificationViewHolder>{

    List<Notification> notifications;

    public RVNotificationAdapter(List<Notification> notifications){
        this.notifications = notifications;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list, parent, false);
        NotificationViewHolder nvh = new NotificationViewHolder(v);
        return nvh;
    }


    @Override
    public void onBindViewHolder(NotificationViewHolder notificationViewHolder, int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        notificationViewHolder.userName.setText("Pepito");
        notificationViewHolder.userPhoto.setImageResource(R.drawable.new_user);
        notificationViewHolder.notificationMessage.setText("Holanda");
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setNotifications(List<Notification> groups) {
        this.notifications = groups;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userPhoto;
        TextView notificationMessage;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userAlias);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            notificationMessage = itemView.findViewById(R.id.notificationText);
        }

    }
}
