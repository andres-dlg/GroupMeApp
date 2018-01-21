package com.andresdlg.groupmeapp.Entities;



public class Message {
    private String idSender;
    private String idReceiver;
    private String text;
    private long timestamp;
    private String id;

    public Message(String idSender, String idReceiver, String text, long timestamp, String id){
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.text = text;
        this.timestamp = timestamp;
        this.id = id;
    }

    public Message(){

    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}