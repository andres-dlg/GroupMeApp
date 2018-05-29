package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVNotificationAdapter;
import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class NotificationFragment extends Fragment {

    RVNotificationAdapter adapter;
    TextView tvNoNotifications;
    DatabaseReference firebaseNotifications;
    List<Notification> notifications = new ArrayList<>();

    OnNewNotificationSetListener mOnNewNotificationSetListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        onAttachToParentFragment(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications,container,false);

        RecyclerView rv = v.findViewById(R.id.rvNotifications);
        rv.setHasFixedSize(true);
        rv.setItemViewCacheSize(100);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        adapter = new RVNotificationAdapter(notifications, getContext());
        rv.setAdapter(adapter);

        tvNoNotifications = v.findViewById(R.id.tvNoNotifications);

        firebaseNotifications = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("notifications");
        firebaseNotifications.keepSynced(true);
        firebaseNotifications.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //notifications.clear();
                int cantNoti = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    Notification n = postSnapshot.getValue(Notification.class);
                    if(n.getState().equals(NotificationStatus.UNREAD.toString())){
                        cantNoti += 1;
                    }
                    /*notifications.add(0,n);
                    adapter.notifyDataSetChanged();*/
                    updateNotifications(n);
                    tvNoNotifications.setVisibility(View.INVISIBLE);
                }

                mOnNewNotificationSetListener.onNewNotificationSet(cantNoti);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    private void updateNotifications(Notification notification) {
        boolean exists = false;
        for(int i=0; i < notifications.size(); i++){
            if(notifications.get(i).getNotificationKey().equals(notification.getNotificationKey())){
                exists = true;
                notifications.remove(i);
                notifications.add(i,notification);
                //adapter.notifyItemChanged(i);
            }
        }
        if(!exists){
            notifications.add(0,notification);
            adapter.notifyDataSetChanged();
            //adapter.notifyItemInserted(0);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isAdded()){
            if(isVisibleToUser && notifications.size()>0){
                for(Notification notification : notifications){
                    if(notification.getState().equals(NotificationStatus.UNREAD.toString())){
                        notification.setState(NotificationStatus.READ.toString());
                        firebaseNotifications
                                .child(notification.getNotificationKey())
                                .child("state")
                                .setValue(NotificationStatus.READ).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }else {
                adapter.updateNotificationStates();
            }
        }
    }

    public interface OnNewNotificationSetListener{
        void onNewNotificationSet(int notificationQuantity);
    }

    public void onAttachToParentFragment(FragmentActivity activity){
        try {
            mOnNewNotificationSetListener = (OnNewNotificationSetListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnUserSelectionSetListener");
        }
    }
}
