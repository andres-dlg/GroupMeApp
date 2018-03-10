package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;

import java.util.List;

/**
 * Created by andresdlg on 17/02/18.
 */

public class RVSubGroupAdapter extends RecyclerView.Adapter<RVSubGroupAdapter.SubGroupViewHolder> {

    private List<SubGroup> subGroups;

    public RVSubGroupAdapter(List<SubGroup> subGroups) {

        this.subGroups = subGroups;
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
        CheckedTextView ctv;

        SubGroupViewHolder(View itemView,int position,ViewGroup parent) {
            /*super(itemView);
            context = itemView.getContext();
            textView_parentName = itemView.findViewById(R.id.tv_parentName);
            linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
            linearLayout_childItems.setVisibility(View.GONE);
            if(subGroups.get(position).getTasks() != null){
                int intMaxNoOfChild = subGroups.get(position).getTasks().size();
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    TextView textView = new TextView(context);
                    textView.setId(indexView);
                    textView.setPadding(30, 30, 0, 30);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    textView.setOnClickListener(this);
                    linearLayout_childItems.addView(textView, layoutParams);
                }
                textView_parentName.setOnClickListener(this);
            }*/

            super(itemView);
            context = itemView.getContext();
            textView_parentName = itemView.findViewById(R.id.tv_parentName);
            linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
            linearLayout_childItems.setVisibility(View.GONE);
            if(subGroups.get(position).getTasks() != null){
                int intMaxNoOfChild = subGroups.get(position).getTasks().size();
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent, false);
                    final FrameLayout fl = v.findViewById(R.id.fltask);
                    ctv = fl.findViewById(R.id.list_item_multicheck_task_name);
                    ctv.setText(subGroups.get(position).getTasks().get(indexView).getName());
                    //ctv.setOnClickListener(this);
                    ctv.setId(indexView);
                    //ctv.setClickable(true);
                    ctv.setPadding(30, 30, 0, 30);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int[] attrs = new int[]{R.attr.selectableItemBackground};
                    TypedArray typedArray = context.obtainStyledAttributes(attrs);

                    int backgroundResource = typedArray.getResourceId(0, 0);
                    typedArray.recycle();
                    fl.setBackgroundResource(backgroundResource);
                    fl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((CheckedTextView) fl.findViewById(R.id.list_item_multicheck_task_name)).toggle();
                        }
                    });
                    linearLayout_childItems.addView(fl, layoutParams);

                }
                textView_parentName.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.tv_parentName) {
                if (linearLayout_childItems.getVisibility() == View.VISIBLE) {
                    linearLayout_childItems.setVisibility(View.GONE);
                } else {
                    linearLayout_childItems.setVisibility(View.VISIBLE);
                }
            } else {
                /*TextView textViewClicked = (TextView) view;
                Toast.makeText(context, "" + textViewClicked.getText().toString(), Toast.LENGTH_SHORT).show();
            */
                //CheckedTextView chkBox = (CheckedTextView) findViewById(R.id.CheckedTextView01);
                /*view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        CheckedTextView check = v.findViewById(R.id.list_item_multicheck_task_name);
                        //((CheckedTextView) v).toggle();
                        check.toggle();
                    }
                });*/
            }
        }
    }
}





