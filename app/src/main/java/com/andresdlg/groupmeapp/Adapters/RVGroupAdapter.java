package com.andresdlg.groupmeapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.GroupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_groups_carview, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder groupViewHolder, int position) {
        groupViewHolder.setDetails(context,groups.get(position).getName(),groups.get(position).getImageUrl(),groups.get(position).getGroupKey());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView groupName;
        ImageView groupPhoto;

        GroupViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cvGroups);
            groupName = itemView.findViewById(R.id.tvGroupName);
            groupPhoto = itemView.findViewById(R.id.ivGroupPhoto);
        }

        void setDetails(final Context context, final String name, final String imageUrl, final String groupKey) {
            groupName.setText(name);

            Glide.with(context)
                    .load(imageUrl)
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
                    .into(groupPhoto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i = new Intent(context, GroupActivity.class);
                    i.putExtra("groupImage", imageUrl);
                    i.putExtra("groupName",name);
                    i.putExtra("groupKey",groupKey);

                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation((Activity) context, groupName,"groupName");
                    //context.startActivity(i,options.toBundle());
                    context.startActivity(i);
                }
            });
        }
    }
}

