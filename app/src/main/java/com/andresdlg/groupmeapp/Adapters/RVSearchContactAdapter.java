package com.andresdlg.groupmeapp.Adapters;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/01/18.
 */

public class RVSearchContactAdapter extends RecyclerView.Adapter<RVSearchContactAdapter.ContactsViewHolder> implements Filterable {

    private List<Users> users;
    public List<Users> usersFiltered;
    private Context context;
    private LayoutInflater inflater;
    private UsersAdapterListener listener;
    //private SparseBooleanArray mSelectedItemsIds;
    private List<String> mSelectedItemsIds;


    Drawable mDrawablePending;


    public interface UsersAdapterListener {
        void onContactSelected(Users user);

    }

    public RVSearchContactAdapter(List<Users> users, Context context, UsersAdapterListener listener){
        this.users = users;
        this.usersFiltered = users;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listener = listener;
        //mSelectedItemsIds = new SparseBooleanArray();
        mSelectedItemsIds = new ArrayList<>();
        mDrawablePending = context.getResources().getDrawable(R.drawable.check_circle);
        mDrawablePending.setTint(context.getResources().getColor(R.color.colorPrimaryDark));

    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.fragment_contact_request_list, parent, false);
        return new ContactsViewHolder(v, usersFiltered, listener);
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
        String id = usersFiltered.get(position).getUserid();
        if(!mSelectedItemsIds.contains(id)){
            mSelectedItemsIds.add(id);
        }else{
            mSelectedItemsIds.remove(id);
        }
        notifyDataSetChanged();
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
        CircleImageView btn;

        ContactsViewHolder(View itemView, List<Users> usersFiltered, UsersAdapterListener listener) {
            super(itemView);
            mView = itemView;
            this.usersFiltered = usersFiltered;
            this.listener = listener;
        }

        void setDetails(final Context context, final String contactName, final String contactAlias, final String contactPhoto, final String iduser) {
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
                                            Log.v("Picasso", "No se ha podido cargar la foto");
                                        }
                                    });
                        }
                    });

            btn = mView.findViewById(R.id.btn_menu);


            /*mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onContactSelected(usersFiltered.get(getAdapterPosition()));
                }
            });*/
        }

        ///TODO: REVISAR ESTE METODO QUE NO ANDA Y BORRAR SEARCH ACTIVITY
        public void setDrawable(boolean selected,Drawable drawable) {
            if(selected){
                btn.setImageDrawable(drawable);
                btn.setVisibility(View.VISIBLE);
            }else{
                btn.setVisibility(View.GONE);
            }
        }
    }
}
