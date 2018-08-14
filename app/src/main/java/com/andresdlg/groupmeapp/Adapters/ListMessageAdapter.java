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
import com.andresdlg.groupmeapp.Utils.ContextValidator;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 11/04/18.
 */

public class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private DatabaseReference usersRef;

    private Context context;
    private Conversation conversation;
    private String userToUrl;
    private String currentUserUrl;
    private String chatType;
    private String conversationKey;

    private List<String> datesToDisplay;
    private Map<String, String> messagesToDisplayDates;

    public ListMessageAdapter(Context context, Conversation conversation, String userToUrl, String currentUserUrl, String chatType, String conversationKey) {
        this.context = context;
        this.conversation = conversation;
        this.userToUrl = userToUrl;
        this.currentUserUrl = currentUserUrl;
        this.chatType = chatType;
        this.conversationKey = conversationKey;
        datesToDisplay = new ArrayList<>();
        messagesToDisplayDates = new HashMap<>();

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //Calendario para mostrar la hora
        long time = conversation.getListMessageData().get(position).getTimestamp();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String timeToShow = calendar.get(Calendar.HOUR_OF_DAY) + ":" + (String.valueOf(calendar.get(Calendar.MINUTE)).length() == 1 ? "0"+String.valueOf(calendar.get(Calendar.MINUTE)) :String.valueOf(calendar.get(Calendar.MINUTE)));

        Calendar cal = Calendar.getInstance();
        for(Message message : conversation.getListMessageData()){
            cal.setTimeInMillis(message.getTimestamp());
            String dateTemp = cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR);
            checkIfDateWasDisplayed(dateTemp);
        }

        for(String dateToShow : datesToDisplay){
            List<Message> tempList = new ArrayList<>();
            for(int i = 0; i<conversation.getListMessageData().size(); i++){
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(conversation.getListMessageData().get(i).getTimestamp());
                String date1 = calendar1.get(Calendar.DAY_OF_MONTH) + "/" + calendar1.get(Calendar.MONTH) + "/" + calendar1.get(Calendar.YEAR);
                if(date1.equals(dateToShow)){
                   tempList.add(conversation.getListMessageData().get(i));
                }
            }
            checkAndSaveTheOldestMessage(tempList,dateToShow);
        }


        if(chatType.equals("User")){
            if (holder instanceof ItemMessageFriendHolder) {
                boolean exists = false;
                for(Map.Entry<String, String> entry: messagesToDisplayDates.entrySet()) {
                    if(entry.getKey().equals(conversation.getListMessageData().get(position).getId())){
                        ((ItemMessageFriendHolder) holder).date.setText(entry.getValue());
                        ((ItemMessageFriendHolder) holder).date.setVisibility(View.VISIBLE);
                        exists = true;
                        break;
                    }
                }
                if(!exists){
                    ((ItemMessageFriendHolder) holder).date.setVisibility(View.GONE);
                }
                ((ItemMessageFriendHolder) holder).textTimeFriend.setText(timeToShow);
                ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageFriendHolder) holder).setAvatar(context,userToUrl);
            } else if (holder instanceof ItemMessageUserHolder) {
                boolean exists = false;
                for(Map.Entry<String, String> entry: messagesToDisplayDates.entrySet()) {
                    if(entry.getKey().equals(conversation.getListMessageData().get(position).getId())){
                        ((ItemMessageUserHolder) holder).date.setText(entry.getValue());
                        ((ItemMessageUserHolder) holder).date.setVisibility(View.VISIBLE);
                        exists = true;
                        break;
                    }
                }
                if(!exists){
                    ((ItemMessageUserHolder) holder).date.setVisibility(View.GONE);
                }
                ((ItemMessageUserHolder) holder).textTimeUser.setText(timeToShow);
                ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageUserHolder) holder).setAvatar(context,currentUserUrl);
            }
        }else{
            if (holder instanceof ItemMessageFriendHolder) {
                boolean exists = false;
                for(Map.Entry<String, String> entry: messagesToDisplayDates.entrySet()) {
                    if(entry.getKey().equals(conversation.getListMessageData().get(position).getId())){
                        ((ItemMessageFriendHolder) holder).date.setText(entry.getValue());
                        ((ItemMessageFriendHolder) holder).date.setVisibility(View.VISIBLE);
                        exists = true;
                        break;
                    }
                }
                if(!exists){
                    ((ItemMessageFriendHolder) holder).date.setVisibility(View.GONE);
                }
                ((ItemMessageFriendHolder) holder).textTimeFriend.setText(timeToShow);
                ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageFriendHolder) holder).setAvatarFromId(context,conversation.getListMessageData().get(position).getIdSender());
            } else if (holder instanceof ItemMessageUserHolder) {
                boolean exists = false;
                for(Map.Entry<String, String> entry: messagesToDisplayDates.entrySet()) {
                    if(entry.getKey().equals(conversation.getListMessageData().get(position).getId())){
                        ((ItemMessageUserHolder) holder).date.setText(entry.getValue());
                        ((ItemMessageUserHolder) holder).date.setVisibility(View.VISIBLE);
                        exists = true;
                        break;
                    }
                }
                if(!exists){
                    ((ItemMessageUserHolder) holder).date.setVisibility(View.GONE);
                }
                ((ItemMessageUserHolder) holder).textTimeUser.setText(timeToShow);
                ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).getText());
                ((ItemMessageUserHolder) holder).setAvatarFromId(context,conversation.getListMessageData().get(position).getIdSender());
            }
            //removeListener();
        }
    }

    private void checkAndSaveTheOldestMessage(List<Message> tempList, String dateToShow){
        String id = tempList.get(0).getId();
        for(int i = 0; i<tempList.size() - 1; i++){
            for (int j = i +1; j < tempList.size() ; j++){
                if(tempList.get(j).getTimestamp()<tempList.get(i).getTimestamp()){
                    id = tempList.get(j).getId();
                }
            }
        }

        Calendar today = Calendar.getInstance();
        String[] fecha = dateToShow.split("/");
        if(today.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(fecha[0]) && today.get(Calendar.MONTH) == Integer.parseInt(fecha[1]) &&  today.get(Calendar.YEAR) == Integer.parseInt(fecha[2]) ) {
            messagesToDisplayDates.put(id, "HOY");
        }else if (today.get(Calendar.DAY_OF_MONTH)-1 == Integer.parseInt(fecha[0]) && today.get(Calendar.MONTH) == Integer.parseInt(fecha[1]) &&  today.get(Calendar.YEAR) == Integer.parseInt(fecha[2]) ) {
            messagesToDisplayDates.put(id, "AYER");
        }else if(today.get(Calendar.DAY_OF_MONTH)-2 == Integer.parseInt(fecha[0]) && today.get(Calendar.MONTH) == Integer.parseInt(fecha[1]) &&  today.get(Calendar.YEAR) == Integer.parseInt(fecha[2]) ) {
            messagesToDisplayDates.put(id, "ANTEAYER");
        }else{
            messagesToDisplayDates.put(id,dateToShow);
        }

    }

    private void checkIfDateWasDisplayed(String date) {
        for(String displayedDate : datesToDisplay){
            if(displayedDate.equals(date)){
                return;
            }
        }
        datesToDisplay.add(date);
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
        TextView textTimeUser;
        TextView date;

        ItemMessageUserHolder(View itemView) {
            super(itemView);
            txtContent =  itemView.findViewById(R.id.textContentUser);
            avatar =  itemView.findViewById(R.id.imageView2);
            textTimeUser = itemView.findViewById(R.id.textTimeUser);
            date = itemView.findViewById(R.id.date);
        }

        public void setAvatar(Context context,String currentUserUrl) {
            if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                Glide.with(itemView.getContext()).load(currentUserUrl).into(avatar);
            }
        }

        public void setAvatarFromId(final Context context, String userId) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                        Glide.with(itemView.getContext())
                                .load(u.getImageURL())
                                .into(avatar);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        CircleImageView avata;
        TextView textTimeFriend;
        TextView date;

        ItemMessageFriendHolder(View itemView) {
            super(itemView);
            txtContent =  itemView.findViewById(R.id.textContentFriend);
            avata =  itemView.findViewById(R.id.imageView3);
            textTimeFriend = itemView.findViewById(R.id.textTimeFriend);
            date = itemView.findViewById(R.id.date);
        }

        public void setAvatar(Context context,String userToUrl) {
            if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                Glide.with(itemView.getContext())
                        .load(userToUrl)
                        .into(avata);
            }
        }

        public void setAvatarFromId(final Context context, String userId) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if(ContextValidator.isValidContextForGlide(itemView.getContext())){
                        Glide.with(itemView.getContext())
                                .load(u.getImageURL())
                                .into(avata);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}

