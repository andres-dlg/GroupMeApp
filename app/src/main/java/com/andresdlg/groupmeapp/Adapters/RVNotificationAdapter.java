package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andresdlg on 13/01/18.
 */

public class RVNotificationAdapter extends RecyclerView.Adapter<RVNotificationAdapter.NotificationViewHolder>{

    private List<Notification> notifications;
    private Context context;

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
    public void onBindViewHolder(final NotificationViewHolder notificationViewHolder, @SuppressLint("RecyclerView") final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(notifications.get(position).getFrom());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                notificationViewHolder.userAlias.setText(u.getAlias());
                notificationViewHolder.setImage(context,u.getImageURL());
                notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());

                String date = dateDifference(notifications.get(position).getDate());

                notificationViewHolder.notificationDate.setText(date);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String dateDifference(Date d) {

        Calendar dateNoti = Calendar.getInstance();
        dateNoti.setTime(d);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - dateNoti.getTimeInMillis();

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

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userAlias;
        ImageView userPhoto;
        TextView notificationMessage;
        TextView notificationDate;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            notificationMessage = itemView.findViewById(R.id.notificationText);
            notificationMessage.setSelected(true);
            notificationDate = itemView.findViewById(R.id.date);
        }

        public void setImage(final Context context, final String imageURL) {
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
