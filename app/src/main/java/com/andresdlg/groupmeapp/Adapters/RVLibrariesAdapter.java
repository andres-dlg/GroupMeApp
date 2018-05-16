package com.andresdlg.groupmeapp.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.Library;
import com.andresdlg.groupmeapp.R;

import java.util.List;


/**
 * Created by andresdlg on 17/01/18.
 */

public class RVLibrariesAdapter extends RecyclerView.Adapter<RVLibrariesAdapter.LibraryViewHolder>{

    private List<Library> libraries;

    public RVLibrariesAdapter(List<Library> libraries){
        this.libraries = libraries;
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_dialog_list_item, parent, false);
        return new LibraryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final LibraryViewHolder libraryViewHolder, final int position) {
        libraryViewHolder.setDetails(libraries.get(position).getName(),libraries.get(position).getDescription(), libraries.get(position).getLink());
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class LibraryViewHolder extends RecyclerView.ViewHolder {

        View mView;

        LibraryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(String name, String description, final String link) {

            TextView tvLibraryName = mView.findViewById(R.id.tvLibraryName);
            tvLibraryName.setText(name);

            TextView tvLibraryDesc = mView.findViewById(R.id.tvLibraryDesc);
            tvLibraryDesc.setText(description);
            tvLibraryDesc.setSelected(true);

            RelativeLayout rl = mView.findViewById(R.id.rl);
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link));
                    mView.getContext().startActivity(browserIntent);
                }
            });


        }
    }
}
