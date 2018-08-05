package com.andresdlg.groupmeapp.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by andresdlg on 17/02/18.
 */

public class Task implements Parcelable {

    private String taskKey;
    private String name;
    private long startDate;
    private long endDate;
    private Boolean finished;
    private String taskDescription;
    private String author;

    public Task(String taskKey, String name,long startDate ,long endDate, Boolean finished, String taskDescription, String author){
        this.taskKey = taskKey;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.finished = finished;
        this.taskDescription = taskDescription;
        this.author = author;
    }

    public Task(){

    }

    protected Task(Parcel in) {
        name = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getFinished() ? 1 : 0);
        return result;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /*public boolean isFinished() {
        return finished;
    }*/
}
