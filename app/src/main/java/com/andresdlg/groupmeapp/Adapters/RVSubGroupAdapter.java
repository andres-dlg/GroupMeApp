package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by andresdlg on 17/02/18.
 */

public class RVSubGroupAdapter extends RecyclerView.Adapter<RVSubGroupAdapter.SubGroupViewHolder> {

    private List<SubGroup> subGroups;
    private Context contexto;

    public RVSubGroupAdapter(List<SubGroup> subGroups, Context context) {

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
        //int noOfChild = subGroup.getTasks().size();
        if(subGroup.getTasks() != null){
            int noOfChild = subGroup.getTasks().size();
            //if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            //}
            /*for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                currentTextView.setText(subGroup.getTasks().get(textViewIndex).getName());
                /*currentTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, "" + ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                });*/
            /*}*/
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

    public class SubGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Context context;
        private TextView textView_parentName;
        private LinearLayout linearLayout_childItems;
        CheckBox checkBox;
        TextView textView;

        SubGroupViewHolder(View itemView,int position,ViewGroup parent) {
            super(itemView);
            context = itemView.getContext();
            textView_parentName = itemView.findViewById(R.id.tv_parentName);
            linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
            linearLayout_childItems.setVisibility(View.GONE);
            if(subGroups.get(position).getTasks() != null){
                int intMaxNoOfChild = subGroups.get(position).getTasks().size();
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent, false);
                    FrameLayout fl = v.findViewById(R.id.fltask);
                    fl.setOnClickListener(this);
                    textView = fl.findViewById(R.id.tasktv);
                    textView.setText(subGroups.get(position).getTasks().get(indexView).getName());
                    checkBox = fl.findViewById(R.id.checkbox);
                    checkBox.setOnClickListener(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    CircleImageView btnMenu = fl.findViewById(R.id.btn_menu);
                    btnMenu.setOnClickListener(this);
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

        @Override
        public void onClick(final View view) {
            int id = view.getId();
            switch (id){
                case R.id.tv_parentName:
                    if (linearLayout_childItems.getVisibility() == View.VISIBLE) {
                        linearLayout_childItems.setVisibility(View.GONE);
                    } else {
                        linearLayout_childItems.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.checkbox:
                    finishResumeTask((CheckBox)view,context);

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
            }
        }
    }

    private void finishResumeTask(final CheckBox checkBox, Context context) {
        if(!checkBox.isChecked()){
            new AlertDialog.Builder(context)
                    .setTitle("¿Está seguro que desea reanudar la tarea?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setChecked(false);
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
}





