package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVFilesAdapter;
import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
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
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupFilesDialogFragment extends DialogFragment {

    private static final int RESULT_LOAD_FILES = 1;

    int notificationChannel;

    private String subGroupName;
    private String subGroupUrlPhoto;
    private String subGroupKey;
    private String groupKey;
    private String groupName;
    private Map<String,String> members;
    private String myRol;

    List<File> files;
    RVFilesAdapter rvFilesAdapter;

    StorageReference mSubgroupFilesStorageRef;
    DatabaseReference mSubgroupFilesDatabaseRef;

    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;

    public SubGroupFilesDialogFragment(String subGroupName, String subGroupUrlPhoto, String subGroupKey, String groupKey, String groupName) {
        this.subGroupName = subGroupName;
        this.subGroupUrlPhoto = subGroupUrlPhoto;
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

        TextView tv = toolbar.findViewById(R.id.action_bar_title_1);
        tv.setText(subGroupName);

        CircleImageView civ = toolbar.findViewById(R.id.conversation_contact_photo);
        Picasso.with(getContext()).load(subGroupUrlPhoto).into(civ);


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

        rvFilesAdapter = new RVFilesAdapter(files,getContext(), subGroupName,groupKey,subGroupKey);
        rv.setAdapter(rvFilesAdapter);

        DatabaseReference subGroupReg = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey);
        subGroupReg.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                members = (Map<String,String>) dataSnapshot.child("members").getValue();
                myRol = setMyRol();
                rvFilesAdapter.setMyRol(myRol);
                mSubgroupFilesDatabaseRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("files");
                mSubgroupFilesDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        files.clear();
                        for (DataSnapshot d : dataSnapshot.getChildren()){
                            File file = d.getValue(File.class);
                            files.add(file);
                        }
                        rvFilesAdapter.notifyDataSetChanged();
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
        }else if(requestCode == 9999 && resultCode == RESULT_OK){
            Toast.makeText(getContext(), "Directorio "+data.getData(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getFile(final Uri fileUri, final int notificationChannel) {


        final String[] fileData = getFileData(fileUri);

        DatabaseReference subGroupFilesRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("files");
        String fileKey = subGroupFilesRef.push().getKey();

        final File file = new File(fileKey, fileData[0],"nourl",fileData[2], Float.valueOf(fileData[1]), 0, StaticFirebaseSettings.currentUserId);

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
                                Toast.makeText(getContext(), "Archivo DB: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                Toast.makeText(getContext(), "Archivo ST: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                files.add(file);
                            }
                        });
                    }
                });
    }

    public String[] getFileData(Uri uri){
        String fileName = null;
        String fileSize = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContext().getContentResolver().query(uri,null,null,null,null);
            try {
                if(cursor != null && cursor.moveToFirst()){
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            }finally {
                cursor.close();
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


    private String getFileType(Uri fileUri) {
        String extension;

        if(fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)){
            final MimeTypeMap mime = MimeTypeMap.getSingleton();

            extension = mime.getExtensionFromMimeType(getContext().getContentResolver().getType(fileUri));
        }else{
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new java.io.File(fileUri.getPath())).toString());
        }
        return extension;
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

    public static void copyFile(java.io.File sourceFile, java.io.File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}