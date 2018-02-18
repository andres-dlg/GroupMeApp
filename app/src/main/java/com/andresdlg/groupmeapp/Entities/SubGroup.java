package com.andresdlg.groupmeapp.Entities;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by andresdlg on 17/02/18.
 */

public class SubGroup extends ExpandableGroup<Task> {

    private String name;
    private List<Task> tasks;

    public SubGroup(String name, List<Task> tasks) {
        super(name, tasks);
        this.name = name;
        this.tasks = tasks;
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
}
