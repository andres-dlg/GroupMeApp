package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.firebasePackage.FilterableFirebaseArray;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by andresdlg on 03/01/18.
 */

public class AddFriendsDialogFragment extends DialogFragment {

    MaterialSearchView searchView;
    private RecyclerView rvContactsResult;
    DatabaseReference mUserDatabase;

    public AddFriendsDialogFragment(){
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main_add_friends_dialog, container, false);


        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        //TOOLBAR INITIALIZATION
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Buscar alias");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));

        assert (getActivity()) != null;
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);


        //RECYCLERVIEW INITIALIZATION
        rvContactsResult = view.findViewById(R.id.rvContactsResult);
        rvContactsResult.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContactsResult.setHasFixedSize(true);


        searchView = view.findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setHint("Buscar");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseUserSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_add_friends_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        menu.removeItem(R.id.contacts);
        menu.removeItem(R.id.add_contact);
        searchView.setMenuItem(item);
    }

    //MANEJO DEL RESULTADO DE LA BUSQUEDA
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if(dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id==android.R.id.home){
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void firebaseUserSearch(String query){

        //La query funciona bien
        final Query firebaseQuery = mUserDatabase.orderByChild("alias")
                                           .startAt(query)
                                           .endAt(query + "\uf8ff")
                                           .limitToFirst(100);

        ClassSnapshotParser<Users> parser = new ClassSnapshotParser<>(Users.class);
        FilterableFirebaseArray filterableFirebaseArray = new FilterableFirebaseArray(firebaseQuery, parser);
        filterableFirebaseArray.addExclude(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setSnapshotArray(filterableFirebaseArray)
                        //.setQuery(filterableFirebaseArray, Users.class)
                        .build();

        final FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_add_contact_list, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                    holder.setDetails(getContext(),model.getName(),model.getAlias(),model.getImageURL(),model.getUserid());
            }
        };

        firebaseRecyclerAdapter.startListening();
        rvContactsResult.setAdapter(firebaseRecyclerAdapter);
    }


    //VIEW HOLDER CLASS

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        void setDetails(final Context context, String contactName, final String contactAlias, String contactPhoto, final String id){
            CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);
            CircleImageView mContactAdd = mView.findViewById(R.id.btn_add_contact);

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            Picasso.with(context).load(contactPhoto).into(mContactPhoto);

            mContactAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String userFrom = FirebaseAuth.getInstance().getUid();

                    DatabaseReference userTo = FirebaseDatabase
                            .getInstance()
                            .getReference("Users")
                            .child(id)
                            .child("notifications");

                    String notificationKey = userTo.push().getKey();

                    Map<String,Object> notification = new HashMap<>();
                    notification.put("title","Solicitud de amistad");
                    notification.put("message","Has recibido una solicitud de amistad de ");
                    notification.put("from",userFrom);
                    notification.put("state", NotificationStatus.UNREAD);
                    notification.put("date", Calendar.getInstance().getTime());
                    notification.put("type", NotificationTypes.FRIENDSHIP);

                    userTo.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

    }

}
