package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by andresdlg on 13/07/17.
 */

public class HeaderDialogFragment extends DialogFragment implements GroupAddMembersFragment.OnUserSelectionSetListener, GroupSetupFragment.OnGroupImageSetListener{

    private ViewPager viewPager;
    private List<Fragment> fragments;

    //FIREBASE DATABASE FIELDS
    DatabaseReference mUsersDatabase;
    DatabaseReference mGroupsDatabase;
    StorageReference mStorageReference;

    List<String> userIds;
    TextView nameText;
    TextView objetiveText;
    private Uri imageUrl;

    //PROGRESS DIALOG
    ProgressDialog mProgress;
    private StorageReference mGroupsStorage;


    public HeaderDialogFragment(){
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups_dialog, container, false);

        /*final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });*/

        userIds = new ArrayList<>();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference("Groups");
        mStorageReference = FirebaseStorage.getInstance().getReference();

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Nuevo grupo");

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);


        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Setup", GroupSetupFragment.class)
                .add("Add contacts", GroupAddMembersFragment.class)
                .create());

        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        //viewPager.setOffscreenPageLimit(2);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) view.findViewById(R.id.ntb);
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

            final String groupKey = mGroupsDatabase.push().getKey();

            mProgress = new ProgressDialog(getContext());
            mProgress.setCancelable(true);
            mProgress.setMessage("Espere por favor");
            mProgress.setTitle("Guardando grupo");
            mProgress.show();

            if(imageUrl != null){
                mGroupsStorage = mStorageReference.child("Groups").child(groupKey).child(imageUrl.getLastPathSegment());
                mGroupsStorage.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageUrl = taskSnapshot.getDownloadUrl();
                        createGroupData(groupKey,nameText.getText().toString(),objetiveText.getText().toString(),userIds);
                        mProgress.dismiss();
                        dismiss();
                    }
                });
            }else{
                mStorageReference.child("new_user.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUrl = uri;
                        createGroupData(groupKey,nameText.getText().toString(),objetiveText.getText().toString(),userIds);
                        mProgress.dismiss();
                        dismiss();
                    }
                });
            }
        }
    }

    private void createGroupData(String groupKey, String name, String obj, List<String> userIds) {

        userIds.add(StaticFirebaseSettings.currentUserId);

        Map<Object,Object> map = new HashMap<>();
        map.put("name", name);
        map.put("objetive", obj);
        map.put("imageUrl",imageUrl.toString());
        map.put("members", userIds);

        mGroupsDatabase.child(groupKey).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Grupo guardado",Toast.LENGTH_SHORT).show();
            }
        });

        Map<String,Object> map2;
        map2 = new HashMap<>();
        map2.put("status", GroupStatus.ACCEPTED);

        for(final String id : userIds){
            mUsersDatabase.child(id).child("groups").child(groupKey).updateChildren(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(),"Grupo guardado en "+id,Toast.LENGTH_SHORT).show();
                }
            });
        }
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


    private void startDialog(){

    }


    @Override
    public void onUserSelectionSet(List<String> userIds) {
        this.userIds = userIds;
    }

    @Override
    public void onGroupImageSet(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }


}
