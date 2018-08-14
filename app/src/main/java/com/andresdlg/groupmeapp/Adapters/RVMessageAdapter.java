package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.ConversationFirebase;
import com.andresdlg.groupmeapp.Entities.Message;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ContextValidator;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by andresdlg on 21/01/18.
 */

public class RVMessageAdapter extends RecyclerView.Adapter<RVMessageAdapter.MessageViewHolder>{


    private DatabaseReference userRef;

    private ValueEventListener userValueEventListener;

    private List<ConversationFirebase> conversations;
    private Context context;

    private PrettyTime prettyTime;

    public RVMessageAdapter(List<ConversationFirebase> conversations, Context context){
        this.conversations = conversations;
        this.context = context;
        this.prettyTime = new PrettyTime(new Locale("es"));
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_messages_list, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") final int position) {

        Collections.sort(conversations, new Comparator<ConversationFirebase>() {
            @Override
            public int compare(ConversationFirebase c1, ConversationFirebase c2) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(c1.getMessage().getTimestamp());
                Calendar cl2 = Calendar.getInstance();
                cl2.setTimeInMillis(c2.getMessage().getTimestamp());
                return cl2.compareTo(c);
            }
        });

        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"hola",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(view.getContext(), ChatActivity.class);
                ArrayList<String> arrayList = new ArrayList<>();
                if(conversations.get(position).getUser1().equals(StaticFirebaseSettings.currentUserId)){
                    arrayList.add(conversations.get(position).getUser2());
                }else{
                    arrayList.add(conversations.get(position).getUser1());
                }
                i.putExtra("conversationKey",conversations.get(position).getId());
                i.putExtra("contactIds",arrayList);
                context.startActivity(i);
            }
        });

        DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference("Conversations").child(conversations.get(position).getId());
        ValueEventListener conversationEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String idUser;
                if (dataSnapshot.child("user1").getValue().toString().equals(StaticFirebaseSettings.currentUserId)) {
                    idUser = dataSnapshot.child("user2").getValue().toString();
                } else {
                    idUser = dataSnapshot.child("user1").getValue().toString();
                }

                userRef = FirebaseDatabase.getInstance().getReference("Users").child(idUser);
                userValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(conversations.size() > 0){
                            Users u = dataSnapshot.getValue(Users.class);
                            messageViewHolder.userAlias.setText(String.format("@%s", u.getAlias()));
                            messageViewHolder.setPhoto(context, u.getImageURL());
                            messageViewHolder.messageText.setText(conversations.get(position).getMessage().getText());

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(conversations.get(position).getMessage().getTimestamp());
                            String date = prettyTime.format(calendar);
                            messageViewHolder.messageDate.setText(date);

                            messageViewHolder.setNewMessageIndicator(conversations.get(position));
                            //removeUsersListener();
                            //removeConversationListener();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                userRef.addValueEventListener(userValueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        conversationRef.addListenerForSingleValueEvent(conversationEventListener);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userAlias;
        ImageView userPhoto;
        ImageView newMessagesIndicator;
        TextView messageText;
        TextView messageDate;
        View mView;

        MessageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userAlias = itemView.findViewById(R.id.userAlias);
            userAlias.setSelected(true);
            userPhoto = itemView.findViewById(R.id.contact_photo);
            messageText = itemView.findViewById(R.id.messageText);
            messageDate = itemView.findViewById(R.id.date);
            newMessagesIndicator = itemView.findViewById(R.id.newMessageIndicator);
        }

        public void setPhoto(Context context, final String imageURL) {
            if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                Glide.with(itemView.getContext())
                        .load(imageURL)
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
                        .into(userPhoto);
            }

        }

        public void setNewMessageIndicator(ConversationFirebase conversation) {
            boolean areThereNewMessages = false;
            for(Message m : conversation.getMessages()){
                boolean existo = false;
                for(String s : m.getSeenBy()){
                    if(s.equals(StaticFirebaseSettings.currentUserId)){
                        existo = true;
                        break;
                    }
                }
                if(!existo){
                    areThereNewMessages = true;
                    break;
                }
            }

            if(areThereNewMessages){
                newMessagesIndicator.setVisibility(View.VISIBLE);
            }else{
                newMessagesIndicator.setVisibility(View.GONE);
            }

        }
    }
}
