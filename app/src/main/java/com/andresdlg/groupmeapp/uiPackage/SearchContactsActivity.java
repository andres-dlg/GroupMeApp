package com.andresdlg.groupmeapp.uiPackage;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVSearchContactAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchContactsActivity extends AppCompatActivity implements RVSearchContactAdapter.UsersAdapterListener{

    //private MaterialSearchView searchView;
    private SearchView searchView;
    List<Users> users;
    DatabaseReference firebaseContacts;
    RVSearchContactAdapter rvSearchContactAdapter;
    RecyclerView rvAddGroupMember;


    //Multiselect stuff
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_groups_dialog_add_contact_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Agregar miembros");

        //RECYCLERVIEW INITIALIZATION
        rvAddGroupMember = findViewById(R.id.rvAddGroupMember);
        rvAddGroupMember.setHasFixedSize(true);
        rvAddGroupMember.setItemAnimator(new DefaultItemAnimator());

        users = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvAddGroupMember.setLayoutManager(llm);

        rvSearchContactAdapter = new RVSearchContactAdapter(users,this, this);
        rvAddGroupMember.setAdapter(rvSearchContactAdapter);

        fetchContacts();

        //Aca arranca el multiselect
        implementRecyclerViewClickListeners();

    }

    private void implementRecyclerViewClickListeners() {
        rvAddGroupMember.addOnItemTouchListener(new RecyclerTouchListener(this, rvAddGroupMember, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {

                //If ActionMode not null select item
                if (mActionMode != null)
                    onListItemSelect(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Select item on long click
                onListItemSelect(position);
            }
        }));
    }

    private void fetchContacts() {
        firebaseContacts = FirebaseDatabase.getInstance().getReference("Users").child(StaticFirebaseSettings.currentUserId).child("friends");
        firebaseContacts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                rvSearchContactAdapter.notifyDataSetChanged();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    if(postSnapshot.child("status").getValue().equals(FriendshipStatus.ACCEPTED.toString())){
                        getUser(postSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //List item select method
    private void onListItemSelect(int position) {
        rvSearchContactAdapter.toggleSelection(position);//Toggle the selection

        boolean hasCheckedItems = rvSearchContactAdapter.getSelectedCount() > 0;//Check if any items are already selected or not
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = this.startSupportActionMode(new Toolbar_ActionMode_Callback(this,rvSearchContactAdapter, (ArrayList<Users>) users, false));
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(rvSearchContactAdapter
                    .getSelectedCount()) + " Seleccionados");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.fragment_groups_add_contact_activity_menu, menu);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

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
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/

    private void getUser(String key) {
        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users").child(key);
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if(!users.contains(u)){
                    users.add(u);
                    rvSearchContactAdapter.notifyDataSetChanged();
                    //rvSearchContactAdapter.setUsers(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onContactSelected(Users user) {
        Toast.makeText(getApplicationContext(), "Selected: " + user.getName(), Toast.LENGTH_LONG).show();
    }

    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null;
    }
}





