package com.andresdlg.groupmeapp.Entities;

import java.util.List;
import java.util.Map;

public class Post {

    private String postId;
    private String text;
    private long time;
    private String userId;
    private String groupName;
    private String groupKey;
    private List<String> seenBy;
    private List<String> likeBy;

    public Post(String postId, String text, long time, String userId, String groupName,String groupKey, List<String> seenBy, List<String> likeBy){
        this.postId = postId;
        this.text = text;
        this.time = time;
        this.userId = userId;
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.seenBy = seenBy;
        this.likeBy = likeBy;
    }

    public Post(){ }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public List<String> getLikeBy() {
        return likeBy;
    }

    public void setLikeBy(List<String> likeBy) {
        this.likeBy = likeBy;
    }
}
