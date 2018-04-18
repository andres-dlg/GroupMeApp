package com.andresdlg.groupmeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.ConversationStatus;
import com.andresdlg.groupmeapp.Utils.FriendshipStatus;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.andresdlg.groupmeapp.uiPackage.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by andresdlg on 17/01/18.
 */

public class RVFilesAdapter extends RecyclerView.Adapter<RVFilesAdapter.FilesViewHolder>{

    private List<File> files;
    private Context context;

    public RVFilesAdapter(List<File> files, Context context){
        this.files = files;
        this.context = context;
    }

    @Override
    public FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_subgroup_files_list_item, parent, false);
        return new FilesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FilesViewHolder filesViewHolder, int position) {
        File f = files.get(position);
        filesViewHolder.setDetails(context,f.getFilename(),f.getFileType(),f.getFileSize(),f.getFileUrl(),f.getUploadTime(),f.getUser());
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    static class FilesViewHolder extends RecyclerView.ViewHolder {

        View mView;

        FilesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setDetails(Context context, String filename, String fileType, float fileSize, String fileUrl, long uploadTime, String user){

            TextView fileNameTv = mView.findViewById(R.id.file_name);
            fileNameTv.setText(filename);
            fileNameTv.setSelected(true);

            TextView fileSizeTv = mView.findViewById(R.id.file_size);
            fileSizeTv.setText(String.valueOf(fileSize)); //HACER MEJOR

            TextView fileTypeTv = mView.findViewById(R.id.file_type);
            fileTypeTv.setText(fileType);

            TextView uploadTimeTv = mView.findViewById(R.id.upload_time);
            uploadTimeTv.setText("Horario"); //HACER MEJOR


            /*CircleImageView btn = mView.findViewById(R.id.btn_menu);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();

                    popupMenu.getMenuInflater().inflate(R.menu.fragment_contact_menu, menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.message:
                                    //ENVIAR MENSAJE
                                    sendMessage(iduser, context);
                                    //Toast.makeText(context,"aceptar "+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.add_to_group:
                                    //AGREGAR A GRUPO
                                    //rejectRequest(iduser);
                                    //Toast.makeText(context,"rechazar"+contactName, Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.delete:
                                    //ELIMINAR CONTACTO
                                    deleteContact(iduser, context);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });*/
        }
    }

}
