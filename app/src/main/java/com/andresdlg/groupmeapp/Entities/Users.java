package com.andresdlg.groupmeapp.Entities;

/**
 * Created by andresdlg on 28/12/17.
 */

public class Users {

    private boolean isGone = false;

    private String alias;
    private String imageUrl;
    private String job;
    private String name;
    private String userid;
    private String phone;
    private String token_id;

    private Users() {}

    public Users(String alias, String imageUrl, String job, String name, String userid, String phone, String token_id){
        this.alias = alias;
        this.imageUrl = imageUrl;
        this.job = job;
        this.name = name;
        this.userid = userid;
        this.phone = phone;
        this.token_id = token_id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getImageURL() {
        return imageUrl;
    }

    public void setImageURL(String imageURL) {
        this.imageUrl = imageURL;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isGone() {
        return isGone;
    }

    public void setGone(boolean gone) {
        isGone = gone;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }
}
