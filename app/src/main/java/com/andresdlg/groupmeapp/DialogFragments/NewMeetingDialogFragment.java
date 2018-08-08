package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Meeting;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupAddMembersFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.MeetingSetupFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by andresdlg on 15/04/18.
 */

public class NewMeetingDialogFragment extends DialogFragment implements GroupAddMembersFragment.OnUserSelectionSetListener, MeetingSetupFragment.OnTimeSetListener{

    private String groupKey;
    private Meeting meeting;

    Toolbar toolbar;
    EditText meetingStartDate;
    EditText meetingStartTime;
    EditText meetingEndDate;
    EditText meetingEndTime;
    EditText meetingDetails;
    EditText meetingTitle;
    EditText meetingPlace;

    private boolean timeRangeIsOk;
    private long startTimeInMillis;
    private long endTimeInMillis;
    int usersQuantity;
    private List<String> userIds;

    public NewMeetingDialogFragment(String groupKey) {
        this.groupKey = groupKey;
        usersQuantity = 0;
    }

    public NewMeetingDialogFragment(String groupKey, Meeting meeting) {
        this.groupKey = groupKey;
        this.meeting = meeting;
        usersQuantity = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_meetings_dialog_meeting, container, false);

        toolbar = view.findViewById(R.id.toolbar_chats);
        if(meeting != null) {
            toolbar.setTitle("Detalles de la reunion");
        }else{
            toolbar.setTitle("Nueva reunion");
        }
        toolbar.inflateMenu(R.menu.fragment_subgroup_new_task);
        if(meeting != null){
            toolbar.getMenu().removeItem(R.id.save);
        }else{
            toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    View setupFragmentView = getChildFragmentManager().getFragments().get(0).getView();
                    meetingTitle = setupFragmentView.findViewById(R.id.meeting_title);
                    meetingStartDate = setupFragmentView.findViewById(R.id.start_date);
                    meetingStartTime = setupFragmentView.findViewById(R.id.start_time);
                    meetingEndDate = setupFragmentView.findViewById(R.id.end_date);
                    meetingEndTime = setupFragmentView.findViewById(R.id.end_time);
                    meetingDetails = setupFragmentView.findViewById(R.id.meeting_details);
                    meetingPlace = setupFragmentView.findViewById(R.id.meeting_place);
                    if(validateFields()){
                        saveNewMeeting();
                    }else{
                        Toast.makeText(getContext(), "Revise los datos ingresados", Toast.LENGTH_SHORT).show();
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

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Setup", MeetingSetupFragment.class)
                .add("Add contacts", GroupAddMembersFragment.class)
                .create());

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        //viewPager.setOffscreenPageLimit(2);

        final NavigationTabBar navigationTabBar =  view.findViewById(R.id.ntb);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.pen),
                        Color.parseColor(colors[2])
                ).title("Editar")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.account_multiple),
                        Color.parseColor(colors[2])
                ).title("Invitados")
                        .badgeTitle("with")
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setInactiveColor(getResources().getColor(R.color.cardview_dark_background));
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setIsTitled(true);
        //navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        navigationTabBar.setTypeface("@font/simplifica_font");
        navigationTabBar.setTitleSize(25);
        navigationTabBar.setIconSizeFraction((float) 0.5);



        /*if(meeting != null){

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
            meetingName.setEnabled(false);
            meetingDecription.setEnabled(false);
            meetingStartDate.setEnabled(false);
            meetingEndDate.setEnabled(false);
            meetingFinished.setEnabled(false);
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
        }*/

        /*Fragment fragment = adapter.getItem(0);
        View setupFragmentView = fragment.getView();
        meetingTitle = setupFragmentView.findViewById(R.id.meeting_title);
        meetingStartDate = setupFragmentView.findViewById(R.id.start_date);
        meetingStartTime = setupFragmentView.findViewById(R.id.start_time);
        meetingEndDate = setupFragmentView.findViewById(R.id.end_date);
        meetingEndTime = setupFragmentView.findViewById(R.id.end_time);*/

        return view;
    }

    private void saveNewMeeting() {
        DatabaseReference groupMeetingsRef = FirebaseDatabase
                .getInstance()
                .getReference("Groups")
                .child(groupKey)
                .child("meetings");

        String meetingKey = groupMeetingsRef.push().getKey();

        Map<String, Object> meetingMap = new HashMap<>();
        meetingMap.put("meetingKey",meetingKey);
        meetingMap.put("title",meetingTitle.getText().toString().trim());
        meetingMap.put("startTime",startTimeInMillis);
        meetingMap.put("endTime",endTimeInMillis);
        meetingMap.put("details",meetingDetails.getText().toString().trim());
        meetingMap.put("finished",false);
        meetingMap.put("authorId", StaticFirebaseSettings.currentUserId);
        meetingMap.put("guestsIds",userIds);
        meetingMap.put("place",meetingPlace.getText().toString().trim());

        groupMeetingsRef.child(meetingKey).setValue(meetingMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Reunión agendada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al agendar reunión", Toast.LENGTH_SHORT).show();
            }
        });

        dismiss();
    }

    private boolean validateFields() {
        if(meetingTitle.getText().toString().isEmpty()){
            meetingTitle.setError("Ingrese título");
            return false;
        }else if(TextUtils.isEmpty(meetingStartDate.getText().toString().trim())){
            meetingStartDate.setError("Ingrese fecha desde");
            return false;
        }else if(TextUtils.isEmpty(meetingStartTime.getText().toString().trim())){
            meetingStartTime.setError("Ingrese hora desde");
            return false;
        }else if(TextUtils.isEmpty(meetingEndDate.getText().toString().trim())){
            meetingEndDate.setError("Ingrese fecha hasta");
            return false;
        }else if(TextUtils.isEmpty(meetingEndTime.getText().toString().trim())){
            meetingEndTime.setError("Ingrese hora hasta");
            return false;
        }else if(!timeRangeIsOk){
            meetingEndDate.setError("Rango de fechas invalido");
            meetingStartDate.setError("Rango de fechas invalido");
            meetingStartTime.setError("Rango de fechas invalido");
            meetingEndTime.setError("Rango de fechas invalido");
            Toast.makeText(getContext(), "Revise el rango de fechas ingresado", Toast.LENGTH_SHORT).show();
            return false;
        }else if(usersQuantity == 0){
            Toast.makeText(getContext(), "Debe haber por lo menos un invitado", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }


    /*private void saveTask(String input, String taskDescription) {

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
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onUserSelectionSet(List<String> userIds) {
        this.userIds = userIds;
        usersQuantity = userIds.size();
        if(usersQuantity > 0){
            toolbar.setSubtitle(usersQuantity + (usersQuantity == 1 ?  " invitado" : " invitados"));
        }else {
            toolbar.setSubtitle(null);
        }
    }

    @Override
    public void onTimeSet(boolean timeRangeIsOk, long startTimeInMillis, long endTimeInMillis) {
        this.timeRangeIsOk = timeRangeIsOk;
        this.startTimeInMillis = startTimeInMillis;
        this.endTimeInMillis = endTimeInMillis;
    }
}