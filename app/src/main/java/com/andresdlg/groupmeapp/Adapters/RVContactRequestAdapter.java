package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/01/18.
 */

public class RVContactRequestAdapter extends RecyclerView.Adapter<RVContactRequestAdapter.ContactsViewHolder>{

    private List<Users> users;
    private Context context;

    public RVContactRequestAdapter(List<Users> users, Context context){
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_contact_request_list, parent, false);
        return new ContactsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, final int position) {
        Users u = users.get(position);
        contactsViewHolder.setDetails(context,u.getName(),u.getAlias(),u.getImageURL(),u.getUserid());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        ContactsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(final Context context, final String contactName, final String contactAlias, final String contactPhoto, final String iduser){
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);
            RelativeLayout rl = mView.findViewById(R.id.rl);

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userProfileIntent = new Intent(context, UserProfileSetupActivity.class);
                    userProfileIntent.putExtra("iduser",iduser);
                    Pair<View, String> p1 = Pair.create((View)mContactPhoto, "userPhoto");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((AppCompatActivity)context, p1);
                    context.startActivity(userProfileIntent, options.toBundle());
                }
            });

            Glide.with(context)
                    .load(contactPhoto)
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
                    .into(mContactPhoto);

            ImageButton btn = mView.findViewById(R.id.btn_menu);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();

                    popupMenu.getMenuInflater().inflate(R.menu.fragment_contact_request_menu, menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.accept:
                                    acceptRequest(iduser);
                                    Toast.makeText(context,"Has aceptado la invitación de "+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.reject:
                                    rejectRequest(iduser);
                                    Toast.makeText(context,"Has rechazado la invitación de"+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        private void acceptRequest(String iduser) {

            final DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(iduser);

            DatabaseReference userToFriendsRef = userToRef.child("friends");
            Map<String,Object> newFriend = new HashMap<>();
            newFriend.put("status", FriendshipStatus.ACCEPTED);
            userToFriendsRef.child(StaticFirebaseSettings.currentUserId).updateChildren(newFriend);

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId);

            DatabaseReference currentUserFriendsRef = currentUserRef.child("friends");
            Map<String,Object> newFriend2 = new HashMap<>();
            newFriend2.put("status", FriendshipStatus.ACCEPTED);
            currentUserFriendsRef.child(iduser).updateChildren(newFriend2);

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);

                    DatabaseReference userToNotifications = userToRef.child("notifications");
                    String notificationKey = userToNotifications.push().getKey();
                    Map<String,Object> notification = new HashMap<>();
                    notification.put("notificationKey",notificationKey);
                    notification.put("title","Invitacion aceptada");
                    notification.put("message",u.getName() + " ha aceptado tu solicitud de contacto");
                    notification.put("from", StaticFirebaseSettings.currentUserId);
                    notification.put("state", NotificationStatus.UNREAD);
                    notification.put("date", Calendar.getInstance().getTimeInMillis());
                    notification.put("type", NotificationTypes.FRIENDSHIP_ACCEPTED);

                    userToNotifications.child(notificationKey).setValue(notification);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void rejectRequest(String iduser) {
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
            Map<String,Object> newFriend2 = new HashMap<>();
            newFriend2.put("status", FriendshipStatus.REJECTED);
            currentUserRef.child(iduser).updateChildren(newFriend2);
        }
    }
}
