package com.andresdlg.groupmeapp.Entities;

import java.util.Calendar;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class WeekViewEventGroupMeApp {
    private String mGroupKey;
    private boolean mTaskFinished;
    private String mTaskDesc;
    private String mSubGroupKey;
    private String mTaskKey;
    private long mId;
    private Calendar mStartTime;
    private Calendar mEndTime;
    private String mName;
    private String mLocation;
    private int mColor;

    public WeekViewEventGroupMeApp(){

    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startYear Year when the event starts.
     * @param startMonth Month when the event starts.
     * @param startDay Day when the event starts.
     * @param startHour Hour (in 24-hour format) when the event starts.
     * @param startMinute Minute when the event starts.
     * @param endYear Year when the event ends.
     * @param endMonth Month when the event ends.
     * @param endDay Day when the event ends.
     * @param endHour Hour (in 24-hour format) when the event ends.
     * @param endMinute Minute when the event ends.
     */
    public WeekViewEventGroupMeApp(long id, String name, int startYear, int startMonth, int startDay, int startHour, int startMinute, int endYear, int endMonth, int endDay, int endHour, int endMinute) {
        this.mId = id;

        this.mStartTime = Calendar.getInstance();
        this.mStartTime.set(Calendar.YEAR, startYear);
        this.mStartTime.set(Calendar.MONTH, startMonth-1);
        this.mStartTime.set(Calendar.DAY_OF_MONTH, startDay);
        this.mStartTime.set(Calendar.HOUR_OF_DAY, startHour);
        this.mStartTime.set(Calendar.MINUTE, startMinute);

        this.mEndTime = Calendar.getInstance();
        this.mEndTime.set(Calendar.YEAR, endYear);
        this.mEndTime.set(Calendar.MONTH, endMonth-1);
        this.mEndTime.set(Calendar.DAY_OF_MONTH, endDay);
        this.mEndTime.set(Calendar.HOUR_OF_DAY, endHour);
        this.mEndTime.set(Calendar.MINUTE, endMinute);

        this.mName = name;
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    public WeekViewEventGroupMeApp(long id, String name, String location, Calendar startTime, Calendar endTime) {
        this.mId = id;
        this.mName = name;
        this.mLocation = location;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    public WeekViewEventGroupMeApp(long id, String name, Calendar startTime, Calendar endTime) {
        this(id, name, null, startTime, endTime);
    }

    public WeekViewEventGroupMeApp(int id, String name, String groupKey, String subGroupKey, String taskKey, String taskDescription, Boolean finished, Calendar taskStartDateTime, Calendar taskEndDateTime) {
        this.mId = id;
        this.mName = name;
        this.mGroupKey = groupKey;
        this.mSubGroupKey = subGroupKey;
        this.mTaskKey = taskKey;
        this.mTaskDesc = taskDescription;
        this.mTaskFinished = finished;
        this.mStartTime = taskStartDateTime;
        this.mEndTime = taskEndDateTime;
    }


    public Calendar getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Calendar startTime) {
        this.mStartTime = startTime;
    }

    public Calendar getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Calendar endTime) {
        this.mEndTime = endTime;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeekViewEventGroupMeApp that = (WeekViewEventGroupMeApp) o;

        return mId == that.mId;

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    public String getmGroupKey() {
        return mGroupKey;
    }

    public void setmGroupKey(String mGroupKey) {
        this.mGroupKey = mGroupKey;
    }

    public boolean ismTaskFinished() {
        return mTaskFinished;
    }

    public void setmTaskFinished(boolean mTaskFinished) {
        this.mTaskFinished = mTaskFinished;
    }

    public String getmTaskDesc() {
        return mTaskDesc;
    }

    public void setmTaskDesc(String mTaskDesc) {
        this.mTaskDesc = mTaskDesc;
    }

    public String getmSubGroupKey() {
        return mSubGroupKey;
    }

    public void setmSubGroupKey(String mSubGroupKey) {
        this.mSubGroupKey = mSubGroupKey;
    }

    public String getmTaskKey() {
        return mTaskKey;
    }

    public void setmTaskKey(String mTaskKey) {
        this.mTaskKey = mTaskKey;
    }
}