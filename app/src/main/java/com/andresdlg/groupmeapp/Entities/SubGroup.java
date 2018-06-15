package com.andresdlg.groupmeapp.Entities;


import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 17/02/18.
 */

public class SubGroup extends ExpandableGroup<File> implements Serializable{

    private String name;
    private String objetive;
    private String imageUrl;
    private String subGroupKey;
    private Map<String,String> members;
    private List<Task> tasks;
    private List<File> files;

    public SubGroup(String name,String objetive, String imageUrl, String subGroupKey, Map<String,String> members, List<Task> tasks) {
        super(name,null);
        this.name = name;
        this.objetive = objetive;
        this.imageUrl = imageUrl;
        this.subGroupKey = subGroupKey;
        this.members = members;
        this.tasks = tasks;
    }

    public SubGroup(String name, List<File> files, String imageUrl,Map<String,String> members,String subGroupKey){
        super(name,files);
        this.name = name;
        this.imageUrl = imageUrl;
        this.files = files;
        this.members = members;
        this.subGroupKey = subGroupKey;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSubGroupKey() {
        return subGroupKey;
    }

    public void setSubGroupKey(String subGroupKey) {
        this.subGroupKey = subGroupKey;
    }

    public Map<String,String> getMembers() {
        return members;
    }

    public void setMembers(Map<String,String> members) {
        this.members = members;
    }

    public String getObjetive() {
        return objetive;
    }

    public void setObjetive(String objective) {
        this.objetive = objective;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
