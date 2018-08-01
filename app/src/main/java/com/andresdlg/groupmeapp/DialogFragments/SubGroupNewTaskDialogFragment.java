package com.andresdlg.groupmeapp.DialogFragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupNewTaskDialogFragment extends DialogFragment {

    final int MODE_START_DATE = 1;
    final int MODE_END_DATE = 2;
    final int INSERT = 3;
    final int UPDATE = 4;

    private String subGroupKey;
    private String groupKey;
    private Task task;
    private boolean fromWeekView;

    EditText taskStartDate;
    EditText taskEndDate;
    EditText taskFinished;
    EditText subgroupName;
    EditText taskDecription;
    EditText taskName;

    Calendar startDateCalendar;
    Calendar endDateCalendar;

    int dateMode;
    int databaseMode;

    public SubGroupNewTaskDialogFragment(String subGroupKey, String groupKey) {
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
    }

    public SubGroupNewTaskDialogFragment(String subGroupKey, String groupKey, Task task) {
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
        this.task = task;
    }

    public SubGroupNewTaskDialogFragment(String subGroupKey, String groupKey, Task task, boolean fromWeekView) {
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
        this.task = task;
        this.fromWeekView = fromWeekView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v;
        if(fromWeekView){
            v = inflater.inflate(R.layout.fragment_subgroup_task_details, container, false);

            taskFinished = v.findViewById(R.id.task_finished);
            if(task.getFinished()){
                taskFinished.setText("Finalizada");
            }else{
                taskFinished.setText("En curso");
            }

            subgroupName = v.findViewById(R.id.subgroup_name);

            FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    subgroupName.setText(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            v = inflater.inflate(R.layout.fragment_subgroup_new_task, container, false);
        }


        taskName = v.findViewById(R.id.task_name);
        taskDecription = v.findViewById(R.id.task_desc);

        taskStartDate = v.findViewById(R.id.start_date);
        taskStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_START_DATE;
                showDatePickerDialog(taskStartDate);
            }
        });

        taskEndDate = v.findViewById(R.id.end_date);
        taskEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateMode = MODE_END_DATE;
                showDatePickerDialog(taskEndDate);
            }
        });

        container = v.findViewById(R.id.container);
        if(fromWeekView){
            container.setEnabled(false);
            taskName.setEnabled(false);
            taskDecription.setEnabled(false);
            taskStartDate.setEnabled(false);
            taskEndDate.setEnabled(false);
            taskFinished.setEnabled(false);
            subgroupName.setEnabled(false);
        }

        Toolbar toolbar = v.findViewById(R.id.toolbar_chats);
        if(fromWeekView) {
            toolbar.setTitle("Detalles de la tarea");
        }else{
            toolbar.setTitle("Nueva tarea");
        }
        toolbar.inflateMenu(R.menu.fragment_subgroup_new_task);
        if(fromWeekView){
            toolbar.getMenu().removeItem(R.id.save);
        }else{
            toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(!taskName.getText().toString().isEmpty()){
                        saveTask(taskName.getText().toString(),taskDecription.getText().toString());
                    }else{
                        Toast.makeText(getContext(), "Debe insertar un nombre para la tarea", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        if(task!=null){
            taskName.setText(task.getName());
            if(task.getStartDate() != 0){
                startDateCalendar = Calendar.getInstance();
                startDateCalendar.setTimeInMillis(task.getStartDate());
                taskStartDate.setText(formatDate(task.getStartDate()));
            }
            if(task.getEndDate() != 0){
                endDateCalendar = Calendar.getInstance();
                endDateCalendar.setTimeInMillis(task.getEndDate());
                taskEndDate.setText(formatDate(task.getEndDate()));
            }
            taskDecription.setText(task.getTaskDescription());
            if(taskDecription.getText().toString().isEmpty()){
                taskDecription.setText("No hay descripción para esta tarea");
            }
            databaseMode = UPDATE;
        }else{
            databaseMode = INSERT;
        }

        return v;
    }

    private void showDatePickerDialog(final EditText date) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = day + " / " + (month+1) + " / " + year;
                if(dateMode == MODE_START_DATE){
                    startDateCalendar = Calendar.getInstance();
                    startDateCalendar.set(year,month,day,0,0,0);
                    checkDateRange();
                }else{
                    endDateCalendar = Calendar.getInstance();
                    endDateCalendar.set(year,month,day,23,59,59);
                    checkDateRange();
                }
                date.setText(selectedDate);
            }
        });
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private boolean checkDateRange() {
        if( (endDateCalendar!= null && endDateCalendar.before(startDateCalendar)) ||
                (startDateCalendar!= null && startDateCalendar.after(endDateCalendar)) ){
            taskEndDate.setError("Rango de fechas invalido");
            taskStartDate.setError("Rango de fechas invalido");
            return false;
        }else{
            taskEndDate.setError(null);
            taskStartDate.setError(null);
            return true;
        }
    }

        private void saveTask(String input, String taskDescription) {

        long startDateInMillis = 0;
        long endDateInMillis = 0;

        if(startDateCalendar!=null){
            startDateInMillis = startDateCalendar.getTimeInMillis();
        }

        if (endDateCalendar != null){
            endDateInMillis = endDateCalendar.getTimeInMillis();
        }

        if(checkDateRange()){

            DatabaseReference subGroupsTasksRef = FirebaseDatabase.getInstance().getReference("Groups")
                    .child(groupKey)
                    .child("subgroups")
                    .child(subGroupKey)
                    .child("tasks");

            if(databaseMode == INSERT){
                String taskKey = subGroupsTasksRef.push().getKey();

                Task task = new Task(taskKey,input,startDateInMillis,endDateInMillis,false,taskDescription,StaticFirebaseSettings.currentUserId);

                Map<String,Object> map = new HashMap<>();
                map.put("taskKey",task.getTaskKey());
                map.put("name",task.getName());
                map.put("startDate",task.getStartDate());
                map.put("endDate",task.getEndDate());
                map.put("finished",task.getFinished());
                map.put("taskDescription",task.getTaskDescription());
                map.put("author", task.getAuthor());

                subGroupsTasksRef.child(taskKey).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Tarea agregada", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error al agregar tarea", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Map<String,Object> map = new HashMap<>();
                map.put("name",input);
                map.put("startDate",startDateInMillis);
                map.put("endDate",endDateInMillis);
                map.put("taskDescription",taskDescription);

                subGroupsTasksRef.child(task.getTaskKey()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Tarea actualizada", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dismiss();
        }else{
            Toast.makeText(getContext(), "Revise las fechas ingresadas", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private DatePickerDialog.OnDateSetListener listener;

        public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener) {
            DatePickerFragment fragment = new DatePickerFragment();
            fragment.setListener(listener);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }


        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        }
    }

    private String formatDate(long timeInMillis){
        Date date = new Date(timeInMillis);
        //SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm")  ;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd/MM/yyyy")  ;
        return simpleDateFormater.format(date);
    }

}