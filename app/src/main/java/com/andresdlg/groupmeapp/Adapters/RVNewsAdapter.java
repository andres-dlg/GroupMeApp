package com.andresdlg.groupmeapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.Entities.Post;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.GroupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by andresdlg on 11/07/17.
 */

public class RVNewsAdapter extends RecyclerView.Adapter<RVNewsAdapter.NewsViewHolder> {

    private Context context;
    private List<Post> posts;
    private boolean isInGroup;
    private PrettyTime prettyTime;
    private String groupKey;
    private DatabaseReference groupRolRef;

    public RVNewsAdapter(Context context, List<Post> posts,boolean isInGroup, String groupKey){
        this.context = context;
        this.posts = posts;
        this.isInGroup = isInGroup;
        this.prettyTime = new PrettyTime(new Locale("es"));
        this.groupKey = groupKey;
        if(isInGroup){
            groupRolRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("members").child(StaticFirebaseSettings.currentUserId);
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_new_post_cardview, parent, false);
        return new NewsViewHolder(v,groupRolRef);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder groupViewHolder, int position) {
        Post post = posts.get(position);
        groupViewHolder.setDetails(context,post.getPostId(),post.getText(),post.getTime(),post.getUserId(),post.getGroupName(),post.getSeenBy(), prettyTime, isInGroup, post.getGroupKey(), post.getLikeBy());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setGroups(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void setPostsAsSeen() {
        for(int i = 0; i<posts.size(); i++){
            posts.get(i).getSeenBy().add(StaticFirebaseSettings.currentUserId);
            notifyItemChanged(i);
        }
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        CircleImageView userProfilePhoto;
        TextView userName;
        TextView postTime;
        TextView postText;
        TextView groupText;
        ImageButton btnMenu;
        ImageButton likeBtn;
        ImageView newPostIndicator;
        TextView likesCountTv;

        DatabaseReference groupRolRef;


        NewsViewHolder(View itemView,DatabaseReference groupRolRef) {
            super(itemView);
            cv = itemView.findViewById(R.id.cvPost);
            userProfilePhoto = itemView.findViewById(R.id.userProfilePhoto);
            userName = itemView.findViewById(R.id.userName);
            postTime = itemView.findViewById(R.id.postTime);
            postText = itemView.findViewById(R.id.postText);
            groupText = itemView.findViewById(R.id.group);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            likeBtn = itemView.findViewById(R.id.likeBy);
            likesCountTv = itemView.findViewById(R.id.likesCountTv);
            newPostIndicator = itemView.findViewById(R.id.newPostIndicator);
            this.groupRolRef = groupRolRef;
        }

        void setDetails(final Context context, final String postId, final String text, final long time, String userId, final String groupName, final List<String> seenBy, final PrettyTime prettyTime, final boolean isInGroup, final String groupKey, List<String> likeBy) {

            FirebaseDatabase.getInstance().getReference("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //TRAIGO AL USUARIO
                    final Users u = dataSnapshot.getValue(Users.class);

                    //CARGO SU FOTO DE PERFIL
                    Glide.with(context)
                            .load(u.getImageURL())
                            .into(userProfilePhoto);

                    //CARGO SU NOMBRE
                    userName.setText(u.getName());

                    //CARGO FECHA DEL POST
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    postTime.setText(prettyTime.format(calendar));

                    //CARGO EL TEXTO DEL POST
                    postText.setText(text);

                    if(isInGroup){
                        groupText.setVisibility(View.GONE);
                    }else{
                        groupText.setText(groupName);
                    }

                    //ESCONDO O MUESTRO EL PUNTO ROJO DE NOTICIA NUEVA
                    if(seenBy!=null){
                        boolean iHaveSeenThisPost = false;
                        for(String entry: seenBy) {
                            if(entry.equals(StaticFirebaseSettings.currentUserId)){
                                iHaveSeenThisPost = true;
                            }
                        }
                        if(iHaveSeenThisPost){
                            newPostIndicator.setVisibility(View.GONE);
                        }else{
                            newPostIndicator.setVisibility(View.VISIBLE);
                        }
                    }else{
                        newPostIndicator.setVisibility(View.VISIBLE);
                    }

                    //SETEO EL BOTON DE MENU SI EL POST ES MIO O SI SOY ADMINISTRADOR DEL GRUPO
                    //SOLO HAGO ESTO SI ESTOY DENTRO DE UN GRUPO (Por eso el if)

                    if(isInGroup){
                        //1) Busco el mi rol
                        final String[] myRol = {""};
                        groupRolRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                myRol[0] = dataSnapshot.getValue().toString();

                                //2)Seteo el menu
                                if(u.getUserid().equals(StaticFirebaseSettings.currentUserId) || myRol[0].equals(Roles.ADMIN.toString())){
                                    btnMenu.setVisibility(View.VISIBLE);
                                    btnMenu.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            //3)Seteo el menu de opciones
                                            PopupMenu popupMenu = new PopupMenu(context, view);
                                            Menu menu = popupMenu.getMenu();
                                            popupMenu.getMenuInflater().inflate(R.menu.fragment_news_post_menu, menu);
                                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                @Override
                                                public boolean onMenuItemClick(MenuItem menuItem) {
                                                    switch (menuItem.getItemId()){
                                                        case R.id.delete:
                                                            deletePost(postId,getAdapterPosition());
                                                            return true;
                                                        default:
                                                            return false;
                                                    }
                                                }
                                            });
                                            popupMenu.show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(likeBy!= null && likeBy.size()>0){
                if(likeBy.size()==1){
                    likesCountTv.setText(String.format("%s vez marcado como visto", String.valueOf(likeBy.size())));
                }else{
                    likesCountTv.setText(String.format("%s veces marcado como visto", String.valueOf(likeBy.size())));
                }
                likeBtn.setColorFilter(Color.argb(255, 63, 81, 181));
            }

            final List<String> likesIds = new ArrayList<>();
            if(likeBy != null){
                likesIds.addAll(likeBy);
            }

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean existo = false;

                    for(String id : likesIds){
                        if(id.equals(StaticFirebaseSettings.currentUserId)){
                            likesIds.remove(StaticFirebaseSettings.currentUserId);
                            existo = true;
                        }
                    }

                    if(!existo){
                        likesIds.add(StaticFirebaseSettings.currentUserId);
                    }


                    if(likesIds.size() == 1){
                        likesCountTv.setText(String.format("%s vez marcado como visto", String.valueOf(likesIds.size())));
                        likeBtn.setColorFilter(Color.argb(255, 63, 81, 181));
                    }else if(likesIds.size() == 0){
                        likesCountTv.setText(null);
                        likeBtn.setColorFilter(Color.argb(255, 158, 158, 158));
                    }else{
                        likesCountTv.setText(String.format("%s veces marcado como visto", String.valueOf(likesIds.size())));
                    }

                    FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("posts").child(postId).child("likeBy").setValue(likesIds);
                }
            });
        }
    }

    private void deletePost(String postId, final int position) {
        FirebaseDatabase
                .getInstance()
                .getReference("Groups")
                .child(groupKey)
                .child("posts")
                .child(postId)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                posts.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,posts.size());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error al eliminar publicación", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

