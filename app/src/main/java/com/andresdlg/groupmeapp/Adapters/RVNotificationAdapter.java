package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
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

import static com.andresdlg.groupmeapp.Utils.NotificationTypes.GROUP_INVITATION;

/**
 * Created by andresdlg on 13/01/18.
 */

public class RVNotificationAdapter extends RecyclerView.Adapter<RVNotificationAdapter.NotificationViewHolder>{

    private DatabaseReference usersRef;
    private DatabaseReference groupsRef;
    private static List<Notification> notifications;
    private Context context;

    public RVNotificationAdapter(List<Notification> notifications, Context context){
        this.notifications = notifications;
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list_item, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder notificationViewHolder, @SuppressLint("RecyclerView") final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        if(!notifications.get(position).getType().equals(GROUP_INVITATION.toString())){
            DatabaseReference userRef = usersRef.child(notifications.get(position).getFrom());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    notificationViewHolder.userAlias.setText(u.getAlias());
                    notificationViewHolder.setImage(context,u.getImageURL());
                    notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                    notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                    String date = dateDifference(notifications.get(position).getDate());
                    notificationViewHolder.notificationDate.setText(date);
                    notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {


                }
            });
        }else{
            DatabaseReference groupRef = groupsRef.child(notifications.get(position).getFrom());
            groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Group g = dataSnapshot.getValue(Group.class);
                        notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                        notificationViewHolder.setImage(context,g.getImageUrl());
                        notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                        String date = dateDifference(notifications.get(position).getDate());
                        notificationViewHolder.notificationDate.setText(date);
                        notificationViewHolder.setGroupKey(g.getGroupKey(),usersRef);
                        notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
        ImageButton menuBtn;
        TextView userAlias;
        ImageView userPhoto;
        TextView notificationMessage;
        TextView notificationDate;
        private String groupKey;
        private DatabaseReference usersRef;
        private String notificationKey;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            notificationMessage = itemView.findViewById(R.id.notificationText);
            notificationMessage.setSelected(true);
            notificationDate = itemView.findViewById(R.id.date);
            menuBtn = itemView.findViewById(R.id.menu_btn);
        }

        void setImage(final Context context, final String imageURL) {
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

        void setGroupKey(String groupKey, DatabaseReference usersRef){
            this.groupKey = groupKey;
            this.usersRef = usersRef;
        }

        void hideBtn(final Context context, String type) {
            if(!type.equals(GROUP_INVITATION.toString())){
                menuBtn.setVisibility(View.GONE);
            }else{
                userAlias.setVisibility(View.INVISIBLE);
                menuBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final PopupMenu popupMenu = new PopupMenu(context, view);
                        final Menu menu = popupMenu.getMenu();

                        popupMenu.getMenuInflater().inflate(R.menu.fragment_notifications_group_menu, menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id){
                                    case R.id.accept:
                                        //ENVIAR MENSAJE
                                        acceptInvitation(context, groupKey);
                                        //Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.reject:
                                        //AGREGAR A GRUPO
                                        //rejectRequest(iduser);
                                        //Toast.makeText(context,"rechazar"+contactName, Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.delete:
                                        //ELIMINAR CONTACTO
                                        rejectInvitation(context, groupKey);
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        }

        private void acceptInvitation(Context context, String groupKey) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("groups").child(groupKey);
            userGroupRef.child("status").setValue(GroupStatus.ACCEPTED.toString());
            deleteNotification(context);
        }

        private void rejectInvitation(Context context, String groupKey) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("groups").child(groupKey);
            userGroupRef.child("status").setValue(GroupStatus.REJECTED.toString());
            deleteNotification(context);
        }

        private void deleteNotification(Context context) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("notifications").child(notificationKey);
            userGroupRef.removeValue();
        }

        void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }
    }
}
