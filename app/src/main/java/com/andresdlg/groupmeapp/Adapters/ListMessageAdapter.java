package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Conversation;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 11/04/18.
 */

public class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Conversation conversation;
    private String userToUrl;
    private String currentUserUrl;
    private String chatType;

    public ListMessageAdapter(Context context, Conversation conversation, String userToUrl, String currentUserUrl, String chatType) {
        this.context = context;
        this.conversation = conversation;
        this.userToUrl = userToUrl;
        this.currentUserUrl = currentUserUrl;
        this.chatType = chatType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if(chatType.equals("User")){
            if (holder instanceof ItemMessageFriendHolder) {
                ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageFriendHolder) holder).setAvatar(context,userToUrl);
            } else if (holder instanceof ItemMessageUserHolder) {
                ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageUserHolder) holder).setAvatar(context,currentUserUrl);
            }
        }else{
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(conversation.getListMessageData().get(position).getIdSender());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    userRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            ///TODO: IR A BUSCAR EL USUARIO Y LUEGO REPETIR EL CODIGO DEL IF UNA VEZ ENCONTRADO
        }


    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).getIdSender().equals(StaticFirebaseSettings.currentUserId) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }


    class ItemMessageUserHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        public CircleImageView avatar;

        public ItemMessageUserHolder(View itemView) {
            super(itemView);
            txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
            avatar = (CircleImageView) itemView.findViewById(R.id.imageView2);
        }

        public void setAvatar(final Context context, final String currentUserUrl) {
            Picasso.with(context).load(currentUserUrl).into(avatar, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(currentUserUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(avatar, new Callback() {
                                @Override
                                public void onSuccess() {

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

    class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        public CircleImageView avata;
        ;

        public ItemMessageFriendHolder(View itemView) {
            super(itemView);
            txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
            avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
        }

        public void setAvatar(final Context context, final String userToUrl) {
            Picasso.with(context).load(userToUrl).into(avata, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(userToUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(avata, new Callback() {
                                @Override
                                public void onSuccess() {

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