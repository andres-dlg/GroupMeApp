package com.andresdlg.groupmeapp.Adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;

import java.util.List;


/**
 * Created by andresdlg on 11/07/17.
 */

public class RVGroupAdapter extends RecyclerView.Adapter<RVGroupAdapter.GroupViewHolder> {

    List<Group> groups;

    public RVGroupAdapter(List<Group> groups){
        this.groups = groups;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_groups_carview, parent, false);
        GroupViewHolder gvh = new GroupViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder groupViewHolder, int position) {
        groupViewHolder.groupName.setText(groups.get(position).getName());
        groupViewHolder.groupPhoto.setImageResource(groups.get(position).getPhotoId());
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

    }
}

