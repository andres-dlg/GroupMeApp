package com.andresdlg.groupmeapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupChatDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupFilesDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupMembersDialogFragment;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.BlurBuilder;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.GroupActivity;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by andresdlg on 17/02/18.
 */

public class RVSubGroupAdapter extends RecyclerView.Adapter<RVSubGroupAdapter.SubGroupViewHolder> {

    private String groupKey;
    private List<SubGroup> subGroups;
    private Context contexto;

    private final int DIALOG_CHAT = 0;
    private final int DIALOG_MEMBERS = 1;
    private final int DIALOG_FILES = 2;

    public RVSubGroupAdapter(List<SubGroup> subGroups, String groupKey, Context context) {
        this.groupKey = groupKey;
        this.subGroups = subGroups;
        this.contexto = context;
    }

    @Override
    public SubGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing, parent, false);
        return new SubGroupViewHolder(v,viewType,parent);
    }

    @Override
    public void onBindViewHolder(SubGroupViewHolder holder, int position) {
        SubGroup subGroup = subGroups.get(position);
        holder.textView_parentName.setText(subGroup.getName());

        //
        int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
        if(subGroup.getTasks() != null){
            int noOfChild = subGroup.getTasks().size();
            for (int index = noOfChild; index < noOfChildTextViews; index++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                currentTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return subGroups.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void clear() {
        subGroups.clear();
        notifyDataSetChanged();
    }

    public void notify(List<SubGroup> list) {
        List<SubGroup> aux = new ArrayList<>();
        aux.addAll(list);
        if (this.subGroups != null) {
            this.subGroups.clear();
            this.subGroups.addAll(aux);
        } else {
            this.subGroups.addAll(aux);
        }
        notifyDataSetChanged();
    }

    public class SubGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, DatePickerDialog.OnDateSetListener{

        private Context context;
        private TextView textView_parentName;
        private LinearLayout linearLayout_childItems;
        int position;
        CheckBox checkBox;
        TextView textView;
        TextView startDate;
        TextView endDate;
        //ImageView addTaskiv;
        //ImageView chat;
        ImageButton addTaskiv;
        ImageButton chat;
        ImageButton membersiv;
        ImageButton files;
        ViewGroup parent;
        LinearLayout cardLl;
        CircleImageView subGroupPhoto;
        ImageView subGroupBg;
        String imageUrl;

        SubGroupViewHolder(final View itemView, final int position, ViewGroup parent) {
            super(itemView);
            this.position = position;
            this.parent = parent;
            context = itemView.getContext();
            textView_parentName = itemView.findViewById(R.id.tv_parentName);

            addTaskiv = itemView.findViewById(R.id.add_task);
            addTaskiv.setOnClickListener(this);

            chat = itemView.findViewById(R.id.chat);
            chat.setOnClickListener(this);

            membersiv = itemView.findViewById(R.id.members);
            membersiv.setOnClickListener(this);

            files = itemView.findViewById(R.id.files);
            files.setOnClickListener(this);

            cardLl = itemView.findViewById(R.id.cardLl);

            ImageView arrow = itemView.findViewById(R.id.list_item_sub_group_arrow);
            arrow.setOnClickListener(this);

            subGroupPhoto = itemView.findViewById(R.id.list_item_sub_group_icon);

            linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
            linearLayout_childItems.setVisibility(View.GONE);

            subGroupBg = itemView.findViewById(R.id.subGroupBg);

            boolean isSubGroupMember = false;
            Map<String,String> members = subGroups.get(position).getMembers();
            for(Map.Entry<String, String> entry: members.entrySet()) {
                if(entry.getKey().equals(StaticFirebaseSettings.currentUserId)){
                    isSubGroupMember = true;
                    //checkBox.setEnabled(true);
                    break;
                }
            }

            if(!isSubGroupMember){
                chat.setEnabled(false);
                addTaskiv.setEnabled(false);
                membersiv.setEnabled(false);
                files.setEnabled(false);
                cardLl.setBackgroundColor(context.getResources().getColor(R.color.gray_200));
            }

            //SETEO LAS FOTOS DE PERFIL Y EL BACKGROUND
            imageUrl = subGroups.get(position).getImageUrl();

            //PERFIL
            Glide.with(context)
                    .load(subGroups.get(position).getImageUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(subGroupPhoto);

            //BACKGROUND
            SimpleTarget target = new SimpleTarget() {
                @Override
                public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                    Bitmap blurredBitmap = BlurBuilder.blur(context, ((BitmapDrawable) resource).getBitmap());
                    subGroupBg.setImageBitmap(blurredBitmap);
                    subGroupBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    //subGroupBg.setImageDrawable((BitmapDrawable)resource);
                    //subGroupBg.setImageBitmap(((BitmapDrawable) resource).getBitmap());
                }
            };

            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.background_placeholder);

            //subGroupBg.setTag(target);
            if(imageUrl.equals("https://firebasestorage.googleapis.com/v0/b/groupmeapp-5aaf6.appspot.com/o/group_work_grey_192x192.png?alt=media&token=4aadc31e-91e6-416e-a866-3138a08425d3")){
                Glide.with(contexto)
                        .load("")
                        .apply(requestOptions)
                        .into(subGroupBg);
            }else{
                Glide.with(context)
                        .load(imageUrl)
                        //.apply(RequestOptions.bitmapTransform(new BlurTransformation(25,5)))
                        .into(target);
            }


            if(subGroups.get(position).getTasks() != null){
                int intMaxNoOfChild = subGroups.get(position).getTasks().size();
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent, false);
                    FrameLayout fl = v.findViewById(R.id.fltask);
                    fl.setOnClickListener(this);

                    //Nombre de la tarea
                    textView = fl.findViewById(R.id.tasktv);
                    textView.setText(subGroups.get(position).getTasks().get(indexView).getName());

                    //Fecha de comienzo de la tarea
                    startDate = fl.findViewById(R.id.taskStartDateTv);
                    final String startDateText = subGroups.get(position).getTasks().get(indexView).getStartDate();
                    if(!TextUtils.isEmpty(startDateText)){
                        startDate.setText(subGroups.get(position).getTasks().get(indexView).getStartDate());
                    }else{
                        startDate.setText("No definida");
                    }

                    //Fecha de fin de la tarea
                    endDate = fl.findViewById(R.id.taskEndDateTv);
                    final String endDateText = subGroups.get(position).getTasks().get(indexView).getEndDate();
                    if(!TextUtils.isEmpty(endDateText)){
                        endDate.setText(subGroups.get(position).getTasks().get(indexView).getEndDate());
                    }else{
                        endDate.setText("No definida");
                    }

                    //Checkbox de la tarea
                    checkBox = fl.findViewById(R.id.checkbox);
                    checkBox.setEnabled(false);
                    if(subGroups.get(position).getTasks().get(indexView).getFinished()){
                        checkBox.setChecked(true);
                    }
                    final int finalIndexView = indexView;
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finishResumeTask((CheckBox)view,context,subGroups.get(position).getSubGroupKey(),subGroups.get(position).getTasks().get(finalIndexView).getTaskKey());
                        }
                    });
                    if(isSubGroupMember){
                        checkBox.setEnabled(true);
                    }

                    //TimeRangePicker listener
                    final TimePickerDialog.OnTimeSetListener timeListener =  new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
                            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                    .child(groupKey)
                                    .child("subgroups")
                                    .child(subGroups.get(position).getSubGroupKey())
                                    .child("tasks").child(subGroups.get(position).getTasks().get(finalIndexView).getTaskKey());

                            String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                            String minuteString = minute < 10 ? "0"+minute : ""+minute;
                            String hourStringEnd = hourOfDayEnd < 10 ? "0"+hourOfDayEnd : ""+hourOfDayEnd;
                            String minuteStringEnd = minuteEnd < 10 ? "0"+minuteEnd : ""+minuteEnd;
                            String time = "You picked the following time: From - "+hourString+"h"+minuteString+" To - "+hourStringEnd+"h"+minuteStringEnd;

                            String startTime = hourString+":"+minuteString;
                            String endTime = hourStringEnd+":"+minuteStringEnd;

                            FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(finalIndexView);
                            TextView tvStartDate = fl.findViewById(R.id.taskStartDateTv);
                            TextView tvEndDate = fl.findViewById(R.id.taskEndDateTv);

                            String startDate = tvStartDate.getText().toString();
                            String endDate = tvEndDate.getText().toString();

                            String startDateTime = startDate+" "+startTime;
                            String endDateTime = endDate+" "+endTime;

                            tvStartDate.setText(startDateTime);
                            tvEndDate.setText(endDateTime);

                            taskRef.child("startDate").setValue(startDateTime);
                            taskRef.child("endDate").setValue(endDateTime);

                            Toast.makeText(context,"Horarios guardados",Toast.LENGTH_SHORT).show();
                        }
                    };

                    //DateRangePicker
                    final DatePickerDialog.OnDateSetListener dateListener =  new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
                            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                    .child(groupKey)
                                    .child("subgroups")
                                    .child(subGroups.get(position).getSubGroupKey())
                                    .child("tasks").child(subGroups.get(position).getTasks().get(finalIndexView).getTaskKey());

                            String newDayOfMonth = String.valueOf(dayOfMonth).length() > 1 ? String.valueOf(dayOfMonth) : "0"+String.valueOf(dayOfMonth);
                            String newMonthOfYear = String.valueOf(monthOfYear+1).length() > 1 ? String.valueOf(monthOfYear+1) : "0"+String.valueOf(monthOfYear+1);
                            String newDayOfMonthEnd = String.valueOf(dayOfMonthEnd).length() > 1 ? String.valueOf(dayOfMonthEnd) : "0"+String.valueOf(dayOfMonthEnd);
                            String newMonthOfYearEnd = String.valueOf(monthOfYearEnd+1).length() > 1 ? String.valueOf(monthOfYearEnd+1) : "0"+String.valueOf(monthOfYearEnd+1);

                            String startDateTxt = newDayOfMonth + "-" + newMonthOfYear +"-" + String.valueOf(year);
                            String endDateTxt = newDayOfMonthEnd + "-" + newMonthOfYearEnd +"-" + String.valueOf(yearEnd);

                            taskRef.child("startDate").setValue(startDateTxt);
                            taskRef.child("endDate").setValue(endDateTxt);

                            FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(finalIndexView);
                            ((TextView)fl.findViewById(R.id.taskStartDateTv)).setText(startDateTxt);
                            ((TextView)fl.findViewById(R.id.taskEndDateTv)).setText(endDateTxt);

                            setTime(startDateTxt, endDateTxt, timeListener);

                            Toast.makeText(context,"Fechas guardadas",Toast.LENGTH_SHORT).show();
                        }
                    };

                    CircleImageView btnTaskCalendar = fl.findViewById(R.id.btn_task_calendar);
                    btnTaskCalendar.setEnabled(false);
                    btnTaskCalendar.setColorFilter(ContextCompat.getColor(context, R.color.gray_400), android.graphics.PorterDuff.Mode.SRC_IN);
                    btnTaskCalendar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar now = Calendar.getInstance();
                            DatePickerDialog dpd = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                                    dateListener,
                                    now.get(Calendar.YEAR),
                                    now.get(Calendar.MONTH),
                                    now.get(Calendar.DAY_OF_MONTH)
                            );
                            dpd.setStartTitle("DESDE");
                            dpd.setEndTitle("HASTA");
                            dpd.setAccentColor(R.color.colorPrimary);
                            dpd.setCancelable(false);
                            dpd.show(((Activity)RVSubGroupAdapter.this.contexto).getFragmentManager(),"Datepickerdialog");
                        }
                    });
                    if(isSubGroupMember){
                        btnTaskCalendar.setEnabled(true);
                        btnTaskCalendar.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    //Menu (3 puntitos)
                    CircleImageView btnMenu = fl.findViewById(R.id.btn_menu);
                    btnMenu.setOnClickListener(this);
                    btnMenu.setEnabled(false);
                    btnMenu.setColorFilter(ContextCompat.getColor(context, R.color.gray_400), android.graphics.PorterDuff.Mode.SRC_IN);
                    if(isSubGroupMember){
                        btnMenu.setEnabled(true);
                        btnMenu.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int[] attrs = new int[]{R.attr.selectableItemBackground};
                    TypedArray typedArray = context.obtainStyledAttributes(attrs);
                    int backgroundResource = typedArray.getResourceId(0, 0);
                    typedArray.recycle();
                    fl.setBackgroundResource(backgroundResource);
                    linearLayout_childItems.addView(fl, layoutParams);
                }
                textView_parentName.setOnClickListener(this);
            }
        }


        private void setTime(String startDateText, String endDateText, TimePickerDialog.OnTimeSetListener timeListener) {
            if(TextUtils.isEmpty(startDateText) || TextUtils.isEmpty(endDateText)){
                Toast.makeText(context,"Primero defina las fechas de la tarea",Toast.LENGTH_SHORT).show();
            }else{
                Calendar now = Calendar.getInstance();
                TimePickerDialog dpd = com.borax12.materialdaterangepicker.time.TimePickerDialog.newInstance(
                        timeListener,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );
                dpd.setAccentColor(R.color.colorPrimary);
                dpd.setTabIndicators("DESDE","HASTA");
                dpd.show(((Activity)RVSubGroupAdapter.this.contexto).getFragmentManager(),"Timepickerdialog");
            }
        }


        @Override
        public void onClick(final View view) {
            int id = view.getId();
            switch (id){
                case R.id.list_item_sub_group_arrow:
                    if(subGroups.get(position).getTasks().size() == 0){
                        Toast.makeText(context, "Aún no hay tareas programadas ;)", Toast.LENGTH_SHORT).show();
                    }else{
                        if (linearLayout_childItems.getVisibility() == View.VISIBLE) {
                            Animation rotation = AnimationUtils.loadAnimation(contexto,R.anim.rotation_down);
                            view.setBackground(null);
                            view.startAnimation(rotation);
                            linearLayout_childItems.setVisibility(View.GONE);
                        } else {
                            Animation rotation = AnimationUtils.loadAnimation(contexto,R.anim.rotation_up);
                            view.setBackground(null);
                            view.startAnimation(rotation);
                            linearLayout_childItems.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case R.id.chat:
                    showDialog(DIALOG_CHAT);
                    break;
                case R.id.members:
                    showDialog(DIALOG_MEMBERS);
                    break;
                case R.id.files:
                    showDialog(DIALOG_FILES);
                    break;
                case R.id.btn_menu:
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();
                    popupMenu.getMenuInflater().inflate(R.menu.task_menu, menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.message:
                                    //ENVIAR MENSAJE
                                    //sendMessage(iduser, context);
                                    //Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.add_to_group:
                                    break;
                                case R.id.delete:
                                    //ELIMINAR CONTACTO
                                    //deleteContact(iduser, context);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                    break;
                case R.id.add_task:
                    new MaterialDialog.Builder(context)
                            .title("Nueva tarea")
                            .content("Nombre")
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("Ingrese el nombre", null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if(!TextUtils.isEmpty(input)){
                                        final Task task = new Task(null,input.toString(),null,null,false);

                                        /*View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent , false);
                                        FrameLayout fl = v.findViewById(R.id.fltask);
                                        fl.setOnClickListener(SubGroupViewHolder.this);
                                        textView = fl.findViewById(R.id.tasktv);
                                        textView.setText(task.getName());
                                        checkBox = fl.findViewById(R.id.checkbox);
                                        //checkBox.setOnClickListener(SubGroupViewHolder.this);
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        CircleImageView btnMenu = fl.findViewById(R.id.btn_menu);
                                        btnMenu.setOnClickListener(SubGroupViewHolder.this);
                                        int[] attrs = new int[]{R.attr.selectableItemBackground};
                                        TypedArray typedArray = context.obtainStyledAttributes(attrs);
                                        int backgroundResource = typedArray.getResourceId(0, 0);
                                        typedArray.recycle();
                                        fl.setBackgroundResource(backgroundResource);
                                        linearLayout_childItems.addView(fl, layoutParams);*/

                                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent , false);
                                        FrameLayout fl = v.findViewById(R.id.fltask);

                                        //Nombre de la tarea
                                        textView = fl.findViewById(R.id.tasktv);
                                        textView.setText(task.getName());

                                        //Fecha de comienzo de la tarea
                                        startDate = fl.findViewById(R.id.taskStartDateTv);
                                        final String startDateText = task.getStartDate();
                                        if(!TextUtils.isEmpty(startDateText)){
                                            startDate.setText(task.getStartDate());
                                        }else{
                                            startDate.setText("No definida");
                                        }

                                        //Fecha de fin de la tarea
                                        endDate = fl.findViewById(R.id.taskEndDateTv);
                                        final String endDateText = task.getEndDate();
                                        if(!TextUtils.isEmpty(endDateText)){
                                            endDate.setText(task.getEndDate());
                                        }else{
                                            endDate.setText("No definida");
                                        }

                                        //Checkbox de la tarea
                                        checkBox = fl.findViewById(R.id.checkbox);
                                        if(task.getFinished()){
                                            checkBox.setChecked(true);
                                        }
                                        //final int finalIndexView = fl.getChildCount();
                                        checkBox.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                finishResumeTask((CheckBox)view,context,subGroups.get(position).getSubGroupKey(),task.getTaskKey());
                                            }
                                        });

                                        //TimeRangePicker listener
                                        final TimePickerDialog.OnTimeSetListener timeListener =  new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
                                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                                        .child(groupKey)
                                                        .child("subgroups")
                                                        .child(subGroups.get(position).getSubGroupKey())
                                                        .child("tasks").child(task.getTaskKey());

                                                String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                                                String minuteString = minute < 10 ? "0"+minute : ""+minute;
                                                String hourStringEnd = hourOfDayEnd < 10 ? "0"+hourOfDayEnd : ""+hourOfDayEnd;
                                                String minuteStringEnd = minuteEnd < 10 ? "0"+minuteEnd : ""+minuteEnd;
                                                String time = "You picked the following time: From - "+hourString+"h"+minuteString+" To - "+hourStringEnd+"h"+minuteStringEnd;

                                                String startTime = hourString+":"+minuteString;
                                                String endTime = hourStringEnd+":"+minuteStringEnd;



                                                FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(linearLayout_childItems.getChildCount()-1);
                                                TextView tvStartDate = fl.findViewById(R.id.taskStartDateTv);
                                                TextView tvEndDate = fl.findViewById(R.id.taskEndDateTv);

                                                String startDate = tvStartDate.getText().toString();
                                                String endDate = tvEndDate.getText().toString();

                                                String startDateTime = startDate+" "+startTime;
                                                String endDateTime = endDate+" "+endTime;

                                                tvStartDate.setText(startDateTime);
                                                tvEndDate.setText(endDateTime);

                                                taskRef.child("startDate").setValue(startDateTime);
                                                taskRef.child("endDate").setValue(endDateTime);

                                                Toast.makeText(context,"Horarios guardados",Toast.LENGTH_SHORT).show();
                                            }
                                        };

                                        //DateRangePicker
                                        final DatePickerDialog.OnDateSetListener dateListener =  new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
                                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                                        .child(groupKey)
                                                        .child("subgroups")
                                                        .child(subGroups.get(position).getSubGroupKey())
                                                        .child("tasks").child(task.getTaskKey());

                                                String newDayOfMonth = String.valueOf(dayOfMonth).length() > 1 ? String.valueOf(dayOfMonth) : "0"+String.valueOf(dayOfMonth);
                                                String newMonthOfYear = String.valueOf(monthOfYear+1).length() > 1 ? String.valueOf(monthOfYear+1) : "0"+String.valueOf(monthOfYear+1);
                                                String newDayOfMonthEnd = String.valueOf(dayOfMonthEnd).length() > 1 ? String.valueOf(dayOfMonthEnd) : "0"+String.valueOf(dayOfMonthEnd);
                                                String newMonthOfYearEnd = String.valueOf(monthOfYearEnd+1).length() > 1 ? String.valueOf(monthOfYearEnd+1) : "0"+String.valueOf(monthOfYearEnd+1);

                                                String startDateTxt = newDayOfMonth + "-" + newMonthOfYear +"-" + String.valueOf(year);
                                                String endDateTxt = newDayOfMonthEnd + "-" + newMonthOfYearEnd +"-" + String.valueOf(yearEnd);

                                                taskRef.child("startDate").setValue(startDateTxt);
                                                taskRef.child("endDate").setValue(endDateTxt);

                                                FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(linearLayout_childItems.getChildCount()-1);
                                                ((TextView)fl.findViewById(R.id.taskStartDateTv)).setText(startDateTxt);
                                                ((TextView)fl.findViewById(R.id.taskEndDateTv)).setText(endDateTxt);

                                                setTime(startDateTxt, endDateTxt, timeListener);

                                                Toast.makeText(context,"Fechas guardadas",Toast.LENGTH_SHORT).show();
                                            }
                                        };

                                        fl.findViewById(R.id.btn_task_calendar).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Calendar now = Calendar.getInstance();
                                                DatePickerDialog dpd = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                                                        dateListener,
                                                        now.get(Calendar.YEAR),
                                                        now.get(Calendar.MONTH),
                                                        now.get(Calendar.DAY_OF_MONTH)
                                                );
                                                dpd.setStartTitle("DESDE");
                                                dpd.setEndTitle("HASTA");
                                                dpd.setAccentColor(R.color.colorPrimary);
                                                dpd.setCancelable(false);
                                                dpd.show(((Activity)RVSubGroupAdapter.this.contexto).getFragmentManager(),"Datepickerdialog");
                                            }
                                        });

                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        CircleImageView btnMenu = fl.findViewById(R.id.btn_menu);
                                        btnMenu.setOnClickListener(SubGroupViewHolder.this);
                                        int[] attrs = new int[]{R.attr.selectableItemBackground};
                                        TypedArray typedArray = context.obtainStyledAttributes(attrs);
                                        int backgroundResource = typedArray.getResourceId(0, 0);
                                        typedArray.recycle();
                                        fl.setBackgroundResource(backgroundResource);
                                        linearLayout_childItems.addView(fl, layoutParams);

                                        updateSubgroup(task,subGroups.get(position).getSubGroupKey(), checkBox,context);

                                        //notifyDataSetChanged();

                                    }else{
                                        dialog.getInputEditText().setError("Este campo es necesario");
                                    }
                                }
                            }).show();
            }
        }

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {

        }

        void showDialog(int dialogType) {

            FragmentManager fragmentManager = ((GroupActivity)contexto).getSupportFragmentManager();

            switch (dialogType){
                case DIALOG_CHAT:
                    SubGroupChatDialogFragment newFragment = new SubGroupChatDialogFragment(textView_parentName.getText().toString(),imageUrl,subGroups.get(position).getSubGroupKey());
                    newFragment.setCancelable(false);
                    newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
                    break;

                case DIALOG_MEMBERS:
                    SubGroupMembersDialogFragment newFragment2 = new SubGroupMembersDialogFragment(textView_parentName.getText().toString(),imageUrl,subGroups.get(position).getSubGroupKey(),groupKey);
                    newFragment2.setCancelable(false);
                    newFragment2.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
                    FragmentTransaction transaction2 = fragmentManager.beginTransaction();
                    transaction2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction2.add(android.R.id.content, newFragment2).addToBackStack(null).commit();
                    break;

                case DIALOG_FILES:
                    SubGroupFilesDialogFragment newFragment3 = new SubGroupFilesDialogFragment(textView_parentName.getText().toString(),imageUrl,subGroups.get(position).getSubGroupKey(),groupKey,((FireApp) context.getApplicationContext()).getGroupName());
                    newFragment3.setCancelable(false);
                    newFragment3.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
                    FragmentTransaction transaction3 = fragmentManager.beginTransaction();
                    transaction3.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction3.add(android.R.id.content, newFragment3).addToBackStack(null).commit();
                    break;
            }
        }
    }

    private void finishResumeTask(final CheckBox checkBox, Context context, String subGroupKey, String taskKey) {

        final DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("tasks").child(taskKey).child("finished");

        if(!checkBox.isChecked()){
            new AlertDialog.Builder(context)
                    .setTitle("¿Está seguro que desea reanudar la tarea?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setChecked(false);
                            taskRef.setValue(false);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setChecked(true);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else{
            new AlertDialog.Builder(context)
                    .setTitle("¿Está seguro que quiere dar por finalizada la tarea?")
                    .setMessage("Se le notificará al administrador del proyecto si la tarea fue finalizada")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setChecked(true);
                            taskRef.setValue(true);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkBox.setChecked(false);
                                }
                            }
                    )
                    .setCancelable(false)
                    .show();
        }
    }

    private void updateSubgroup(Task task, final String subGroupKey, CheckBox checkBox, final Context context) {

        DatabaseReference subGroupTasksRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("tasks");

        final String taskKey = subGroupTasksRef.push().getKey();

        task.setTaskKey(taskKey);

        Map<String,Object> map = new HashMap<>();
        map.put("taskKey",taskKey);
        map.put("name",task.getName());
        map.put("startDate",task.getStartDate());
        map.put("endDate",task.getEndDate());
        map.put("finished",task.getFinished());

        subGroupTasksRef.child(taskKey).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(contexto, "TAREA AGREGADA!", Toast.LENGTH_SHORT).show();
                //RVSubGroupAdapter.this.notifyDataSetChanged();
            }
        });

       checkBox.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               finishResumeTask((CheckBox)view,context,subGroupKey,taskKey);
           }
       });
    }

}





