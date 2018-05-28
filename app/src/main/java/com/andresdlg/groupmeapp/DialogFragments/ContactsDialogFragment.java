package com.andresdlg.groupmeapp.DialogFragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.andresdlg.groupmeapp.uiPackage.fragments.FriendListFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.FriendRequestsFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by andresdlg on 13/07/17.
 */

public class ContactsDialogFragment extends DialogFragment {
    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    FragmentPagerItemAdapter adapter;
    ViewPager viewPager;

    public ContactsDialogFragment(){
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_dialog, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Contactos");

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        //Armo mi tab Layout con getChildFragmentManager() -> se usa este para permitir nested fragments
        /*
        * FriendsDialogFragments
        * |_FriendRequestsFragment
        * |_FriendListFragment
        *
        * */
        adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Contactos", FriendListFragment.class)
                .add("Solicitudes", FriendRequestsFragment.class)
                .create());

        viewPager = view.findViewById(R.id.friendviewpager);
        viewPager.setAdapter(adapter);

        final LayoutInflater inflater2 = LayoutInflater.from(getContext());
        final Resources res = getResources();

        final SmartTabLayout viewPagerTab =  view.findViewById(R.id.friendviewpagertab);
        viewPagerTab.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                View itemView = inflater2.inflate(R.layout.tab_text, container, false);
                TextView text = itemView.findViewById(R.id.custom_tab_text);

                //Obtengo las metricas de la pantalla
                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                //Divido por la cantidad de fragmentos y determino el ancho del imageview que va en
                // cada tab
                text.getLayoutParams().width = metrics.widthPixels / 2;

                switch (position) {
                    case 0:
                        text.setText("CONTACTOS");
                        break;
                    case 1:
                        text.setText("SOLICITUDES");
                        break;
                    default:
                        throw new IllegalStateException("Invalid position: " + position);
                }
                return itemView;
            }
        });
        viewPagerTab.setViewPager(viewPager);
        return view;
    }


    /** The system calls this only when creating the layout in a dialog. */
    //@Override
    //public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
    //    Dialog dialog = super.onCreateDialog(savedInstanceState);
    //    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);*/
    //    return dialog;
    //}

    @Override
    public void onResume() {
        super.onResume();
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.fragment_groups_dialog_menu, menu);
        menu.removeItem(R.id.contacts);
        menu.removeItem(R.id.add_contact);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id==android.R.id.home){
            onDestroyView();
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Dialog dialog = getDialog();
        if(dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }
        adapter = null;
        viewPager = null;
    }
}
