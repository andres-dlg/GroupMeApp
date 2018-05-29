package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.uiPackage.fragments.FriendListFragment;
import com.andresdlg.groupmeapp.uiPackage.fragments.FriendRequestsFragment;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by andresdlg on 13/07/17.
 */

public class ContactsDialogFragment extends DialogFragment implements FriendRequestsFragment.OnNewContactRequestSetListener {
    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    FragmentPagerItemAdapter adapter;
    ViewPager viewPager;

    ArrayList<NavigationTabBar.Model> models;

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

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        NavigationTabBar navigationTabBar = view.findViewById(R.id.ntb);
        models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        ContextCompat.getDrawable(getContext(),R.drawable.account_multiple),
                        Color.parseColor(colors[2])
                ).title("Contactos")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(ContextCompat.getDrawable(getContext(),R.drawable.telegram),
                        Color.parseColor(colors[2])
                ).title("Solicitudes")
                        .badgeTitle("icon")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setInactiveColor(getResources().getColor(R.color.cardview_dark_background));
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setIsTitled(true);
        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ACTIVE);
        //navigationTabBar.setTypeface(customFont);
        navigationTabBar.setTitleSize(10 * getResources().getDisplayMetrics().density);
        navigationTabBar.setIconSizeFraction((float) 0.5);

        navigationTabBar.setBadgePosition(NavigationTabBar.BadgePosition.RIGHT);
        navigationTabBar.setIsBadged(true);
        navigationTabBar.setBadgeBgColor(Color.RED);
        navigationTabBar.setBadgeTitleColor(Color.RED);
        navigationTabBar.setBadgeSize(20);

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

    @Override
    public void onNewContactRequestSet(int requestQuantity) {
        NavigationTabBar.Model model = models.get(1);
        if(requestQuantity > 0){
            model.showBadge();
            //model.setBadgeTitle(String.valueOf(notificationQuantity));
            model.setBadgeTitle(String.valueOf(1));
        }else{
            model.hideBadge();
        }
    }
}
