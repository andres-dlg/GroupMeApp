package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.andresdlg.groupmeapp.DialogFragments.SubGroupNewTaskDialogFragment;
import com.andresdlg.groupmeapp.Entities.Meeting;
import com.andresdlg.groupmeapp.R;

import java.text.MessageFormat;
import java.util.Calendar;


public class MeetingSetupFragment extends Fragment {

    final int MODE_START_DATE = 1;
    final int MODE_END_DATE = 2;

    EditText meetingStartDate;
    EditText meetingStartTime;
    EditText meetingEndDate;
    EditText meetingEndTime;
    EditText meetingDetails;
    EditText meetingTitle;
    EditText meetingPlace;

    Calendar startDateCalendar;
    Calendar endDateCalendar;

    int dateMode;

    Meeting meeting;

    private OnTimeSetListener mOnTimeSetListener;

    public MeetingSetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
        Bundle bundle = getArguments();
        meeting = (Meeting) bundle.getSerializable("meeting");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meeting_setup, container, false);

        dateMode = MODE_START_DATE;

        meetingTitle = v.findViewById(R.id.meeting_title);
        meetingTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        meetingStartDate = v.findViewById(R.id.start_date);
        meetingStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_START_DATE;
                meetingStartTime.setEnabled(true);
                showDatePickerDialog(meetingStartDate);
            }
        });
        meetingStartDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        meetingStartTime = v.findViewById(R.id.start_time);
        meetingStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_START_DATE;
                showTimePickerDialog(meetingStartTime);
            }
        });
        meetingStartTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        meetingEndDate = v.findViewById(R.id.end_date);
        meetingEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_END_DATE;
                meetingEndTime.setEnabled(true);
                showDatePickerDialog(meetingEndDate);
            }
        });
        meetingEndDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        meetingEndTime = v.findViewById(R.id.end_time);
        meetingEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_END_DATE;
                showTimePickerDialog(meetingEndTime);
            }
        });
        meetingEndTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        meetingDetails = v.findViewById(R.id.meeting_details);

        meetingPlace = v.findViewById(R.id.meeting_place);
        meetingPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                meetingPlace.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        if(meeting!=null){
            startDateCalendar = Calendar.getInstance();
            startDateCalendar.setTimeInMillis(meeting.getStartTime());

            endDateCalendar = Calendar.getInstance();
            endDateCalendar.setTimeInMillis(meeting.getEndTime());

            mOnTimeSetListener.onTimeSet(true,startDateCalendar != null ? startDateCalendar.getTimeInMillis() : 0, endDateCalendar != null ? endDateCalendar.getTimeInMillis() : 0);

            meetingTitle.setText(meeting.getTitle());
            meetingPlace.setText(meeting.getPlace());
            meetingStartDate.setText(MessageFormat.format("{0} / {1} / {2}", startDateCalendar.get(Calendar.DAY_OF_MONTH), startDateCalendar.get(Calendar.MONTH), String.valueOf(startDateCalendar.get(Calendar.YEAR))));
            meetingEndDate.setText(MessageFormat.format("{0} / {1} / {2}", endDateCalendar.get(Calendar.DAY_OF_MONTH), endDateCalendar.get(Calendar.MONTH), String.valueOf(endDateCalendar.get(Calendar.YEAR))));
            meetingStartTime.setText(MessageFormat.format("{0}:{1}", startDateCalendar.get(Calendar.HOUR_OF_DAY) > 9 ? startDateCalendar.get(Calendar.HOUR_OF_DAY) : "0"+startDateCalendar.get(Calendar.HOUR_OF_DAY), startDateCalendar.get(Calendar.MINUTE) > 9 ? startDateCalendar.get(Calendar.MINUTE) : "0"+startDateCalendar.get(Calendar.MINUTE)));
            meetingStartTime.setEnabled(true);
            meetingEndTime.setText(MessageFormat.format("{0}:{1}", endDateCalendar.get(Calendar.HOUR_OF_DAY) > 9 ? endDateCalendar.get(Calendar.HOUR_OF_DAY) : "0"+endDateCalendar.get(Calendar.HOUR_OF_DAY), endDateCalendar.get(Calendar.MINUTE) > 9 ? endDateCalendar.get(Calendar.MINUTE) : "0"+endDateCalendar.get(Calendar.MINUTE)));
            meetingEndTime.setEnabled(true);
            meetingDetails.setText(meeting.getDetails());

        }

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
                        time.setText(MessageFormat.format("{0}:{1}", hourOfDay > 9 ? String.valueOf(hourOfDay) : "0"+String.valueOf(hourOfDay),minute > 9 ? String.valueOf(minute) : "0"+String.valueOf(minute)));
                        if(dateMode == MODE_START_DATE){
                            if(startDateCalendar!=null){
                                int day = startDateCalendar.get(Calendar.DAY_OF_MONTH);
                                int month = startDateCalendar.get(Calendar.MONTH);
                                int year = startDateCalendar.get(Calendar.YEAR);
                                startDateCalendar = Calendar.getInstance();
                                startDateCalendar.set(year,month,day,hourOfDay,minute);
                            }else{
                                startDateCalendar = Calendar.getInstance();
                                startDateCalendar.add(Calendar.HOUR_OF_DAY,hourOfDay);
                                startDateCalendar.add(Calendar.MINUTE,minute);
                            }
                        }else{
                            if(endDateCalendar!=null){
                                int day = endDateCalendar.get(Calendar.DAY_OF_MONTH);
                                int month = endDateCalendar.get(Calendar.MONTH);
                                int year = endDateCalendar.get(Calendar.YEAR);
                                endDateCalendar = Calendar.getInstance();
                                endDateCalendar.set(year,month,day,hourOfDay,minute);
                            }else {
                                endDateCalendar = Calendar.getInstance();
                                endDateCalendar.add(Calendar.HOUR_OF_DAY,hourOfDay);
                                endDateCalendar.add(Calendar.MINUTE,minute);
                            }

                        }
                        checkDateRange();
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
                if(dateMode == MODE_START_DATE){
                    if(!TextUtils.isEmpty(meetingStartTime.getText().toString().trim())){
                        int hour = startDateCalendar.get(Calendar.HOUR_OF_DAY);
                        int minute = startDateCalendar.get(Calendar.MINUTE);
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(year,month,day,hour,minute);
                    }else{
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(year,month,day,0,0,0);
                    }
                }else{
                    if(!TextUtils.isEmpty(meetingEndTime.getText().toString().trim())){
                        int hour = endDateCalendar.get(Calendar.HOUR_OF_DAY);
                        int minute = endDateCalendar.get(Calendar.MINUTE);
                        endDateCalendar = Calendar.getInstance();
                        endDateCalendar.set(year,month,day,hour,minute);
                    }else{
                        endDateCalendar = Calendar.getInstance();
                        endDateCalendar.set(year,month,day,23,59,59);
                    }
                }
                date.setText(selectedDate);
                checkDateRange();
            }
        });
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private void checkDateRange() {
        if( (endDateCalendar!= null && endDateCalendar.before(startDateCalendar)) ||
                (startDateCalendar!= null && startDateCalendar.after(endDateCalendar)) ){
            meetingEndDate.setError("Rango de fechas invalido");
            meetingStartDate.setError("Rango de fechas invalido");
            meetingStartTime.setError("Rango de fechas invalido");
            meetingEndTime.setError("Rango de fechas invalido");
            mOnTimeSetListener.onTimeSet(false,0,0);
        }else{
            meetingEndDate.setError(null);
            meetingStartDate.setError(null);
            meetingStartTime.setError(null);
            meetingEndTime.setError(null);
            mOnTimeSetListener.onTimeSet(true,startDateCalendar != null ? startDateCalendar.getTimeInMillis() : 0, endDateCalendar != null ? endDateCalendar.getTimeInMillis() : 0);
        }
    }

    public interface OnTimeSetListener{
        void onTimeSet(boolean timeRangeIsOk, long startTimeInMillis, long endTimeInMillis);
    }

    public void onAttachToParentFragment(Fragment fragment){
        try {
            mOnTimeSetListener = (OnTimeSetListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(e.getMessage());
        }
    }

}
