package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.DialogFragments.SubGroupChatDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupFilesDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupMembersDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupNewTaskDialogFragment;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.GroupActivity;
import com.andresdlg.groupmeapp.uiPackage.SubGroupDetailActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private final int DIALOG_NEW_TASK = 3;

    private taskTypes actualTaskType;

    private Map<String,Integer> cantidadDeTasks;

    public void setCantidadTasks(String subGroupKey, int size) {
        for(Map.Entry<String, Integer> entry: cantidadDeTasks.entrySet()) {
            if(entry.getKey().equals(subGroupKey)){
                entry.setValue(size);
                return;
            }
        }
        cantidadDeTasks.put(subGroupKey,size);
    }

    public taskTypes checkTasksSize(String subGroupKey, int size) {
        if(size == cantidadDeTasks.get(subGroupKey)){
            return taskTypes.UPDATED_TASK;
        }else if(size > cantidadDeTasks.get(subGroupKey)){
            return taskTypes.NEW_TASK;
        }else{
            return taskTypes.DELETED_TASK;
        }
    }

    public enum taskTypes{
        NEW_TASK,
        UPDATED_TASK,
        DELETED_TASK,
        EXISTING_TASK
    }

    public RVSubGroupAdapter(List<SubGroup> subGroups, String groupKey, Context context) {
        this.groupKey = groupKey;
        this.subGroups = subGroups;
        this.contexto = context;

        actualTaskType = taskTypes.EXISTING_TASK;
        cantidadDeTasks = new HashMap<>();
    }

    @NonNull
    @Override
    public SubGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing, parent, false);
        return new SubGroupViewHolder(v,viewType,parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SubGroupViewHolder holder, int position) {
        SubGroup subGroup = subGroups.get(position);
        holder.textView_parentName.setText(subGroup.getName());

        switch (actualTaskType){

            case NEW_TASK:
                holder.setTasks(actualTaskType,subGroup.getTasks().get(subGroup.getTasks().size()-1));
                break;
            case UPDATED_TASK:
                holder.setTasks(actualTaskType,null);
                break;
            case EXISTING_TASK:
                holder.setTasks(actualTaskType,null);
                break;
            case DELETED_TASK:
                holder.setTasks(actualTaskType,null);
                break;
            default:
                break;
        }

        /*int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
        if(subGroup.getTasks() != null){
            int noOfChild = subGroup.getTasks().size();
            for (int index = noOfChild; index < noOfChildTextViews; index++) {
                FrameLayout currentTextView = (FrameLayout) holder.linearLayout_childItems.getChildAt(index);
            }
        }*/
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

    public void setNewTaskFlag() {
        actualTaskType = taskTypes.NEW_TASK;
    }

    public void setUpdatedTaskFlag() {
        actualTaskType = taskTypes.UPDATED_TASK;
    }

    public void setDeletedTaskFlag() {
        actualTaskType = taskTypes.DELETED_TASK;
    }

    public class SubGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Context context;
        private TextView textView_parentName;
        private LinearLayout linearLayout_childItems;
        int position;
        CheckBox checkBox;
        TextView textView;
        TextView startDate;
        TextView endDate;
        ImageButton addTaskiv;
        ImageButton chat;
        ImageButton membersiv;
        ImageButton files;
        ViewGroup parent;
        LinearLayout cardLl;
        CircleImageView subGroupPhoto;
        ImageView subGroupBg;
        String imageUrl;

        boolean isSubGroupMember;

        SubGroupViewHolder(final View itemView, final int position, ViewGroup parent) {
            super(itemView);
            this.position = position;
            this.parent = parent;
            context = itemView.getContext();

            final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, SubGroupDetailActivity.class);
                    // Pass data object in the bundle and populate details activity.
                    intent.putExtra("subGroupName", subGroups.get(position).getName());
                    intent.putExtra("subGroupPhotoUrl", subGroups.get(position).getImageUrl());
                    intent.putExtra("subGroupKey", subGroups.get(position).getSubGroupKey());
                    intent.putExtra("groupKey", groupKey);
                    Pair<View, String> p1 = Pair.create((View)subGroupPhoto, "photo");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((AppCompatActivity)contexto, p1);
                    context.startActivity(intent, options.toBundle());
                }
            };

            textView_parentName = itemView.findViewById(R.id.tv_parentName);
            textView_parentName.setOnClickListener(onClickListener);

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
            subGroupPhoto.setOnClickListener(onClickListener);

            linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
            linearLayout_childItems.setVisibility(View.GONE);

            subGroupBg = itemView.findViewById(R.id.subGroupBg);
            subGroupBg.setOnClickListener(onClickListener);

            RelativeLayout relativeLayout = itemView.findViewById(R.id.relativeLayout);
            relativeLayout.setOnClickListener(onClickListener);

            isSubGroupMember = false;
            final Map<String,String> members = subGroups.get(position).getMembers();
            for(Map.Entry<String, String> entry: members.entrySet()) {
                if(entry.getKey().equals(StaticFirebaseSettings.currentUserId)){
                    isSubGroupMember = true;
                    break;
                }
            }

            if(!isSubGroupMember){
                chat.setEnabled(false);
                addTaskiv.setEnabled(false);
                membersiv.setEnabled(false);
                files.setEnabled(false);
                cardLl.setBackgroundColor(context.getResources().getColor(R.color.gray_200));
                textView_parentName.setOnClickListener(null);
                subGroupPhoto.setOnClickListener(null);
                subGroupBg.setOnClickListener(null);
                relativeLayout.setOnClickListener(null);
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
                    //Bitmap blurredBitmap = BlurBuilder.blur(context, ((BitmapDrawable) resource).getBitmap());
                    //subGroupBg.setImageBitmap(blurredBitmap);

                    //subGroupBg.setScaleType(ImageView.ScaleType.FIT_XY);
                    //subGroupBg.setImageDrawable((BitmapDrawable)resource);


                    int heightPx = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 203, metrics));
                    int widthPx = metrics.widthPixels;

                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                    subGroupBg.setImageBitmap(Bitmap.createScaledBitmap(bitmap,widthPx,heightPx,true));
                }
            };

            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.background_placeholder);

            //RequestOptions requestOptions2 = new RequestOptions().transform(new BlurTransformation(25,1)).placeholder(R.drawable.background_placeholder);

            //subGroupBg.setTag(target);
            if(imageUrl.equals("https://firebasestorage.googleapis.com/v0/b/groupmeapp-5aaf6.appspot.com/o/ic_launcher.png?alt=media&token=9740457d-49b7-4463-b78c-4c3513d768a7")){
                Glide.with(contexto)
                        .load("")
                        .apply(requestOptions)
                        .into(subGroupBg);
            }else{
                Glide.with(contexto)
                        .load(imageUrl)
                        //.apply(RequestOptions.bitmapTransform(new SupportRSBlurTransformation(25,1)))
                        //.apply(requestOptions2)
                        .into(target);
            }

        }

        private void setTasks(taskTypes mode, final Task task) {
            if(mode == taskTypes.EXISTING_TASK){
                int intMaxNoOfChild = subGroups.get(position).getTasks().size();
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    addTaskToList(subGroups.get(position).getTasks().get(indexView));
                }
            }else if(mode == taskTypes.NEW_TASK){
                addTaskToList(task);
            }else if(mode == taskTypes.UPDATED_TASK){
                linearLayout_childItems.removeAllViews();
                setTasks(taskTypes.EXISTING_TASK,null);
            }else if(mode == taskTypes.DELETED_TASK){
                linearLayout_childItems.removeAllViews();
                setTasks(taskTypes.EXISTING_TASK,null);
            }
        }

        @SuppressLint("SetTextI18n")
        private void addTaskToList(final Task task) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent, false);
            FrameLayout fl = v.findViewById(R.id.fltask);
            if(isSubGroupMember){
                fl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTaskDialog(task, subGroups.get(position).getSubGroupKey(), groupKey);
                    }
                });
            }

            //Nombre de la tarea
            textView = fl.findViewById(R.id.tasktv);
            textView.setText(task.getName());

            //Fecha de comienzo de la tarea
            startDate = fl.findViewById(R.id.taskStartDateTv);
            long startDateLong = task.getStartDate();
            if(startDateLong != 0){
                startDate.setText(formatDate(task.getStartDate()));
            }else{
                startDate.setText("No definida");
            }

            //Fecha de fin de la tarea
            endDate = fl.findViewById(R.id.taskEndDateTv);
            long endDateLong = task.getEndDate();
            if(endDateLong != 0){
                endDate.setText(formatDate(task.getEndDate()));
            }else{
                endDate.setText("No definida");
            }

            //Checkbox de la tarea
            checkBox = fl.findViewById(R.id.checkbox);
            checkBox.setEnabled(false);
            if(task.getFinished()){
                checkBox.setChecked(true);
            }
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishResumeTask((CheckBox)view,context,subGroups.get(position).getSubGroupKey(),task.getTaskKey(),task.getName(),subGroups.get(position).getName(),position);
                }
            });
            if(isSubGroupMember){
                checkBox.setEnabled(true);
            }

            //region OLD CALENDAR BUTTON WITH LISTENERS
            //TimeRangePicker listener
                    /*final TimePickerDialog.OnTimeSetListener timeListener =  new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
                            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                    .child(groupKey)
                                    .child("subgroups")
                                    .child(subGroups.get(position).getSubGroupKey())
                                    .child("tasks").child(subGroups.get(position).getTasks().get(finalIndexView).getTaskKey());

                            FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(finalIndexView);
                            TextView tvStartDate = fl.findViewById(R.id.taskStartDateTv);
                            TextView tvEndDate = fl.findViewById(R.id.taskEndDateTv);

                            long timeStartInMillisWithoutHour = calendarStart.getTimeInMillis();
                            long timeEndInMillisWithoutHour = calendarEnd.getTimeInMillis();

                            long timeStartInMillis = hourOfDay*3600000+minute*60000;
                            long timeEndInMillis = hourOfDayEnd*3600000+minuteEnd*60000;

                            Calendar finalStartDate = Calendar.getInstance();
                            finalStartDate.setTimeInMillis(timeStartInMillisWithoutHour+timeStartInMillis);

                            Calendar finalEndDate = Calendar.getInstance();
                            finalEndDate.setTimeInMillis(timeEndInMillisWithoutHour+timeEndInMillis);

                            tvStartDate.setText(formatDate(finalStartDate.getTimeInMillis()));
                            tvEndDate.setText(formatDate(finalEndDate.getTimeInMillis()));

                            taskRef.child("startDate").setValue(finalStartDate.getTimeInMillis());
                            taskRef.child("endDate").setValue(finalEndDate.getTimeInMillis());

                            Toast.makeText(context,"Horarios guardados",Toast.LENGTH_SHORT).show();
                        }
                    };*/

            //DateRangePicker
                    /*final DatePickerDialog.OnDateSetListener dateListener =  new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
                            calendarStart = Calendar.getInstance();
                            calendarStart.set(year,monthOfYear,dayOfMonth,0,0,0);

                            calendarEnd = Calendar.getInstance();
                            calendarEnd.set(yearEnd,monthOfYearEnd,dayOfMonthEnd,23,59,59);

                            if(calendarStart.after(calendarEnd)){
                                Toast.makeText(context,"Rango de fechas inválido",Toast.LENGTH_SHORT).show();
                            }else{

                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Groups")
                                        .child(groupKey)
                                        .child("subgroups")
                                        .child(subGroups.get(position).getSubGroupKey())
                                        .child("tasks").child(subGroups.get(position).getTasks().get(finalIndexView).getTaskKey());

                                FrameLayout fl = (FrameLayout)linearLayout_childItems.getChildAt(finalIndexView);
                                TextView tvStartDate = fl.findViewById(R.id.taskStartDateTv);
                                TextView tvEndDate = fl.findViewById(R.id.taskEndDateTv);

                                tvStartDate.setText(formatDate(calendarStart.getTimeInMillis()));
                                tvEndDate.setText(formatDate(calendarEnd.getTimeInMillis()));

                                taskRef.child("startDate").setValue(calendarStart.getTimeInMillis());
                                taskRef.child("endDate").setValue(calendarEnd.getTimeInMillis());

                                Toast.makeText(context,"Horarios guardados",Toast.LENGTH_SHORT).show();


                                // POR AHORA SOLO SE GUARDARAN LAS FECHAS PARA ALL EL DIA
                                //setTime2(timeListener);
                                //Toast.makeText(context,"Ahora elije a que hora comenzará la primer tarea y a que hora terminará la ultima",Toast.LENGTH_SHORT).show();
                            }
                        }
                    };*/

                    /*CircleImageView btnTaskCalendar = fl.findViewById(R.id.btn_task_calendar);
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
                            dpd.setAutoHighlight(true);
                            dpd.show(((Activity)RVSubGroupAdapter.this.contexto).getFragmentManager(),"Datepickerdialog");
                        }
                    });

                    if(isSubGroupMember){
                        btnTaskCalendar.setEnabled(true);
                        btnTaskCalendar.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                    }*/
            //endregion

            //Menu (3 puntitos)
            CircleImageView btnMenu = fl.findViewById(R.id.btn_menu);
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(context, v);
                    final Menu menu = popupMenu.getMenu();
                    popupMenu.getMenuInflater().inflate(R.menu.task_menu, menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                /*case R.id.comment:
                                    break;*/
                                case R.id.delete:
                                    deleteTask(task.getTaskKey(),subGroups.get(position).getSubGroupKey(),groupKey);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
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
                case R.id.add_task:
                    showDialog(DIALOG_NEW_TASK);
                    break;
            }
        }

        private void deleteTask(final String taskKey, final String subGroupKey, final String groupKey) {
            new AlertDialog.Builder(context)
                    .setTitle("¿Está seguro que desea eliminar la tarea?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference taskRef = FirebaseDatabase
                                    .getInstance()
                                    .getReference("Groups")
                                    .child(groupKey)
                                    .child("subgroups")
                                    .child(subGroupKey)
                                    .child("tasks")
                                    .child(taskKey);
                            taskRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error al eliminar tarea", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
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

                case DIALOG_NEW_TASK:
                    SubGroupNewTaskDialogFragment newFragment4 = new SubGroupNewTaskDialogFragment(subGroups.get(position).getSubGroupKey(),groupKey);
                    newFragment4.setCancelable(false);
                    newFragment4.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
                    FragmentTransaction transaction4 = fragmentManager.beginTransaction();
                    transaction4.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction4.add(android.R.id.content, newFragment4).addToBackStack(null).commit();
                    break;
            }
        }
    }

    private void showTaskDialog(Task task, String subGroupKey, String groupKey) {
        FragmentManager fragmentManager = ((GroupActivity)contexto).getSupportFragmentManager();
        SubGroupNewTaskDialogFragment newFragment4 = new SubGroupNewTaskDialogFragment(subGroupKey,groupKey, task);
        newFragment4.setCancelable(false);
        newFragment4.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction4 = fragmentManager.beginTransaction();
        transaction4.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction4.add(android.R.id.content, newFragment4).addToBackStack(null).commit();
    }


    private void finishResumeTask(final CheckBox checkBox, final Context context, final String subGroupKey, final String taskKey, final String taskName, final String subgroupName, final int position) {

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
                    .setMessage("Se le notificará al administrador del subgrupo que la tarea fue finalizada")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendNotificationToSubgroupAdmins(subgroupName,taskName,position);
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

    private void sendNotificationToSubgroupAdmins(String subGroupName, String taskName, int position) {
        SubGroup s = subGroups.get(position);
        for(Map.Entry<String, String> entry: s.getMembers().entrySet()) {
            String userid = entry.getKey();
            if(!userid.equals(StaticFirebaseSettings.currentUserId) && entry.getValue().equals(Roles.SUBGROUP_ADMIN.toString())){
                DatabaseReference userToNotifications = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("notifications");
                String notificationKey = userToNotifications.push().getKey();
                Map<String,Object> notification = new HashMap<>();
                notification.put("notificationKey",notificationKey);
                notification.put("title","Tarea finalizada en " + subGroupName);
                notification.put("message","La tarea " + taskName + " ha sido finalizada en " + subGroupName);
                notification.put("from", groupKey);
                notification.put("state", NotificationStatus.UNREAD);
                notification.put("date", Calendar.getInstance().getTimeInMillis());
                notification.put("type", NotificationTypes.TASK_FINISHED);
                userToNotifications.child(notificationKey).setValue(notification);
            }
        }
    }

    private String formatDate(long timeInMillis){
        Date date = new Date(timeInMillis);
        //SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm")  ;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd/MM/yyyy")  ;
        return simpleDateFormater.format(date);
    }

}





