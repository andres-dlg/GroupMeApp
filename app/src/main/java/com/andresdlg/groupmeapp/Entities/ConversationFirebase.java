package com.andresdlg.groupmeapp.Entities;

import java.util.List;

/**
 * Created by andresdlg on 21/01/18.
 */

public class ConversationFirebase {

    private String user1;
    private String user2;
    private String id;
    private Message message;
    private List<Message> messages;

    public ConversationFirebase(String user1, String user2, String id, Message message){
        this.user1 = user1;
        this.user2 = user2;
        this.id = id;
        this.message = message;
    }

    public ConversationFirebase(){

    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
