package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.firebasePackage.FilterableFirebaseArray;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.UserProfileSetupActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by andresdlg on 03/01/18.
 */

public class AddFriendsDialogFragment extends DialogFragment {

    MaterialSearchView searchView;
    RecyclerView rvContactsResult;
    DatabaseReference mUserDatabase;
    TextView tvSearchUsers;
    ProgressBar progressBar;

    List<String> results;
    private boolean isSeaching;

    public AddFriendsDialogFragment(){
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main_add_friends_dialog, container, false);

        progressBar = view.findViewById(R.id.progressBar);

        tvSearchUsers = view.findViewById(R.id.tvSearchUsers);

        results = new ArrayList<>();

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        //TOOLBAR INITIALIZATION
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Buscar por nombre o alias");
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

        /*FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.child("alias").getValue() != null){
                        String alias = data.child("alias").getValue().toString();
                        if(!results.contains(alias)){
                            results.add(alias);
                        }
                    }
                }

                searchView.setSuggestions(results.toArray(new String[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        searchView.setHint("Buscar");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseUserSearch(query.toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if(newText.length() > 0 ){
                    tvSearchUsers.setVisibility(View.GONE);
                }else{
                    if(rvContactsResult.getChildCount() == 0){
                        if(!isSeaching){
                            tvSearchUsers.setVisibility(View.VISIBLE);
                        }
                    }
                }
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


    private void firebaseUserSearch(final String query){
        //La query funciona bien
        tvSearchUsers.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        isSeaching = true;

        final Query firebaseQuery;
        if(query.charAt(0) == '@'){
            firebaseQuery = mUserDatabase.orderByChild("alias")
                    .startAt(query.substring(1))
                    .endAt(query.substring(1) + "\uf8ff")
                    .limitToFirst(100);
        }else{
            firebaseQuery = mUserDatabase.orderByChild("lowerCaseName")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limitToFirst(100);
        }

        //Esto es para esconder o no el textview
        firebaseQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                isSeaching = false;
                if(dataSnapshot.hasChildren()){
                    tvSearchUsers.setVisibility(View.GONE);
                }else {
                    if(rvContactsResult.getChildCount() == 0){
                        tvSearchUsers.setVisibility(View.VISIBLE);
                        tvSearchUsers.setText(String.format("No se encontraron contactos para la búsqueda: \"%s\"", query));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ClassSnapshotParser<Users> parser = new ClassSnapshotParser<>(Users.class);
        FilterableFirebaseArray filterableFirebaseArray = new FilterableFirebaseArray(firebaseQuery, parser);
        filterableFirebaseArray.addExclude(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setSnapshotArray(filterableFirebaseArray)
                        .build();

        final FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_add_contact_list, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final Users model) {
                    holder.setDetails(getContext(),model.getName(),model.getAlias(),model.getImageURL(),model.getUserid());
            }
        };

        firebaseRecyclerAdapter.startListening();
        rvContactsResult.setAdapter(firebaseRecyclerAdapter);
    }


    //VIEW HOLDER CLASS

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton mContactAdd;

        UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContactAdd = mView.findViewById(R.id.btn_add_contact);
        }

        void setDetails(final Context context, String contactName, final String contactAlias, final String contactPhoto, final String id){
            final CircleImageView mContactPhoto = mView.findViewById(R.id.contact_photo);
            TextView mContactName = mView.findViewById(R.id.contact_name);
            TextView mContactAlias = mView.findViewById(R.id.contact_alias);
            RelativeLayout rl = mView.findViewById(R.id.rl);

            //Reviso si ya se envio la solicitud al usuario. En ese caso cambio el icono y deshabilito envio
            wasSent(id, context);

            mContactAlias.setText(String.format("@%s", contactAlias));
            mContactAlias.setSelected(true);

            mContactName.setText(contactName);
            mContactName.setSelected(true);

            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userProfileIntent = new Intent(context, UserProfileSetupActivity.class);
                    userProfileIntent.putExtra("iduser",id);
                    Pair<View, String> p1 = Pair.create((View)mContactPhoto, "userPhoto");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((AppCompatActivity)context, p1);
                    context.startActivity(userProfileIntent, options.toBundle());
                }
            });

            Glide.with(context)
                    .load(contactPhoto)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            itemView.findViewById(R.id.homeprogress).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mContactPhoto);
        }

        private void wasSent(final String id, final Context context) {

            //Obtengo el reloj de arena y lo pinto
            final Drawable mDrawablePending = ContextCompat.getDrawable(context,R.drawable.ic_timer_sand_black_24dp);
            //mDrawablePending.setTint(context.getResources().getColor(R.color.add_photo));

            //Obtengo el reloj de arena y lo pinto
            final Drawable mDrawableAccepted = ContextCompat.getDrawable(context,R.drawable.ic_account_check_black_24dp);
            //mDrawableAccepted.setTint(context.getResources().getColor(R.color.add_photo));

            final String idCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(id).child("friends").child(idCurrentUser);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot data) {
                    //mContactAdd = mView.findViewById(R.id.btn_add_contact);
                    if(data.child("status").getValue()!= null && data.child("status").getValue().equals(FriendshipStatus.PENDING.toString())){
                        mContactAdd.setImageDrawable(mDrawablePending);
                        mContactAdd.setEnabled(false);
                    }
                    else if(data.child("status").getValue() != null && data.child("status").getValue().equals(FriendshipStatus.ACCEPTED.toString())){
                        mContactAdd.setImageDrawable(mDrawableAccepted);
                        mContactAdd.setEnabled(false);
                    }
                    else{
                        mContactAdd.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_account_plus_black_24dp));
                        mContactAdd.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mContactAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final String userFrom = StaticFirebaseSettings.currentUserId;

                    FirebaseDatabase
                            .getInstance()
                            .getReference("Users")
                            .child(userFrom).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Users me = dataSnapshot.getValue(Users.class);

                            DatabaseReference userTo = FirebaseDatabase
                                    .getInstance()
                                    .getReference("Users")
                                    .child(id);

                            //Envio y almacenamiento de notificación
                            DatabaseReference userToNotifications = userTo.child("notifications");
                            String notificationKey = userToNotifications.push().getKey();
                            Map<String,Object> notification = new HashMap<>();
                            notification.put("notificationKey",notificationKey);
                            notification.put("title","Solicitud de amistad");
                            notification.put("message","Has recibido una solicitud de amistad de " + me.getName());
                            notification.put("from",userFrom);
                            notification.put("state", NotificationStatus.UNREAD);
                            notification.put("date", Calendar.getInstance().getTimeInMillis());
                            notification.put("type", NotificationTypes.FRIENDSHIP);

                            userToNotifications.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //Almacenamiento de nodo friend
                            DatabaseReference userToFriends = userTo.child("friends");
                            Map<String,Object> friend = new HashMap<>();
                            friend.put("status", FriendshipStatus.PENDING);
                            friend.put("seen", NotificationStatus.UNREAD);
                            userToFriends.child(userFrom).updateChildren(friend);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }
}
