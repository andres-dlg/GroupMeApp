package com.andresdlg.groupmeapp.uiPackage;

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
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Adapters.RVGroupFilesAdapter;
import com.andresdlg.groupmeapp.Adapters.RVGroupFilesSubgroupsAdapter;
import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationStatus;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.Utils.Roles;
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

public class GroupFilesActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_FILES = 1;

    int notificationChannel;
    NotificationManager notificationManager;

    NotificationCompat.Builder mBuilder;

    StorageReference mGroupFilesStorageRef;
    DatabaseReference mGroupFilesDatabaseRef;

    List<File> groupFiles;

    RVGroupFilesSubgroupsAdapter adapter;
    RecyclerView rvSubGroupFiles;
    RecyclerView rvGroupFiles;
    ImageButton addGroupFileBtn;
    TextView tvNoSubGroupsFiles;
    TextView tvNoGroupsFiles;

    String groupKey;
    String groupName;

    RVGroupFilesAdapter rvGroupFilesAdapter;
    private Map<String, String> groupMembers;
    private String myRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_files);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Archivos compartidos");

        notificationChannel = 0;

        groupFiles = new ArrayList<>();

        groupKey = getIntent().getStringExtra("groupKey");
        groupName = getIntent().getStringExtra("groupName");

        mGroupFilesStorageRef = FirebaseStorage.getInstance().getReference("Groups").child(groupKey).child("files");
        mGroupFilesDatabaseRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("files");

        //-------- SECCION DE ARCHIVOS DE GRUPO
        tvNoGroupsFiles = findViewById(R.id.tvNoGroupsFiles);

        addGroupFileBtn = findViewById(R.id.addGroupFileBtn);

        FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupMembers = (Map<String,String>) dataSnapshot.getValue();
                myRol = setMyRol();
                rvGroupFilesAdapter.setMyRol(myRol);
                if(myRol.equals(Roles.ADMIN.toString())){
                    addGroupFileBtn.setVisibility(View.VISIBLE);
                    setAddFileButtonListener();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //RECYCLER VIEW PARA ARCHIVOS DE GRUPO
        rvGroupFiles = findViewById(R.id.rvGroupFiles);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvGroupFiles.setLayoutManager(layoutManager);
        rvGroupFiles.setHasFixedSize(false);
        RecyclerView.ItemAnimator animator1 = rvGroupFiles.getItemAnimator();
        if (animator1 instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator1).setSupportsChangeAnimations(true);
        }
        rvGroupFilesAdapter = new RVGroupFilesAdapter(groupFiles,this,groupKey);
        rvGroupFiles.setAdapter(rvGroupFilesAdapter);
        getGroupFiles();

        //-------- SECCION DE ARCHIVOS DE SUBGRUPO
        tvNoSubGroupsFiles = findViewById(R.id.tvNoSubGroupsFiles);

        //RECYCLER VIEW PARA ARCHIVOS DE SUBGRUPOS
        rvSubGroupFiles = findViewById(R.id.rvSubGroupFiles);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        rvSubGroupFiles.setLayoutManager(layoutManager1);
        rvSubGroupFiles.setHasFixedSize(false);
        RecyclerView.ItemAnimator animator = rvSubGroupFiles.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(true);
        }
        getSubgroupsAndFiles();
    }

    private void getGroupFiles() {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGroupFilesDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupFiles.clear();
                        for (DataSnapshot d : dataSnapshot.getChildren()){
                            File file = d.getValue(File.class);
                            if(file.isPublished()){
                                groupFiles.add(file);
                            }
                        }
                        if(groupFiles.size() > 0){
                            tvNoGroupsFiles.setVisibility(View.GONE);
                        }else{
                            tvNoGroupsFiles.setVisibility(View.VISIBLE);
                        }
                        rvGroupFilesAdapter.notifyDataSetChanged();
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
    }

    private String setMyRol() {
        myRol = "";
        for(Map.Entry<String, String> entry: groupMembers.entrySet()) {
            if(StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                myRol = entry.getValue();
                break;
            }
        }
        return myRol;
    }

    private void setAddFileButtonListener() {
        addGroupFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("*/*");
                String[] mimeTypes = {"image/*","audio/*","video/*","text/*","application/*"};
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Seleccione los archivos"),RESULT_LOAD_FILES);
            }
        });
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

            DatabaseReference groupFilesRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("groupFiles");
            String fileKey = groupFilesRef.push().getKey();

            final File file = new File(fileKey, fileData[0],"nourl",fileData[2], Float.valueOf(fileData[1]), 0, StaticFirebaseSettings.currentUserId,false);

            StorageReference fileStgRef = mGroupFilesStorageRef.child(fileKey);

            fileStgRef.putFile(fileUri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            Intent intent = new Intent();
                            final PendingIntent pendingIntent = PendingIntent.getActivity(
                                    GroupFilesActivity.this, 17, intent, 0);
                            mBuilder =
                                    new NotificationCompat.Builder(GroupFilesActivity.this,"GroupMeAppChannel")
                                            //.setSmallIcon(android.R.drawable.ic_menu_upload)
                                            .setOngoing(true)
                                            .setOnlyAlertOnce(true)
                                            .setSmallIcon(android.R.drawable.stat_sys_upload)  // here is the animated icon
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.stat_sys_upload))
                                            .setContentTitle("Subiendo archivo");

                            mBuilder.setContentIntent(pendingIntent);
                            notificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
                            map.put("published",true);
                            map.put("user",file.getUser());


                            //GUARDO UNA COPIA DEL ARCHIVO EN LA CARPETA DE MI SUBGRUPO

                            String folderLocation = Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName;
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
                                in = getContentResolver().openInputStream(fileUri);
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


                            final DatabaseReference fileDbRef = mGroupFilesDatabaseRef.child(file.getFileKey());
                            fileDbRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(getContext(), "Archivo DB: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                    // Toast.makeText(getContext(), "Archivo ST: " + fileData[0] + " agregado.", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(GroupFilesActivity.this, fileData[0] + " agregado al repositorio", Toast.LENGTH_SHORT).show();
                                    for(int i = 0; i < groupFiles.size(); i++){
                                        if(file.getFileKey().equals(groupFiles.get(i).getFileKey())){
                                            groupFiles.remove(i);
                                            groupFiles.add(i,file);
                                            break;
                                        }
                                    }
                                }
                            });

                            //NOTIFICO A LOS MIEMBROS DEL SUBGRUPO QUE SE HA SUBIDO UN NUEVO ARCHIVO
                            for(Map.Entry<String, String> entry: groupMembers.entrySet()) {
                                if(!StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                                    DatabaseReference userToNotifications = FirebaseDatabase.getInstance().getReference("Users").child(entry.getKey()).child("notifications");
                                    String notificationKey = userToNotifications.push().getKey();
                                    Map<String,Object> notification = new HashMap<>();
                                    notification.put("notificationKey",notificationKey);
                                    notification.put("title","Nuevo archivo en " + groupName);
                                    notification.put("message","Se ha subido " + file.getFilename() + " en " + groupName);
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
            Toast.makeText(this, "El archivo no puede superar los 20 MB", Toast.LENGTH_SHORT).show();
        }
    }

    public String[] getFileData(Uri uri){
        String fileName = null;
        String fileSize = null;
        if(uri.getScheme().equals("content")){
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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

    private void getSubgroupsAndFiles() {
        FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<SubGroup> subGroups = new ArrayList<>();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    SubGroup sgf = new SubGroup(data.child("name").getValue().toString(),null,data.child("imageUrl").getValue().toString(),(Map<String,String>)data.child("members").getValue(),data.child("subGroupKey").getValue().toString());
                    List<File> files = new ArrayList<>();
                    for(DataSnapshot d : data.child("files").getChildren()){
                        File file = d.getValue(File.class);
                        if(file.isPublished()){
                            files.add(file);
                        }
                    }
                    sgf.setFiles(files);
                    if(sgf.getFiles().size() > 0){
                        SubGroup sg = new SubGroup(sgf.getName(),sgf.getFiles(),sgf.getImageUrl(),sgf.getMembers(),sgf.getSubGroupKey());
                        subGroups.add(sg);
                    }
                }

                if(subGroups.size() > 0){
                    adapter = new RVGroupFilesSubgroupsAdapter(subGroups,GroupFilesActivity.this,groupName,groupKey);
                    rvSubGroupFiles.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else{
                    tvNoSubGroupsFiles.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(adapter!=null){
            adapter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(adapter!=null){
            adapter.onRestoreInstanceState(savedInstanceState);
        }
    }

}
