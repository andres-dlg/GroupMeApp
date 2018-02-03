package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.DialogFragments.HeaderDialogFragment;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.SearchContactsActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 01/02/18.
 */

public class RVGroupAddContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static int BUTTONTYPE = 0;
    private static int CONTACTTYPE = 0;

    private List<Users> users;
    private Context context;

    public RVGroupAddContactAdapter(List<Users> users, Context context){
        this.users = users;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if(viewType == BUTTONTYPE){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_groups_dialog_add_contact_button, parent, false);
            return new ButtonAddContactViewHolder(v);
        }else{
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list, parent, false);
            return new GroupAddContactViewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupAddContactViewHolder) {
            ((GroupAddContactViewHolder) holder).setDetails(context,users.get(position).getName(),users.get(position).getAlias(),users.get(position).getImageURL(),users.get(position).getUserid());
        }else if (holder instanceof ButtonAddContactViewHolder) {
            ((ButtonAddContactViewHolder) holder).setListener(context);
        }
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == BUTTONTYPE ? BUTTONTYPE : CONTACTTYPE;
    }

    public static class ButtonAddContactViewHolder extends RecyclerView.ViewHolder {
        View mView;

        ButtonAddContactViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        private void setListener(final Context context){
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context,"Agregar contacto al grupo",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, SearchContactsActivity.class);
                    context.startActivity(i);
                }
            });
        }

    }

    public static class GroupAddContactViewHolder extends RecyclerView.ViewHolder {

        View mView;

        GroupAddContactViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(final Context context, String contactName, final String contactAlias, final String contactPhoto, final String id){
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);

            CircleImageView mContactAdd = null;
            //Reviso si ya se envio la solicitud al usuario. En ese caso cambio el icono y deshabilito envio

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            //new Picasso.Builder(context).downloader(new OkHttpDownloader(context,Integer.MAX_VALUE)).build().load(contactPhoto).placeholder(R.drawable.progress_animation).into(mContactPhoto);

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
        }
    }
}
