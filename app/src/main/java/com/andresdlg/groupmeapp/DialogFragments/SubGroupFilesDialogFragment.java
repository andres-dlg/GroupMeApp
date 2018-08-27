package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVSubGroupFilesAdapter;
import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupFilesDialogFragment extends DialogFragment {

    private static final int RESULT_LOAD_FILES = 1;

    int notificationChannel;

    private String subGroupName;
    private String subGroupKey;
    private String groupKey;
    private String groupName;
    private Map<String,String> members;
    private String myRol;

    List<File> files;
    RVSubGroupFilesAdapter rvSubGroupFilesAdapter;

    StorageReference mSubgroupFilesStorageRef;
    DatabaseReference mSubgroupFilesDatabaseRef;

    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;

    public SubGroupFilesDialogFragment(String subGroupName, String subGroupKey, String groupKey, String groupName) {
        this.subGroupName = subGroupName;
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
        this.groupName = groupName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        notificationChannel = 0;
        mSubgroupFilesStorageRef = FirebaseStorage.getInstance().getReference("Groups").child(groupKey).child(subGroupKey).child("files");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subgroup_files, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar_chats);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        toolbar.setTitle("Archivos compartidos");

        Button addFileBtn = v.findViewById(R.id.add_file);
        addFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("*/*");
                String[] mimeTypes = {"image/*","audio/*","video/*","text/*","application/*"};
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Seleccione los archivos"),RESULT_LOAD_FILES);
            }
        });

        files = new ArrayList<>();
        members = new HashMap<>();

        RecyclerView rv = v.findViewById(R.id.rvFiles);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        rvSubGroupFilesAdapter = new RVSubGroupFilesAdapter(files,getContext(), subGroupName,groupKey,subGroupKey);
        rv.setAdapter(rvSubGroupFilesAdapter);

        DatabaseReference subGroupReg = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);
        subGroupReg.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members = (Map<String,String>) dataSnapshot.child("members").getValue();
                myRol = setMyRol();
                rvSubGroupFilesAdapter.setMyRol(myRol);
                mSubgroupFilesDatabaseRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("files");
                mSubgroupFilesDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        files.clear();
                        for (DataSnapshot d : dataSnapshot.getChildren()){
                            File file = d.getValue(File.class);
                            updateFiles(file);
                        }
                        rvSubGroupFilesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_FILES && resultCode == RESULT_OK){
            if(data.getClipData() != null){
                int totalItemsSelected = data.getClipData().getItemCount();
                for(int i = 0; i < totalItemsSelected; i++){
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    getFile(fileUri,notificationChannel);
                    notificationChannel++;
                }
                //Toast.makeText(getContext(), "Ha seleccionado varios archivos!", Toast.LENGTH_SHORT).show();
            }else if(data.getData() != null){
                Uri fileUri = data.getData();
                getFile(fileUri,notificationChannel++);
                notificationChannel++;
            }
        }//else if(requestCode == 9999 && resultCode == RESULT_OK){
            //Toast.makeText(getContext(), "Directorio "+data.getData(), Toast.LENGTH_SHORT).show();
        //}
    }

    private void getFile(final Uri fileUri, final int notificationChannel) {

        final String[] fileData = getFileData(fileUri);

        if(Float.valueOf(fileData[1]) < 20971520){

            DatabaseReference subGroupFilesRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("files");
            String fileKey = subGroupFilesRef.push().getKey();

            final File file = new File(fileKey, fileData[0],"nourl",fileData[2], Float.valueOf(fileData[1]), 0, StaticFirebaseSettings.currentUserId,false);

            StorageReference fileStgRef = mSubgroupFilesStorageRef.child(fileKey);

            fileStgRef.putFile(fileUri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            Intent intent = new Intent();
                            final PendingIntent pendingIntent = PendingIntent.getActivity(
                                    getContext(), 17, intent, 0);
                            mBuilder =
                                    new NotificationCompat.Builder(getContext(),"GroupMeAppChannel")
                                            //.setSmallIcon(android.R.drawable.ic_menu_upload)
                                            .setOngoing(true)
                                            .setOnlyAlertOnce(true)
                                            .setSmallIcon(android.R.drawable.stat_sys_upload)  // here is the animated icon
                                            .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), android.R.drawable.stat_sys_upload))
                                            .setContentTitle("Subiendo archivo");

                            mBuilder.setContentIntent(pendingIntent);
                            notificationManager =
                                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder.setProgress(100
                                    , (int) Math.round(progress), false);
                            mBuilder.setContentText(file.getFilename());

                        /*PROGRESS IS INTEGER VALUE THATS U GOT IT FROM IMPLEMENT METHOD. EXAMPLE IN AWS :

                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                        MAKE IT INTEGER,LIKE THIS :
                        int progress = (int) ((double) bytesCurrent * 100 / bytesTotal);
                        */

                            notificationManager.notify(notificationChannel, mBuilder.build());
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mBuilder.setContentTitle("El archivo se ha subido")
                                    .setProgress(0,0,false);
                            mBuilder.setContentText(file.getFilename());
                            mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                            mBuilder.setOngoing(false);
                            notificationManager.notify(notificationChannel, mBuilder.build());

                            file.setFileUrl(taskSnapshot.getDownloadUrl().toString());
                            file.setUploadTime(Calendar.getInstance().getTimeInMillis());

                            Map<String,Object> map = new HashMap<>();
                            map.put("fileKey",file.getFileKey());
                            map.put("fileName",file.getFilename());
                            map.put("fileUrl",file.getFileUrl());
                            map.put("fileType",file.getFileType());
                            map.put("fileSize",file.getFileSize());
                            map.put("uploadTime",file.getUploadTime());
                            map.put("published",false);
                            map.put("user",file.getUser());


                            //GUARDO UNA COPIA DEL ARCHIVO EN LA CARPETA DE MI SUBGRUPO

                            String folderLocation = Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName+"/Sub Grupos/"+subGroupName;
                            java.io.File localFile = null;
                            try {
                                java.io.File output = new java.io.File(folderLocation);
                                if (!output.exists()) {
                                    output.mkdirs();
                                }
                                localFile = new java.io.File(output, fileData[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            InputStream in = null;
                            try {
                                in = getContext().getContentResolver().openInputStream(fileUri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            OutputStream out = null;
                            try {
                                out = new FileOutputStream(localFile);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            byte[] buf = new byte[1024];
                            int len;
                            try {
                                while((len=in.read(buf))>0){
                                    out.write(buf,0,len);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            final DatabaseReference fileDbRef = mSubgroupFilesDatabaseRef.child(file.getFileKey());
                            fileDbRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(getContext(), "Archivo DB: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                    // Toast.makeText(getContext(), "Archivo ST: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), fileData[0] + " agregado al repositorio", Toast.LENGTH_SHORT).show();
                                    updateFiles(file);
                                }
                            });


                            //NOTIFICO A LOS MIEMBROS DEL SUBGRUPO QUE SE HA SUBIDO UN NUEVO ARCHIVO
                            for(Map.Entry<String, String> entry: members.entrySet()) {
                                if(!StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                                    DatabaseReference userToNotifications = FirebaseDatabase.getInstance().getReference("Users").child(entry.getKey()).child("notifications");
                                    String notificationKey = userToNotifications.push().getKey();
                                    Map<String,Object> notification = new HashMap<>();
                                    notification.put("notificationKey",notificationKey);
                                    notification.put("title","Nuevo archivo en " + subGroupName);
                                    notification.put("message","Se ha subido " + file.getFilename() + " en " + subGroupName);
                                    notification.put("from", groupKey);
                                    notification.put("state", NotificationStatus.UNREAD);
                                    notification.put("date", Calendar.getInstance().getTimeInMillis());
                                    notification.put("type", NotificationTypes.NEW_FILE);
                                    userToNotifications.child(notificationKey).setValue(notification);
                                }
                            }
                        }
                    });
        }else {
            Toast.makeText(getContext(), "El archivo no puede superar los 20 MB", Toast.LENGTH_SHORT).show();
        }
    }

    public String[] getFileData(Uri uri){
        String fileName = null;
        String fileSize = null;
        if(uri.getScheme().equals("content")){
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            }
        }
        if(fileName == null){
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if(cut != -1){
                fileName = fileName.substring(cut + 1);
            }
        }

        //String fileType = getFileType(uri);
        String fileType = getFileExtension(fileName);

        return new String[]{fileName,fileSize,fileType};
    }


    private String getFileExtension(String fileName){
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if(i>=0){
            ext = fileName.substring(i+1);
        }
        return ext;
    }

    private String setMyRol() {
        myRol = "";
        for(Map.Entry<String, String> entry: members.entrySet()) {
            if(StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                myRol = entry.getValue();
                break;
            }
        }
        return myRol;
    }

    private void updateFiles(File file) {
        boolean exists = false;
        for(int i=0; i < files.size(); i++){
            if(files.get(i).getFileKey().equals(file.getFileKey())){
                exists = true;
                files.remove(i);
                files.add(i,file);
                rvSubGroupFilesAdapter.notifyItemChanged(i);
            }
        }
        if(!exists){
            files.add(file);
            rvSubGroupFilesAdapter.notifyDataSetChanged();
        }
    }

}