package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ConversationStatus;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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
    String conversationKey;

    public RVContactAdapter(List<Users> users, Context context){
        this.users = users;
        this.context = context;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_contact_request_list, parent, false);
        return new ContactsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ContactsViewHolder contactsViewHolder, final int position) {
        Users u = users.get(position);
        contactsViewHolder.setDetails(context,u.getName(),u.getAlias(),u.getImageURL(),u.getUserid());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setNotifications(List<Users> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        String conversationKey;

        ContactsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(final Context context, String contactName, final String contactAlias, String contactPhoto, final String iduser){
            CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);

            CircleImageView mContactAdd = null;

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            Picasso.with(context).load(contactPhoto).into(mContactPhoto);

            CircleImageView btn = mView.findViewById(R.id.btn_menu);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();

                    popupMenu.getMenuInflater().inflate(R.menu.fragment_contact_menu, menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.message:
                                    //ENVIAR MENSAJE
                                    sendMessage(iduser, context);
                                    //Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.add_to_group:
                                    //AGREGAR A GRUPO
                                    //rejectRequest(iduser);
                                    //Toast.makeText(context,"rechazar"+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.delete:
                                    //ELIMINAR CONTACTO
                                    deleteContact(iduser, context);
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
            String userToId = iduser;

            conversationKey = currentUserId+iduser;

            final DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference("Conversations");
            conversationRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(iduser).child("conversation");
                    userToRef.child(conversationKey).updateChildren(map2);

                    Map<String,Object> map3 = new HashMap<>();
                    map3.put("status",ConversationStatus.SEEN);
                    DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("conversation");
                    currentUserRef.child(conversationKey).updateChildren(map3);

                    Intent intent = new Intent(context,ChatActivity.class);
                    ArrayList<String> contactIds = new ArrayList<>();
                    contactIds.add(iduser);
                    intent.putExtra("contactIds",contactIds);
                    intent.putExtra("conversationKey",conversationKey);
                    context.startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });





        }

        private void deleteContact(String iduser, final Context context) {
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
