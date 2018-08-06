package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.andresdlg.groupmeapp.DialogFragments.SubGroupNewTaskDialogFragment;
import com.andresdlg.groupmeapp.R;

import java.util.Calendar;


public class MeetingSetupFragment extends Fragment {

    EditText meetingStartDate;
    EditText meetingStartTime;
    EditText meetingEndDate;
    EditText meetingEndTime;
    EditText meetingDetails;
    EditText meetingTitle;

    Calendar startDateCalendar;
    Calendar endDateCalendar;

    boolean meetingFinished;

    public MeetingSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meeting_setup, container, false);

        meetingTitle = v.findViewById(R.id.meeting_title);

        meetingStartDate = v.findViewById(R.id.start_date);
        meetingStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(meetingStartDate);
            }
        });

        meetingStartTime = v.findViewById(R.id.start_time);
        meetingStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(meetingStartTime);
            }
        });

        meetingEndDate = v.findViewById(R.id.end_date);
        meetingEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(meetingEndDate);
            }
        });

        meetingEndTime = v.findViewById(R.id.end_time);
        meetingEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(meetingEndTime);
            }
        });

        meetingDetails = v.findViewById(R.id.meeting_details);

        return v;
    }

    private void showTimePickerDialog(final EditText time) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                R.style.datepicker,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(hourOfDay + ":" + minute);
                    }
                },
                mHour,
                mMinute,
                true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog(final EditText date) {
        SubGroupNewTaskDialogFragment.DatePickerFragment newFragment = SubGroupNewTaskDialogFragment.DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = day + " / " + (month+1) + " / " + year;
                /*if(dateMode == MODE_START_DATE){
                    startDateCalendar = Calendar.getInstance();
                    startDateCalendar.set(year,month,day,0,0,0);
                    checkDateRange();
                }else{
                    endDateCalendar = Calendar.getInstance();
                    endDateCalendar.set(year,month,day,23,59,59);
                    checkDateRange();
                }*/
                date.setText(selectedDate);
            }
        });
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

}
