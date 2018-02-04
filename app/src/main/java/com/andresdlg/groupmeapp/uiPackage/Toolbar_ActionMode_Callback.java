package com.andresdlg.groupmeapp.uiPackage;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVSearchContactAdapter;
import com.andresdlg.groupmeapp.Entities.Users;
import com.andresdlg.groupmeapp.R;

import java.util.ArrayList;

/**
 * Created by SONU on 22/03/16.
 */
public class Toolbar_ActionMode_Callback implements ActionMode.Callback {

    private Context context;
    private RVSearchContactAdapter recyclerView_adapter;
    //private ListView_Adapter listView_adapter;
    private ArrayList<Users> users;
    private boolean isListViewFragment;
    private SearchView searchView;


    public Toolbar_ActionMode_Callback(Context context, RVSearchContactAdapter recyclerView_adapter, ArrayList<Users> users, boolean isListViewFragment) {
        this.context = context;
        this.recyclerView_adapter = recyclerView_adapter;
        //this.listView_adapter = listView_adapter;
        this.users = users;
        this.isListViewFragment = isListViewFragment;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.fragment_groups_add_contact_activity_menu, menu);

        //mode.getMenuInflater().inflate(R.menu.menu_main, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        ComponentName cn = new ComponentName(context, SearchContactsActivity.class);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(cn));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                recyclerView_adapter.getFilter().filter(query);
                recyclerView_adapter.notifyDataSetChanged();
                //recyclerView_adapter.checkSelectedItems();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                recyclerView_adapter.getFilter().filter(query);
                recyclerView_adapter.notifyDataSetChanged();
                //recyclerView_adapter.checkSelectedItems();
                return false;
            }
        });

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels
        /*if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_copy), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_forward), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }*/

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

        }
 /*       switch (item.getItemId()) {
            case R.id.action_delete:

                //Check if current action mode is from ListView Fragment or RecyclerView Fragment
                if (isListViewFragment) {
                    Fragment listFragment = new MainActivity().getFragment(0);//Get list view Fragment
                    if (listFragment != null)
                        //If list fragment is not null
                        ((ListView_Fragment) listFragment).deleteRows();//delete selected rows
                } else {
                    //If current fragment is recycler view fragment
                    Fragment recyclerFragment = new MainActivity().getFragment(1);//Get recycler view fragment
                    if (recyclerFragment != null)
                        //If recycler fragment not null
                        ((RecyclerView_Fragment) recyclerFragment).deleteRows();//delete selected rows
                }
                break;
            case R.id.action_copy:

                //Get selected ids on basis of current fragment action mode
                SparseBooleanArray selected;
                if (isListViewFragment)
                    selected = listView_adapter
                            .getSelectedIds();
                else
                    selected = recyclerView_adapter
                            .getSelectedIds();

                int selectedMessageSize = selected.size();

                //Loop to all selected items
                for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        //get selected data in Model
                        Users model = users.get(selected.keyAt(i));
                        String title = model.getName();
                        String subTitle = model.getAlias();
                        //Print the data to show if its working properly or not
                        Log.e("Selected Items", "Title - " + title + "n" + "Sub Title - " + subTitle);

                    }
                }
                Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;
            case R.id.action_forward:
                Toast.makeText(context, "You selected Forward menu.", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;


        }*/
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        recyclerView_adapter.removeSelection();  // remove selection
        ((SearchContactsActivity) context).setNullToActionMode();//Set action mode null
    }
}