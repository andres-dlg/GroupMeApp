package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.DialogFragments.AttendantsDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.HaveSeenThePostDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.NewMeetingDialogFragment;
import com.andresdlg.groupmeapp.DialogFragments.SubGroupMembersDialogFragment;
import com.andresdlg.groupmeapp.Entities.Meeting;
import com.andresdlg.groupmeapp.Entities.Post;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.DateFormatter;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.MeetingsActivity;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by andresdlg on 11/07/17.
 */

public class RVMeetingsAdapter extends RecyclerView.Adapter<RVMeetingsAdapter.MeetingsViewHolder> {

    private Context context;
    private List<Meeting> meetings;
    private String groupKey;

    public RVMeetingsAdapter(Context context, List<Meeting> meetings, String groupKey){
        this.context = context;
        this.meetings = meetings;
        this.groupKey = groupKey;
    }

    @NonNull
    @Override
    public MeetingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_meetings_cardview, parent, false);
        return new MeetingsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingsViewHolder groupViewHolder, int position) {
        Meeting meeting = meetings.get(position);
        groupViewHolder.setDetails(meeting);
        groupViewHolder.hideDetailsButton(meeting);
        groupViewHolder.hideMenuButton(meeting);
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class MeetingsViewHolder extends RecyclerView.ViewHolder {

        TextView meetingTitle;
        TextView meetingPlace;
        TextView meetingStartTime;
        TextView meetingEndTime;
        TextView meetingAttendantsNum;
        TextView meetingAttendantsText;
        TextView meetingDetails;
        ImageButton btnMenu;
        Button btnMeetingDetails;
        LinearLayout meetingAttendantsBtn;
        View meetingDetailsDivider;

        boolean detailsBoxIsOpen;

        MeetingsViewHolder(View itemView) {
            super(itemView);
            meetingTitle = itemView.findViewById(R.id.meeting_title);
            meetingPlace = itemView.findViewById(R.id.meeting_place);
            meetingStartTime = itemView.findViewById(R.id.meeting_start_time);
            meetingEndTime = itemView.findViewById(R.id.meeting_end_time);
            meetingAttendantsNum = itemView.findViewById(R.id.meeting_attendants_btn_num);
            meetingAttendantsText = itemView.findViewById(R.id.meeting_attendants_btn_text);
            meetingDetails = itemView.findViewById(R.id.meeting_details);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnMeetingDetails = itemView.findViewById(R.id.see_meeting_details);
            meetingAttendantsBtn = itemView.findViewById(R.id.meeting_attendants_btn);
            meetingDetailsDivider = itemView.findViewById(R.id.meeting_details_divider);
            detailsBoxIsOpen = false;
        }

        void setDetails(final Meeting meeting) {
            meetingTitle.setText(meeting.getTitle());meetingTitle.setSelected(true);
            meetingPlace.setText(meeting.getPlace());meetingPlace.setSelected(true);
            meetingStartTime.setText(DateFormatter.formatDate(meeting.getStartTime()));
            meetingEndTime.setText(DateFormatter.formatDate(meeting.getEndTime()));
            meetingDetails.setText(meeting.getDetails());
            meetingAttendantsNum.setText(String.valueOf(meeting.getGuestsIds().size()));
            meetingAttendantsText.setText(meeting.getGuestsIds().size() > 1 ? "invitados" : "invitado");
            meetingAttendantsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AttendantsDialogFragment newFragment2 = new AttendantsDialogFragment(meeting.getGuestsIds());
                    newFragment2.setCancelable(false);
                    newFragment2.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
                    FragmentTransaction transaction2 = ((MeetingsActivity)context).getSupportFragmentManager().beginTransaction();
                    transaction2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction2.add(android.R.id.content, newFragment2).addToBackStack(null).commit();
                }
            });
        }

        void hideDetailsButton(final Meeting meeting) {
            String details = meeting.getDetails();
            if(details == null || TextUtils.isEmpty(details)){
                btnMeetingDetails.setVisibility(View.GONE);
            }else{
                btnMeetingDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        detailsBoxIsOpen = !detailsBoxIsOpen;
                        if(detailsBoxIsOpen){
                            meetingDetails.setVisibility(View.VISIBLE);
                            meetingDetailsDivider.setVisibility(View.VISIBLE);
                            btnMeetingDetails.setText("OCULTAR");
                        }else{
                            meetingDetails.setVisibility(View.GONE);
                            meetingDetailsDivider.setVisibility(View.GONE);
                            btnMeetingDetails.setText("VER DETALLES");
                        }
                    }
                });
            }
        }

        void hideMenuButton(Meeting meeting) {
            if(!meeting.getAuthorId().equals(StaticFirebaseSettings.currentUserId)){
                btnMenu.setVisibility(View.GONE);
            }else{
                btnMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(context, view);
                        Menu menu = popupMenu.getMenu();
                        popupMenu.getMenuInflater().inflate(R.menu.activity_meetings_item_menu, menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()){
                                    case R.id.edit:
                                        return true;
                                    case R.id.delete:
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        }
    }

    private void showNewMeetingDialog(Meeting meeting) {
        FragmentManager fragmentManager = ((MeetingsActivity)context).getSupportFragmentManager();
        NewMeetingDialogFragment newFragment = new NewMeetingDialogFragment(groupKey,meeting);
        newFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme_DialogFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

}

