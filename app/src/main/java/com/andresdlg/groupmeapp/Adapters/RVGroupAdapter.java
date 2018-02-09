package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by andresdlg on 11/07/17.
 */

public class RVGroupAdapter extends RecyclerView.Adapter<RVGroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groups;

    public RVGroupAdapter(Context context, List<Group> groups){
        this.context = context;
        this.groups = groups;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_groups_carview, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder groupViewHolder, int position) {
        groupViewHolder.setDetails(context,groups.get(position).getName(),groups.get(position).getImageUrl());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView groupName;
        ImageView groupPhoto;

        GroupViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cvGroups);
            groupName = itemView.findViewById(R.id.tvGroupName);
            groupPhoto = itemView.findViewById(R.id.ivGroupPhoto);
        }

        public void setDetails(final Context context, String name, final String imageUrl) {
            groupName.setText(name);


            Picasso.with(context).load(imageUrl).into(groupPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                }
                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(imageUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(groupPhoto, new Callback() {
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

