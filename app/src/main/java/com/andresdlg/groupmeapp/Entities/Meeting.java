package com.andresdlg.groupmeapp.Entities;

import java.util.List;

public class Meeting {

    private String meetingKey;
    private String title;
    private long startTime;
    private long endTime;
    private String details;
    private boolean finished;
    private String authorId;
    private List<String> guestsIds;
    private String place;

    public Meeting(String meetingKey, String title, long startTime, long endTime, String details, boolean finished, String authorId, List<String> guestsIds, String place){
        this.meetingKey = meetingKey;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.details = details;
        this.finished = finished;
        this.authorId = authorId;
        this.guestsIds = guestsIds;
        this.place = place;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public List<String> getGuestsIds() {
        return guestsIds;
    }

    public void setGuestsIds(List<String> guestsIds) {
        this.guestsIds = guestsIds;
    }

    public String getMeetingKey() {
        return meetingKey;
    }

    public void setMeetingKey(String meetingKey) {
        this.meetingKey = meetingKey;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}
