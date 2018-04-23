package com.andresdlg.groupmeapp.uiPackage.ReciclerViewClickListener;

import android.view.View;

public interface RecyclerClick_Listener {

    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
