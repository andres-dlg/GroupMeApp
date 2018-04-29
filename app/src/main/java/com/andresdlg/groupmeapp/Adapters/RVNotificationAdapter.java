package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private DatabaseReference userRef;
    private DatabaseReference groupRef;
    private ValueEventListener usersEventListener;
    private ValueEventListener groupsEventListener;

    private List<Notification> notifications;
    private Context context;

    private OnSaveGroupListener mOnSaveGroupListener;

    public RVNotificationAdapter(List<Notification> notifications, Context context){
        this.notifications = notifications;
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

        onAttachToParentFragment(((AppCompatActivity)context).getSupportFragmentManager().getFragments().get(1));
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list_item, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, @SuppressLint("RecyclerView") final int position) {
        ///TODO: Recuperar información del usuario que envio la notificacion con FirebaseDatabase

        if(!notifications.get(position).getType().equals(GROUP_INVITATION.toString())){
            userRef = usersRef.child(notifications.get(position).getFrom());
            usersEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    notificationViewHolder.setPosition(position);
                    notificationViewHolder.userAlias.setText(u.getAlias());
                    notificationViewHolder.setImage(context,u.getImageURL());
                    notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                    notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                    String date = dateDifference(notifications.get(position).getDate());
                    notificationViewHolder.notificationDate.setText(date);
                    notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());

                    removeUserListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {


                }
            };
            userRef.addValueEventListener(usersEventListener);
        }else{
            groupRef = groupsRef.child(notifications.get(position).getFrom());
            groupsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Group g = dataSnapshot.getValue(Group.class);
                    notificationViewHolder.setPosition(position);
                    notificationViewHolder.hideBtn(context,notifications.get(position).getType());
                    notificationViewHolder.setImage(context,g.getImageUrl());
                    notificationViewHolder.notificationMessage.setText(notifications.get(position).getMessage());
                    String date = dateDifference(notifications.get(position).getDate());
                    notificationViewHolder.notificationDate.setText(date);
                    notificationViewHolder.setGroupKey(g.getGroupKey(),usersRef);
                    notificationViewHolder.setNotificationKey(notifications.get(position).getNotificationKey());

                    removeGroupsListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            groupRef.addListenerForSingleValueEvent(groupsEventListener);
        }
    }

    private void removeGroupsListener() {
        groupRef.removeEventListener(groupsEventListener);
    }

    private void removeUserListener() {
        userRef.removeEventListener(usersEventListener);
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageButton menuBtn;
        TextView userAlias;
        ImageView userPhoto;
        TextView notificationMessage;
        TextView notificationDate;
        private String groupKey;
        private DatabaseReference usersRef;
        private String notificationKey;
        private int position;

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

        void setPosition(int position){
            this.position = position;
        }

        void setImage(final Context context, final String imageURL) {
            Glide.with(context)
                    .load(imageURL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(userPhoto);

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
                                        acceptInvitation(context, groupKey, position);
                                        //Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.reject:
                                        //AGREGAR A GRUPO
                                        //rejectRequest(iduser);
                                        //Toast.makeText(context,"rechazar"+contactName, Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.delete:
                                        //ELIMINAR CONTACTO
                                        rejectInvitation(context, groupKey, position);
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

        private void acceptInvitation(Context context, String groupKey, int position) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("groups").child(groupKey);
            userGroupRef.child("status").setValue(GroupStatus.ACCEPTED.toString());
            deleteNotification(context);

            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notifications.size());

            mOnSaveGroupListener.onSavedGroup(true);
        }

        private void rejectInvitation(Context context, String groupKey, int position) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("groups").child(groupKey);
            userGroupRef.child("status").setValue(GroupStatus.REJECTED.toString());
            deleteNotification(context);

            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notifications.size());

            mOnSaveGroupListener.onSavedGroup(true);
        }

        private void deleteNotification(Context context) {
            DatabaseReference userGroupRef = usersRef.child(StaticFirebaseSettings.currentUserId).child("notifications").child(notificationKey);
            userGroupRef.removeValue();
        }

        void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }
    }


    public interface OnSaveGroupListener{
        public void onSavedGroup(boolean saved);
    }

    private void onAttachToParentFragment(Fragment fragment){
        try {
            mOnSaveGroupListener = (OnSaveGroupListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(fragment.toString() + " must implement OnUserSelectionSetListener");
        }
    }

}
