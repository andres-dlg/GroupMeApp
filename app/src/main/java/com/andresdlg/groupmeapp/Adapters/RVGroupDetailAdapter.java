package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/03/18.
 */

public class RVGroupDetailAdapter extends RecyclerView.Adapter<RVGroupDetailAdapter.GroupDetailViewHolder>{

    private DatabaseReference usersRef;
    private DatabaseReference groupsRef;
    private List<Users> usersList;
    private Map<String, String> usersRoles;
    private String groupKey;
    private Context context;

    public RVGroupDetailAdapter(List<Users> usersList, Map<String, String> usersRoles,String groupKey, Context context){
        this.usersList = usersList;
        this.usersRoles = usersRoles;
        this.groupKey = groupKey;
        this.context = context;
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
    }

    @Override
    public GroupDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_group_details_list_item, parent, false);
        return new GroupDetailViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final GroupDetailViewHolder groupDetailViewHolder, @SuppressLint("RecyclerView") final int position) {
        Users u = usersList.get(position);
        String rol = null;
        for(Map.Entry<String, String> entry : usersRoles.entrySet()) {
            if(u.getUserid().equals(entry.getKey())){
                rol = entry.getValue();
            }
        }
        groupDetailViewHolder.setDetails(context,u.getName(),u.getAlias(),rol,u.getImageURL(),u.getUserid());
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public class GroupDetailViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public GroupDetailViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(final Context context, String contactName, final String contactAlias,String rol, final String contactPhoto, final String iduser){
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.tvUserName);
            TextView mContactAlias = mView.findViewById(R.id.tvUserAlias);
            final TextView mContactRol = mView.findViewById(R.id.tvRol);

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

            DatabaseReference ref = groupsRef.child(groupKey).child("members").child(iduser);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String rol = dataSnapshot.getValue().toString();
                    if(rol.equals("ADMIN")){
                        mContactRol.setText("ADMINISTRADOR");
                    }else{
                        mContactRol.setText("MIEMBRO");
                    }
                    mContactRol.setSelected(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
    }
}
