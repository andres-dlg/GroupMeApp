package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.GroupStatus;
import com.andresdlg.groupmeapp.Utils.GroupType;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupAddMembersFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.GroupSetupFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by andresdlg on 13/07/17.
 */

public class HeaderDialogFragment extends DialogFragment implements GroupAddMembersFragment.OnUserSelectionSetListener, GroupSetupFragment.OnGroupImageSetListener{

    private List<Fragment> fragments;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUsersDatabase;
    DatabaseReference mGroupsDatabase;
    StorageReference mStorageReference;

    List<String> userIds;
    TextView nameText;
    TextView objetiveText;
    private Uri imageUrl;

    ProgressBar mProgressBar;

    GroupType type;
    String parentGroupKey;
    private boolean saved;
    private OnSaveGroupListener mOnSaveGroupListener;

    public HeaderDialogFragment(GroupType type){
        setRetainInstance(true);
        this.type = type;
    }

    public HeaderDialogFragment(GroupType type, String groupKey){
        this.parentGroupKey = groupKey;
        setRetainInstance(true);
        this.type = type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups_dialog, container, false);

        userIds = new ArrayList<>();

        mProgressBar = view.findViewById(R.id.progressBar);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference("Groups");
        mStorageReference = FirebaseStorage.getInstance().getReference();

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        TextView tv = ((TextView)toolbar.findViewById(R.id.dialogTitle));
        if(type == GroupType.GROUP){
            tv.setText("Nuevo grupo");
        }else{
            tv.setText("Nuevo subgrupo");
        }

        (toolbar.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        (toolbar.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGroup();
            }
        });


        //toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);

        /*((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }*/
        setHasOptionsMenu(true);


        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Setup", GroupSetupFragment.class)
                .add("Add contacts", GroupAddMembersFragment.class)
                .create());

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        //viewPager.setOffscreenPageLimit(2);

        final NavigationTabBar navigationTabBar =  view.findViewById(R.id.ntb);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.pen),
                        Color.parseColor(colors[2])
                ).title("Editar")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.account_multiple),
                        Color.parseColor(colors[2])
                ).title("Contactos")
                        .badgeTitle("with")
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setInactiveColor(getResources().getColor(R.color.cardview_dark_background));
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setIsTitled(true);
        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        navigationTabBar.setTypeface("@font/simplifica_font");
        navigationTabBar.setTitleSize(25);
        navigationTabBar.setIconSizeFraction((float) 0.5);

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
        inflater.inflate(R.menu.fragment_groups_dialog_menu, menu);
        menu.removeItem(R.id.contacts);
        menu.removeItem(R.id.add_contact);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(type == GroupType.GROUP){
            Fragment fragment = getFragmentManager().getFragments().get(1);
            onAttachToParentFragment(fragment);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.save:
                return true;
            case android.R.id.home:
                dismiss();
                return true;
            case R.id.menu_save:
                saveGroup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGroup() {

        fragments = new ArrayList<>();
        fragments = getChildFragmentManager().getFragments();

        if(validateFields()){
            String groupKey = null;
            String subGroupKey = null;
            if(type == GroupType.GROUP) {
                groupKey = mGroupsDatabase.push().getKey();
            }else {
                subGroupKey = mGroupsDatabase.child(parentGroupKey).child("subgroups").push().getKey();
            }

            if(type == GroupType.GROUP){
                if(imageUrl != null){
                    mProgressBar.setVisibility(View.VISIBLE);
                    StorageReference mGroupsStorage = mStorageReference.child("Groups").child(groupKey).child(imageUrl.getLastPathSegment());
                    final String finalGroupKey = groupKey;
                    mGroupsStorage.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageUrl = taskSnapshot.getDownloadUrl();
                            userIds.add(StaticFirebaseSettings.currentUserId);
                            Map<Object,Object> map = new HashMap<>();
                            for(String id: userIds){
                                if(id.equals(StaticFirebaseSettings.currentUserId)){
                                    map.put(id,Roles.ADMIN);
                                }else{
                                    map.put(id,Roles.MEMBER);
                                }
                            }
                            createGroupData(finalGroupKey,nameText.getText().toString(),objetiveText.getText().toString(),map);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mOnSaveGroupListener.onSavedGroup(true);
                            dismiss();
                        }
                    });
                }else{
                    final String finalGroupKey1 = groupKey;
                    imageUrl = Uri.parse("android.resource://com.andresdlg.groupmeapp/"+R.drawable.login_background_cardview);
                    StorageReference mGroupsStorage = mStorageReference.child("Groups").child(groupKey).child(imageUrl.getLastPathSegment());
                    mGroupsStorage.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageUrl = taskSnapshot.getDownloadUrl();
                            userIds.add(StaticFirebaseSettings.currentUserId);
                            Map<Object,Object> map = new HashMap<>();
                            for(String id: userIds){
                                if(id.equals(StaticFirebaseSettings.currentUserId)){
                                    map.put(id,Roles.ADMIN);
                                }else{
                                    map.put(id,Roles.MEMBER);
                                }
                            }
                            createGroupData(finalGroupKey1,nameText.getText().toString(),objetiveText.getText().toString(),map);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mOnSaveGroupListener.onSavedGroup(true);
                            dismiss();
                        }
                    });
                }
            }

            if(type == GroupType.SUBGROUP){
                if(userIds.size()>0){
                    mProgressBar.setVisibility(View.VISIBLE);
                    if(imageUrl != null){
                        StorageReference mSubGroupsStorage = mStorageReference.child("Groups").child(parentGroupKey).child(subGroupKey).child(imageUrl.getLastPathSegment());
                        final String finalSubGroupKey = subGroupKey;
                        mSubGroupsStorage.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageUrl = taskSnapshot.getDownloadUrl();
                                userIds.add(StaticFirebaseSettings.currentUserId);
                                Map<Object,Object> map = new HashMap<>();
                                for(String id: userIds){
                                    if(id.equals(StaticFirebaseSettings.currentUserId)){
                                        map.put(id,Roles.SUBGROUP_ADMIN);
                                    }else{
                                        map.put(id,Roles.SUBGROUP_MEMBER);
                                    }
                                }
                                createSubGroupData(finalSubGroupKey,nameText.getText().toString(), objetiveText.getText().toString(), map);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                dismiss();
                            }
                        });
                    }else{
                        final String finalSubGroupKey1 = subGroupKey;
                        imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/groupmeapp-5aaf6.appspot.com/o/ic_launcher.png?alt=media&token=9740457d-49b7-4463-b78c-4c3513d768a7");
                        userIds.add(StaticFirebaseSettings.currentUserId);
                        Map<Object,Object> map = new HashMap<>();
                        for(String id: userIds){
                            if(id.equals(StaticFirebaseSettings.currentUserId)){
                                map.put(id,Roles.SUBGROUP_ADMIN);
                            }else{
                                map.put(id,Roles.SUBGROUP_MEMBER);
                            }
                        }
                        createSubGroupData(finalSubGroupKey1,nameText.getText().toString(),objetiveText.getText().toString(),map);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        dismiss();

                        /*StorageReference mSubGroupsStorage = mStorageReference.child("Groups").child(groupKey).child(subGroupKey).child(imageUrl.getLastPathSegment());
                        mSubGroupsStorage.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageUrl = taskSnapshot.getDownloadUrl();
                                userIds.add(StaticFirebaseSettings.currentUserId);
                                Map<Object,Object> map = new HashMap<>();
                                for(String id: userIds){
                                    if(id.equals(StaticFirebaseSettings.currentUserId)){
                                        map.put(id,Roles.SUBGROUP_ADMIN);
                                    }else{
                                        map.put(id,Roles.SUBGROUP_MEMBER);
                                    }
                                }
                                createSubGroupData(finalSubGroupKey1,nameText.getText().toString(),objetiveText.getText().toString(),map);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mOnSaveGroupListener.onSavedGroup(true);
                                dismiss();
                            }
                        });*/

                        /*mStorageReference.child("group_work_grey_192x192.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri;
                                //userIds.add(StaticFirebaseSettings.currentUserId);
                                Map<Object,Object> map = new HashMap<>();
                                for(String id: userIds){
                                    if(id.equals(StaticFirebaseSettings.currentUserId)){
                                        map.put(id,Roles.SUBGROUP_ADMIN);
                                    }else{
                                        map.put(id,Roles.SUBGROUP_MEMBER);
                                    }
                                }
                                createSubGroupData(finalSubGroupKey1,nameText.getText().toString(),map);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                dismiss();
                            }
                        });*/
                    }
                }else{
                    Toast.makeText(getContext(), "Seleccione al menos un integrante", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void createSubGroupData(String subGroupKey, String name, String objetive, Map<Object, Object> userIdsMap) {

        Map<Object,Object> map = new HashMap<>();
        map.put("name", name);
        map.put("imageUrl",imageUrl.toString());
        map.put("objetive",objetive);
        map.put("members", userIdsMap);
        map.put("subGroupKey", subGroupKey);

        mGroupsDatabase.child(parentGroupKey).child("subgroups").child(subGroupKey).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Subgrupo guardado",Toast.LENGTH_SHORT).show();
            }
        });

        //Notification
        Map<String,Object> map2;
        if(!userIds.isEmpty()){
            map2 = new HashMap<>();
            map2.put("status", GroupStatus.ACCEPTED);
            for(final String id : userIds){
                mUsersDatabase.child(id).child("groups").child(parentGroupKey).child("subgroups").child(subGroupKey).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),"Subgrupo guardado en "+id,Toast.LENGTH_SHORT).show();
                    }
                });

                DatabaseReference userToNotifications = mUsersDatabase.child(id).child("notifications");
                String notificationKey = userToNotifications.push().getKey();
                Map<String,Object> notification = new HashMap<>();
                notification.put("notificationKey",notificationKey);
                notification.put("title","Invitación a grupo");
                notification.put("message","Te han añadido al subgrupo " + name);
                notification.put("from", parentGroupKey);
                notification.put("state", NotificationStatus.UNREAD);
                notification.put("date", Calendar.getInstance().getTimeInMillis());
                notification.put("type", NotificationTypes.GROUP_INVITATION);

                userToNotifications.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Noti de agregación a subgrupo enviada", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void createGroupData(String groupKey, String name, String obj, Map<Object,Object> userIdsMaps) {

        Map<Object,Object> map = new HashMap<>();
        map.put("name", name);
        map.put("objetive", obj);
        map.put("imageUrl",imageUrl.toString());
        map.put("members", userIdsMaps);
        map.put("groupKey", groupKey);

        mGroupsDatabase.child(groupKey).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Grupo guardado",Toast.LENGTH_SHORT).show();
            }
        });


        Map<String,Object> map2;
        if(!userIds.isEmpty()){
            map2 = new HashMap<>();
            map2.put("status", GroupStatus.PENDING);

            for(final String id : userIds){
                mUsersDatabase.child(id).child("groups").child(groupKey).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),"Grupo guardado en "+id,Toast.LENGTH_SHORT).show();
                    }
                });

                DatabaseReference userToNotifications = mUsersDatabase.child(id).child("notifications");
                String notificationKey = userToNotifications.push().getKey();
                Map<String,Object> notification = new HashMap<>();
                notification.put("notificationKey",notificationKey);
                notification.put("title","Invitación a grupo");
                notification.put("message","Has recibido una invitación para unirte al grupo " + name);
                notification.put("from", groupKey);
                notification.put("state", NotificationStatus.UNREAD);
                notification.put("date", Calendar.getInstance().getTimeInMillis());
                notification.put("type", NotificationTypes.GROUP_INVITATION);

                userToNotifications.child(notificationKey).setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Invitación de grupo enviada", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        map2 = new HashMap<>();
        map2.put("status", GroupStatus.ACCEPTED);
        mUsersDatabase.child(StaticFirebaseSettings.currentUserId).child("groups").child(groupKey).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Grupo guardado en "+StaticFirebaseSettings.currentUserId,Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean validateFields() {
        View focusView;
        objetiveText = fragments.get(0).getView().findViewById(R.id.etobj);
        nameText = fragments.get(0).getView().findViewById(R.id.etId);
        if(TextUtils.isEmpty(nameText.getText())){
            Toast.makeText(getContext(),"Ingrese un nombre para el grupo",Toast.LENGTH_SHORT).show();
            focusView = nameText;
            focusView.requestFocus();
            return false;
        }
        return true;
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
    public void onUserSelectionSet(List<String> userIds) {
        this.userIds = userIds;
    }

    @Override
    public void onGroupImageSet(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }


    public interface OnSaveGroupListener{
        public void onSavedGroup(boolean saved);
    }

    public void onAttachToParentFragment(Fragment fragment){
        try {
            mOnSaveGroupListener = (OnSaveGroupListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(fragment.toString() + " must implement OnUserSelectionSetListener");
        }
    }

}
