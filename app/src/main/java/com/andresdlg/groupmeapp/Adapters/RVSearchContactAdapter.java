package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ContextValidator;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/01/18.
 */

public class RVSearchContactAdapter extends RecyclerView.Adapter<RVSearchContactAdapter.ContactsViewHolder> implements Filterable {

    private List<Users> users;
    private List<Users> usersFiltered;
    private Context context;
    private LayoutInflater inflater;
    private UsersAdapterListener listener;
    private String groupKey;
    private Map<String, String> usersIdsWIthStatus;
    //private SparseBooleanArray mSelectedItemsIds;
    private List<String> mSelectedItemsIds;

    private Drawable mDrawablePending;


    public interface UsersAdapterListener {
        void onContactSelected(Users user);
    }

    public RVSearchContactAdapter(List<Users> users, Context context, UsersAdapterListener listener, String groupKey, Map<String, String> usersIdsWIthStatus){
        this.users = users;
        this.usersFiltered = users;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.groupKey = groupKey;
        this.usersIdsWIthStatus = usersIdsWIthStatus;
        //mSelectedItemsIds = new SparseBooleanArray();
        mSelectedItemsIds = new ArrayList<>();
        mDrawablePending = context.getResources().getDrawable(R.drawable.check_circle);
        mDrawablePending.setTint(context.getResources().getColor(R.color.colorPrimaryDark));

    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.fragment_contact_request_list, parent, false);
        return new ContactsViewHolder(v, usersFiltered, listener,groupKey);
    }

    @Override
    public void onBindViewHolder(final ContactsViewHolder contactsViewHolder, final int position) {
        Users u = usersFiltered.get(position);
        contactsViewHolder.setDetails(context,u.getName(),u.getAlias(),u.getImageURL(),u.getUserid());
        //Tener en cuenta si esta seleccionado
        /** Change background color of the selected items in list view  **/
        /*contactsViewHolder.itemView
                .setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
                        : Color.TRANSPARENT);*/
        /*contactsViewHolder.itemView
                .setBackgroundColor(mSelectedItemsIds.contains(usersFiltered.get(position).getUserid()) ? 0x9934B5E4
                        : Color.TRANSPARENT);*/

        if(mSelectedItemsIds.contains(usersFiltered.get(position).getUserid())){
            contactsViewHolder.setDrawable(true,mDrawablePending);
        }else{
            contactsViewHolder.setDrawable(false,null);
        }
    }

    @Override
    public int getItemCount() {
        return usersFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    usersFiltered = users;
                } else {
                    List<Users> filteredList = new ArrayList<>();
                    for (Users row : users) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    usersFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = usersFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                usersFiltered = (ArrayList<Users>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    //Toggle selection methods
    public void toggleSelection(int position) {
        //selectView(position, !mSelectedItemsIds.get(position));
        if(position != -1){
            String value = usersIdsWIthStatus.get(usersFiltered.get(position).getUserid());
            if(value == null){
                String id = usersFiltered.get(position).getUserid();
                if(!mSelectedItemsIds.contains(id)){
                    mSelectedItemsIds.add(id);
                }else{
                    mSelectedItemsIds.remove(id);
                }
                notifyDataSetChanged();
            }
        }
    }

    //Remove selected selections
    public void removeSelection() {
        //mSelectedItemsIds = new SparseBooleanArray();
        mSelectedItemsIds = new ArrayList<>();
        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }



    //Return all selected ids
    public ArrayList<String> getSelectedIds() {
        return (ArrayList<String>) mSelectedItemsIds;
    }
    /*public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }*/


    static class ContactsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        private List<Users> usersFiltered;
        private UsersAdapterListener listener;
        private String groupKey;
        ImageButton btn;

        ContactsViewHolder(View itemView, List<Users> usersFiltered, UsersAdapterListener listener, String groupKey) {
            super(itemView);
            mView = itemView;
            this.usersFiltered = usersFiltered;
            this.listener = listener;
            this.groupKey = groupKey;
        }

        void setDetails(final Context context, final String contactName, final String contactAlias, final String contactPhoto, final String iduser) {
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);
            final RelativeLayout rl = mView.findViewById(R.id.rl);

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

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

            if(iduser.equals(StaticFirebaseSettings.currentUserId)){
                mContactName.setText("Tú");
            }else{
                mContactName.setText(contactName);
            }
            mContactName.setSelected(true);

            if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                Glide.with(itemView.getContext())
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
            }

            btn = mView.findViewById(R.id.btn_menu);

            if(groupKey != null){
                FirebaseDatabase.getInstance().getReference("Users").child(iduser).child("groups").child(groupKey).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals(GroupStatus.PENDING.toString())){

                            View.OnClickListener clickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(context, "Ya has enviado invitación a este contacto", Toast.LENGTH_SHORT).show();
                                }
                            };
                            Drawable mDrawablePending = ContextCompat.getDrawable(context,R.drawable.ic_timer_sand_black_24dp);
                            btn.setImageDrawable(mDrawablePending);
                            btn.setVisibility(View.VISIBLE);
                            btn.setOnClickListener(clickListener);
                            rl.setOnClickListener(clickListener);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        void setDrawable(boolean selected, Drawable drawable) {
            if(selected){
                btn.setImageDrawable(drawable);
                btn.setVisibility(View.VISIBLE);
            }else{
                btn.setVisibility(View.GONE);
            }
        }
    }
}
