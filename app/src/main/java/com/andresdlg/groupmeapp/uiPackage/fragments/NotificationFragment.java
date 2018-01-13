package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVNotificationAdapter;
import com.andresdlg.groupmeapp.Entities.Notification;
import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotificationResult;
    RVNotificationAdapter adapter;
    TextView tvNoNotifications;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications,container,false);
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Notification> notifications = new ArrayList<>();





        RecyclerView rv = view.findViewById(R.id.rvNotifications);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        adapter = new RVNotificationAdapter(notifications);
        rv.setAdapter(adapter);

        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        checkNotificationsQuantity();
    }

    private void checkNotificationsQuantity() {
        if(adapter.getItemCount() == 0){
            tvNoNotifications.setVisibility(View.VISIBLE);
        }
    }
}
