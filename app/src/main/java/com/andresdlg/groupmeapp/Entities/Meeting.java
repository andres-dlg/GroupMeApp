package com.andresdlg.groupmeapp.Entities;

import java.util.List;

public class Meeting {

    private String title;
    private long startTime;
    private long entTime;
    private String details;
    private boolean finished;
    private String authorId;
    private List<String> guestsIds;

    public Meeting(String title, long startTime, long entTime, String details, boolean finished, String authorId, List<String> guestsIds){
        this.title = title;
        this.startTime = startTime;
        this.entTime = entTime;
        this.details = details;
        this.finished = finished;
        this.authorId = authorId;
        this.guestsIds = guestsIds;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getEntTime() {
        return entTime;
    }

    public void setEntTime(long entTime) {
        this.entTime = entTime;
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
}
