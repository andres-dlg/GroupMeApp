package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 11/04/18.
 */

public class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private DatabaseReference userRef;
    private ValueEventListener userValueEventListener;

    private Context context;
    private Conversation conversation;
    private String userToUrl;
    private String currentUserUrl;
    private String chatType;
    private String conversationKey;


    public ListMessageAdapter(Context context, Conversation conversation, String userToUrl, String currentUserUrl, String chatType, String conversationKey) {
        this.context = context;
        this.conversation = conversation;
        this.userToUrl = userToUrl;
        this.currentUserUrl = currentUserUrl;
        this.chatType = chatType;
        this.conversationKey = conversationKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if(chatType.equals("User")){
            if (holder instanceof ItemMessageFriendHolder) {
                ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageFriendHolder) holder).setAvatar(context,userToUrl);
            } else if (holder instanceof ItemMessageUserHolder) {
                ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageUserHolder) holder).setAvatar(context,currentUserUrl);
            }
        }else{
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(conversation.getListMessageData().get(position).getIdSender());
            userValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if (holder instanceof ItemMessageFriendHolder) {
                        ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                        ((ItemMessageFriendHolder) holder).setAvatar(context,u.getImageURL());
                    } else if (holder instanceof ItemMessageUserHolder) {
                        ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                        ((ItemMessageUserHolder) holder).setAvatar(context,u.getImageURL());
                    }
                    removeListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userRef.addListenerForSingleValueEvent(userValueEventListener);
        }
    }

    private void removeListener() {
        userRef.removeEventListener(userValueEventListener);
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).getIdSender().equals(StaticFirebaseSettings.currentUserId) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }


    public void updateAllMessagesToSeen() {
        for(Message m : conversation.getListMessageData()){
            List<String> seenBy = m.getSeenBy();

            boolean existo = false;
            for(String s : seenBy){
                if(s.equals(StaticFirebaseSettings.currentUserId)){
                    existo = true;
                }
            }
            if(!existo){
                seenBy.add(StaticFirebaseSettings.currentUserId);
                FirebaseDatabase.getInstance().getReference("Conversations").child(conversationKey).child("messages").child(m.getId()).child("seenBy").setValue(seenBy);
            }
        }
        notifyDataSetChanged();
    }


    class ItemMessageUserHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        CircleImageView avatar;

        ItemMessageUserHolder(View itemView) {
            super(itemView);
            txtContent =  itemView.findViewById(R.id.textContentUser);
            avatar =  itemView.findViewById(R.id.imageView2);
        }

        public void setAvatar(final Context context, final String currentUserUrl) {
            Glide.with(context).load(currentUserUrl).into(avatar);
        }
    }

    class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        CircleImageView avata;


        ItemMessageFriendHolder(View itemView) {
            super(itemView);
            txtContent =  itemView.findViewById(R.id.textContentFriend);
            avata =  itemView.findViewById(R.id.imageView3);
        }

        public void setAvatar(final Context context, final String userToUrl) {
            Glide.with(context)
                    .load(userToUrl)
                    .into(avata);
        }
    }
}