package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Adapters.RVContactAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FirebaseContacts;

import java.util.List;

/**
 * Created by andresdlg on 02/05/17.
 */

public class FriendListFragment extends Fragment implements FirebaseContacts.OnUserContactsListener {

    RVContactAdapter adapter;
    TextView tvFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends_list,container,false);

        RecyclerView rv = v.findViewById(R.id.rvContactsList);
        rv.setHasFixedSize(true);

        tvFriends = v.findViewById(R.id.tvFriends);

        RelativeLayout.LayoutParams parameter = (RelativeLayout.LayoutParams) rv.getLayoutParams();
        int config = getResources().getConfiguration().orientation;

        int dpValue1 = 55;
        int dpValue2 = 48;
        float d = getResources().getDisplayMetrics().density;
        int margin1 = (int)(dpValue1*d);
        int margin2 = (int)(dpValue2*d);

        if(config == Configuration.ORIENTATION_LANDSCAPE){
            parameter.setMargins(parameter.leftMargin,margin2,parameter.rightMargin,parameter.bottomMargin);
        }else if(config == Configuration.ORIENTATION_PORTRAIT){
            parameter.setMargins(parameter.leftMargin,margin1,parameter.rightMargin,parameter.bottomMargin);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        hideNoContactsTextView();

        adapter = new RVContactAdapter(FirebaseContacts.users, getContext(), false);
        rv.setAdapter(adapter);

        return v;
    }

    private void hideNoContactsTextView() {
        if(FirebaseContacts.users.size()>0){
            tvFriends.setVisibility(View.INVISIBLE);
        }else {
            tvFriends.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        FirebaseContacts.setListener(this);
    }

    @Override
    public void onUserContactsChange(List<Users> users) {
        adapter.notifyDataSetChanged();
        hideNoContactsTextView();
    }
}