package com.andresdlg.groupmeapp.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by andresdlg on 17/02/18.
 */

public class Task implements Parcelable {

    private String name;
    private Calendar startDate;
    private Calendar endDate;
    private Boolean finished;

    public Task(String name,Calendar startDate ,Calendar endDate, Boolean finished){
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.finished = finished;
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

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
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
        result = 31 * result + (isFinished() ? 1 : 0);
        return result;
    }

    public boolean isFinished() {
        return finished;
    }
}
