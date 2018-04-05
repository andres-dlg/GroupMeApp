package com.andresdlg.groupmeapp.uiPackage;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;

import com.alamkanak.weekview.WeekViewEvent;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by andresdlg on 25/03/18.
 */

public class TaskWeekViewActivity extends TaskWeekViewBaseActivty {

    //String groupKey;
    //List<WeekViewEvent> events;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //groupKey = getIntent().getStringExtra("groupKey");
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
        // Populate the week view with some events.
        // final List<WeekViewEvent> events = new ArrayList<>();

        /*DatabaseReference subGroupsRef = FirebaseDatabase.getInstance().getReference("Groups")
                .child(groupKey)
                .child("subgroups");*/

        /*subGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot subgroupRef: dataSnapshot.getChildren()){
                    for(DataSnapshot taskRef: subgroupRef.child("tasks").getChildren()){
                        Task task = taskRef.getValue(Task.class);

                        Calendar taskStartDateTime = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:MM");
                        try {
                            taskStartDateTime.setTime(dateFormat.parse(task.getStartDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar taskEndDateTime = Calendar.getInstance();
                        try {
                            taskEndDateTime.setTime(dateFormat.parse(task.getEndDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, taskStartDateTime.get(Calendar.HOUR_OF_DAY));
                        startTime.set(Calendar.MINUTE, taskStartDateTime.get(Calendar.MINUTE));
                        startTime.set(Calendar.MONTH, taskStartDateTime.get(Calendar.MONTH));
                        startTime.set(Calendar.YEAR, taskStartDateTime.get(Calendar.YEAR));

                        Calendar endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY, taskEndDateTime.get(Calendar.HOUR_OF_DAY));
                        endTime.set(Calendar.MINUTE, taskEndDateTime.get(Calendar.MINUTE));
                        endTime.set(Calendar.MONTH, taskEndDateTime.get(Calendar.MONTH));
                        endTime.set(Calendar.YEAR, taskEndDateTime.get(Calendar.YEAR));

                        WeekViewEvent event = new WeekViewEvent(i, task.getName(), startTime, endTime);
                        event.setColor(getResources().getColor(R.color.colorPrimary));
                        events.add(event);
                        i++;
                    }
                }
                i = 0;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        /*Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR, 1);
        endTime.set(Calendar.MONTH, newMonth - 1);
        WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimary));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 4);
        endTime.set(Calendar.MINUTE, 30);
        endTime.set(Calendar.MONTH, newMonth-1);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.mdtp_date_picker_text_disabled_dark_theme));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 4);
        startTime.set(Calendar.MINUTE, 20);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 5);
        endTime.set(Calendar.MINUTE, 0);
        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimaryDark));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 30);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 2);
        endTime.set(Calendar.MONTH, newMonth-1);
        event = new WeekViewEvent(2, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.mdtp_date_picker_text_disabled_dark_theme));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        startTime.add(Calendar.DATE, 1);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(3, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimaryDark));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 15);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(4, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.mdtp_done_text_color));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 1);
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimary));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.HOUR_OF_DAY, 15);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 3);
        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
        event.setColor(getResources().getColor(R.color.theme_secondary_text_inverted));
        events.add(event);

        //AllDay event
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 23);
        event = new WeekViewEvent(7, getEventTitle(startTime),null, startTime, endTime);
        event.setColor(getResources().getColor(R.color.mdtp_calendar_selected_date_text));
        events.add(event);
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 8);
        startTime.set(Calendar.HOUR_OF_DAY, 2);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.DAY_OF_MONTH, 10);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        event = new WeekViewEvent(8, getEventTitle(startTime),null, startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimaryDark));
        events.add(event);

        // All day event until 00:00 next day
        startTime = Calendar.getInstance();
        startTime.set(Calendar.DAY_OF_MONTH, 10);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.DAY_OF_MONTH, 11);
        event = new WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimary));
        events.add(event);
        return events;*/

        List<WeekViewEvent> eventos = ((FireApp)getApplication()).getEvents();
        List<WeekViewEvent> eventosAgregados = new ArrayList<>();
        for (WeekViewEvent myEvent: eventos) {
            //TODO: this is your comparison
            if (myEvent.getStartTime().get(Calendar.MONTH) == (newMonth-1) && myEvent.getStartTime().get(Calendar.YEAR) == newYear ) {
                Calendar startTime = myEvent.getStartTime();
                Calendar endTime = myEvent.getEndTime();
                WeekViewEvent event = new WeekViewEvent((long)(Math.random()*999999999), myEvent.getName(), startTime, endTime);
                //event.setColor(ContextCompat.getColor(this, android.R.color.holo_blue_bright));
                event.setColor(getColor());
                eventosAgregados.add(event);
            }
        }

        /*List<WeekViewEvent> events = ((FireApp)getApplication()).getEvents();
        if (events!=null) {
            if (events.size() > 0) {
                events.clear();
            }
        }
        events = getEvents(newYear, newMonth);*/

        return eventosAgregados;
    }

    private int getColor(){
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        return Color.rgb(r,g,b);
    }
}
