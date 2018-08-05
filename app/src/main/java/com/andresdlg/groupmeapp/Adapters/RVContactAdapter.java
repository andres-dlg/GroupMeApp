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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ConversationStatus;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by andresdlg on 17/01/18.
 */

public class RVContactAdapter extends RecyclerView.Adapter<RVContactAdapter.ContactsViewHolder>{

    private List<Users> users;
    private Context context;
    private boolean fromNewsFragment;

    public RVContactAdapter(List<Users> users, Context context, boolean fromNewsFragment){
        this.users = users;
        this.context = context;
        this.fromNewsFragment = fromNewsFragment;
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
        contactsViewHolder.setDetails(context,u.getName(),u.getAlias(),u.getImageURL(),u.getUserid(),fromNewsFragment);
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

        DatabaseReference conversationRef;
        ValueEventListener conversationEventListener;

        View mView;
        String conversationKey;

        ContactsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(final Context context, String contactName, final String contactAlias, final String contactPhoto, final String iduser, final boolean fromNewsFragment){
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

            if(iduser.equals(StaticFirebaseSettings.currentUserId)){
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                btn.setVisibility(View.GONE);
                rl.setLayoutParams(param);
            }

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();

                    popupMenu.getMenuInflater().inflate(R.menu.fragment_contact_menu, menu);
                    popupMenu.getMenu().removeItem(R.id.add_to_group);

                    if(fromNewsFragment){
                        //popupMenu.getMenu().removeItem(R.id.add_to_group);
                        popupMenu.getMenu().removeItem(R.id.delete);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.message:
                                    //ENVIAR MENSAJE
                                    sendMessage(iduser, context);
                                    break;
                                case R.id.add_to_group:
                                    //AGREGAR A GRUPO
                                    break;
                                case R.id.delete:
                                    //ELIMINAR CONTACTO
                                    deleteContact(iduser);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        private void sendMessage(final String iduser, final Context context) {

            final String currentUserId = StaticFirebaseSettings.currentUserId;

            conversationKey = currentUserId+iduser;

            conversationRef = FirebaseDatabase.getInstance().getReference("Conversations");
            conversationEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getKey().equals(currentUserId+iduser)){
                            conversationKey = currentUserId+iduser;
                            break;
                        }else if(data.getKey().equals(iduser+currentUserId)){
                            conversationKey = iduser+currentUserId;
                            break;
                        }
                    }

                    Map<String,Object> map = new HashMap<>();
                    map.put("user1",currentUserId);
                    map.put("user2",iduser);
                    map.put("id",conversationKey);
                    conversationRef.child(conversationKey).updateChildren(map);

                    Map<String,Object> map2 = new HashMap<>();
                    map2.put("status",ConversationStatus.UNSEEN);
                    map2.put("user1",currentUserId);
                    map2.put("user2",iduser);
                    map2.put("id",conversationKey);
                    DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(iduser).child("conversation");
                    userToRef.child(conversationKey).updateChildren(map2);

                    Map<String,Object> map3 = new HashMap<>();
                    map3.put("status",ConversationStatus.SEEN);
                    map3.put("user1",currentUserId);
                    map3.put("user2",iduser);
                    map3.put("id",conversationKey);
                    DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("conversation");
                    currentUserRef.child(conversationKey).updateChildren(map3);

                    Intent intent = new Intent(context,ChatActivity.class);
                    ArrayList<String> contactIds = new ArrayList<>();
                    contactIds.add(iduser);
                    intent.putExtra("contactIds",contactIds);
                    intent.putExtra("conversationKey",conversationKey);
                    context.startActivity(intent);

                    removeListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            conversationRef.addListenerForSingleValueEvent(conversationEventListener);
        }

        private void removeListener() {
            conversationRef.removeEventListener(conversationEventListener);
        }

        private void deleteContact(String iduser) {
            DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(iduser).child("friends");
            Map<String,Object> newFriend = new HashMap<>();
            newFriend.put("status", FriendshipStatus.REJECTED);
            userToRef.child(StaticFirebaseSettings.currentUserId).updateChildren(newFriend);

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
            Map<String,Object> newFriend2 = new HashMap<>();
            newFriend2.put("status", FriendshipStatus.REJECTED);
            currentUserRef.child(iduser).updateChildren(newFriend2);
        }
    }

}
