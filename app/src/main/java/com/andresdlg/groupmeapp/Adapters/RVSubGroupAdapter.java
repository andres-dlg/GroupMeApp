package com.andresdlg.groupmeapp.Adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;
import com.andresdlg.groupmeapp.Entities.Task;
import com.andresdlg.groupmeapp.R;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import java.util.List;

/**
 * Created by andresdlg on 17/02/18.
 */

public class RVSubGroupAdapter extends ExpandableRecyclerViewAdapter<RVSubGroupAdapter.SubGroupViewHolder, RVSubGroupAdapter.TaskViewHolder> {

    public RVSubGroupAdapter(List<? extends ExpandableGroup> tasks) {
        super(tasks);
    }

    @Override
    public SubGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group, parent, false);
        return new SubGroupViewHolder(view);
    }

    @Override
    public TaskViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_group_task, parent, false);
        return new TaskViewHolder(view);
    }


    @Override
    public void onBindChildViewHolder(TaskViewHolder holder, int flatPosition, ExpandableGroup group,
                                      int childIndex) {
        final Task task = (Task) group.getItems().get(childIndex);
        holder.setTaskName(task.getName());
    }

    @Override
    public void onBindGroupViewHolder(SubGroupViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setSubGroupName(group);
    }

    public class SubGroupViewHolder extends GroupViewHolder {

        private TextView name;

        public SubGroupViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item_sub_group_name);
        }

        public void setSubGroupName(ExpandableGroup group) {
            name.setText(group.getTitle());
        }

    }

    public class TaskViewHolder extends CheckableChildViewHolder {

        private CheckedTextView name;

        public TaskViewHolder(View itemView) {
            super(itemView);
            name = (CheckedTextView) itemView.findViewById(R.id.list_item_multicheck_task_name);
        }

        @Override
        public Checkable getCheckable() {
            return name;
        }

        public void setTaskName(String taskName) {
            name.setText(taskName);
        }

    }
}



