package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andresdlg.groupmeapp.Adapters.RVSearchContactAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ReciclerViewClickListener.RecyclerClick_Listener;
import com.andresdlg.groupmeapp.uiPackage.ReciclerViewClickListener.RecyclerTouchListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 05/02/18.
 */

public class GroupAddMembersFragment extends Fragment implements RVSearchContactAdapter.UsersAdapterListener{

    private SearchView searchView;
    List<Users> users;
    DatabaseReference firebaseContacts;
    RVSearchContactAdapter rvSearchContactAdapter;
    RecyclerView rvAddGroupMember;

    CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    int selected;

    //Multiselect stuff
    private ActionMode mActionMode;
    private boolean isShowing;
    private int pxUp;

    View v;

    OnUserSelectionSetListener mOnUserSelectionSetListener;

    List<Users> groupUsers;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups_dialog_add_contacts, container, false);

        coordinatorLayout = view.findViewById(R.id.clgroupaddmembers);

        //RECYCLERVIEW INITIALIZATION
        rvAddGroupMember = view.findViewById(R.id.rvAddGroupMember);
        rvAddGroupMember.setHasFixedSize(true);
        rvAddGroupMember.setItemAnimator(new DefaultItemAnimator());

        users = new ArrayList<>();
        //groupUsers = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvAddGroupMember.setLayoutManager(llm);

        rvSearchContactAdapter = new RVSearchContactAdapter(users,getContext(),null, ((FireApp) getActivity().getApplication()).getGroupKey(), null);
        rvAddGroupMember.setAdapter(rvSearchContactAdapter);

        searchView = view.findViewById(R.id.toolbar);
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                rvSearchContactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                rvSearchContactAdapter.getFilter().filter(query);
                return false;
            }
        });



        Fragment parentFragment = getParentFragment();


        snackbar = Snackbar.make(parentFragment.getView().findViewById(R.id.viewpager), selected + " Seleccionados",Snackbar.LENGTH_INDEFINITE);
        List<Fragment> fragments = parentFragment.getChildFragmentManager().getFragments();
        v = fragments.get(0).getView().findViewById(R.id.nsSetup);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        pxUp = Math.round(50 * (displayMetrics.xdpi) / DisplayMetrics.DENSITY_DEFAULT);


        //groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();
        groupUsers = ((FireApp) getActivity().getApplication()).getGroupUsers();

        fetchContacts();

        //Aca arranca el multiselect
        implementRecyclerViewClickListeners();

        return view;
    }

    private void implementRecyclerViewClickListeners() {
        rvAddGroupMember.addOnItemTouchListener(new RecyclerTouchListener(getContext(), rvAddGroupMember, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                    onListItemSelect(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
                //onListItemSelect(position);
            }
        }));
    }

    @Override
    public void onContactSelected(Users user) {

    }

    private void fetchContacts() {
        final String groupKey = ((FireApp) getActivity().getApplication()).getGroupKey();

        if(groupKey == null){
            firebaseContacts = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
            firebaseContacts.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //users.clear();
                    rvSearchContactAdapter.notifyDataSetChanged();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        //Getting the data from snapshot
                        if(postSnapshot.child("status").getValue().equals(FriendshipStatus.ACCEPTED.toString())){
                            getUser(postSnapshot.getKey(),groupKey);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            for(Users u: groupUsers){
                users.add(u);
                rvSearchContactAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getUser(final String userKey, String groupKey) {
        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users").child(userKey);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //users.clear();
                Users u = dataSnapshot.getValue(Users.class);
                if(groupUsers == null){
                    if(!users.contains(u)){
                        users.add(u);
                        rvSearchContactAdapter.notifyDataSetChanged();
                        //rvSearchContactAdapter.setUsers(users);
                    }
                }else{
                    if(!users.contains(u) && validateExistingMembers(u)){
                        users.add(u);
                        rvSearchContactAdapter.notifyDataSetChanged();
                        //rvSearchContactAdapter.setUsers(users);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean validateExistingMembers(Users u) {
        for(Users user : groupUsers){
            if(u.getUserid().equals(user.getUserid())){
                return true;
            }
        }
        return false;
    }

    private void onListItemSelect(int position) {

        mOnUserSelectionSetListener.onUserSelectionSet(rvSearchContactAdapter.getSelectedIds());

        rvSearchContactAdapter.toggleSelection(position);//Toggle the selection
        selected = rvSearchContactAdapter.getSelectedCount();
        snackbar.setText( selected + ((selected > 1 ? " Seleccionados" : " Seleccionado")));
        if(selected > 0 && !isShowing){
            //rvAddGroupMember.setTranslationY(pxUp*-1);
            rvAddGroupMember.setPadding(0,0,0,pxUp);
            snackbar.show();
            isShowing = true;
            v.setPadding(0,0,0,pxUp);
        }else if(selected == 0 && isShowing){
            rvAddGroupMember.setPadding(0,0,0,0);
            v.setPadding(0,0,0,0);
            //rvAddGroupMember.setTranslationY(pxDown);
            snackbar.dismiss();
            isShowing = false;
        }
    }

    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }

    public interface OnUserSelectionSetListener{
        public void onUserSelectionSet(List<String> userIds);
    }

    public void onAttachToParentFragment(Fragment fragment){
        try {
            mOnUserSelectionSetListener = (OnUserSelectionSetListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(fragment.toString() + " must implement OnUserSelectionSetListener");
        }
    }

    /*@Override
    public void onResume() {
        super.onResume();
        if(users == null){
            users = new ArrayList<>();
        }else{
            users.clear();
        }
        fetchContacts();
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }
}
