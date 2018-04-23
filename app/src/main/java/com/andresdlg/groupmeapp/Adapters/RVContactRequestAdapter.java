package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            Picasso.with(context)
                    .load(contactPhoto)
                    .into(mContactPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(contactPhoto)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(mContactPhoto, new Callback() {
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

            CircleImageView btn = mView.findViewById(R.id.btn_menu);
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
                                    Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.reject:
                                    rejectRequest(iduser);
                                    Toast.makeText(context,"rechazar"+contactName, Toast.LENGTH_SHORT).show();
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
            DatabaseReference userToRef = FirebaseDatabase.getInstance().getReference("Users").child(iduser).child("friends");
            Map<String,Object> newFriend = new HashMap<>();
            newFriend.put("status", FriendshipStatus.ACCEPTED);
            userToRef.child(StaticFirebaseSettings.currentUserId).updateChildren(newFriend);

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
            Map<String,Object> newFriend2 = new HashMap<>();
            newFriend2.put("status", FriendshipStatus.ACCEPTED);
            currentUserRef.child(iduser).updateChildren(newFriend2);


        }

        private void rejectRequest(String iduser) {
            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
            Map<String,Object> newFriend2 = new HashMap<>();
            newFriend2.put("status", FriendshipStatus.REJECTED);
            currentUserRef.child(iduser).updateChildren(newFriend2);
        }
    }
}
