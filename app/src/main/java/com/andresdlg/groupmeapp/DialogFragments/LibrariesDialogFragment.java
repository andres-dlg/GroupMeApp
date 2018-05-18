package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.andresdlg.groupmeapp.Adapters.RVLibrariesAdapter;
import com.andresdlg.groupmeapp.Entities.Library;
import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andresdlg on 13/07/17.
 */

public class LibrariesDialogFragment extends DialogFragment {

    List<Library> libraries;

    RVLibrariesAdapter rvLibrariesAdapter;

    public LibrariesDialogFragment(){
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.libraries_dialog, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Librerias utilizadas");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);


        libraries = new ArrayList<>();

        fillLibraries();

        RecyclerView rvLibrary = view.findViewById(R.id.rvLibrary);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvLibrary.setLayoutManager(llm);

        rvLibrariesAdapter = new RVLibrariesAdapter(libraries);
        rvLibrary.setAdapter(rvLibrariesAdapter);

        rvLibrariesAdapter.notifyDataSetChanged();

        return view;
    }

    private void fillLibraries() {
        Library lib = new Library("CircleImageView", "A fast circular ImageView perfect for profile images", "https://github.com/hdodenhof/CircleImageView");
        libraries.add(lib);

        Library lib2 = new Library("Android-Image-Cropper", "Image Cropping Library for Android, optimized for Camera / Gallery", "https://github.com/ArthurHub/Android-Image-Cropper");
        libraries.add(lib2);

        Library lib3 = new Library("MaterialSearchView", "Cute library to implement SearchView in a Material Design Approach", "https://github.com/MiguelCatalan/MaterialSearchView");
        libraries.add(lib3);

        Library lib4 = new Library("Material Dialogs", "A beautiful, fluid, and customizable dialogs API", "https://github.com/afollestad/material-dialogs");
        libraries.add(lib4);

        Library lib5 = new Library("Android-Week-View", "Library to display calendars (week view or day view) within the app", "https://github.com/alamkanak/Android-Week-View");
        libraries.add(lib5);

        Library lib6 = new Library("SmartTabLayout", "A custom ViewPager title strip which gives continuous feedback to the user when scrolling", "https://github.com/ogaclejapan/SmartTabLayout");
        libraries.add(lib6);

        Library lib7 = new Library("NavigationTabBar", "Navigation tab bar with colorful interactions.", "https://github.com/Devlight/NavigationTabBar");
        libraries.add(lib7);

        Library lib8 = new Library("Glide", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide");
        libraries.add(lib8);

        Library lib9 = new Library("Glide Transformations", "An Android transformation library providing a variety of image transformations for Glide", "https://github.com/wasabeef/glide-transformations");
        libraries.add(lib9);

        Library lib10 = new Library("PrettyTime", "Social Style Date and Time Formatting for Java", "https://github.com/ocpsoft/prettytime");
        libraries.add(lib10);

        Library lib11 = new Library("Smart App Rate", "An Android library that encourages users to rate the app on the Google Play", "https://github.com/codemybrainsout/smart-app-rate");
        libraries.add(lib11);

        Library lib12 = new Library("PhotoView", "Implementation of ImageView for Android that supports zooming, by various touch gestures", "https://github.com/chrisbanes/PhotoView");
        libraries.add(lib12);

        Library lib13 = new Library("Spotlight", "Android Library that lights items for tutorials or walk-throughs etc...", "https://github.com/TakuSemba/Spotlight");
        libraries.add(lib13);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.contacts);
        menu.removeItem(R.id.add_contact);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    }
}
