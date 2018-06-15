package com.andresdlg.groupmeapp.Adapters;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.FireApp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


/**
 * Created by andresdlg on 17/01/18.
 */

public class RVGroupFilesAdapter extends RecyclerView.Adapter<RVGroupFilesAdapter.FilesViewHolder>{

    private List<File> files;
    private Context context;
    private String groupName;
    private String groupKey;
    private String myRol;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private int notificationChannel;

    public RVGroupFilesAdapter(List<File> files, Context context, String groupKey){
        this.files = files;
        this.context = context;
        this.groupName = ((FireApp) context.getApplicationContext()).getGroupName();
        this.groupKey = groupKey;
        notificationChannel = 596;
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_subgroup_files_list_item, parent, false);
        return new FilesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder filesViewHolder, int position) {
        File f = files.get(position);
        filesViewHolder.setDetails(context,f.getFileKey(),f.getFilename(),f.getFileType(),f.getFileSize(),f.getFileUrl(),f.getUploadTime(),f.getUser(),position);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setMyRol(String myRol) {
        this.myRol = myRol;
    }

    class FilesViewHolder extends RecyclerView.ViewHolder {

        View mView;
        //int position;

        FilesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            //position = getAdapterPosition();
        }

        void setDetails(final Context context, final String fileKey, final String fileName, final String fileType, float fileSize, final String fileUrl, long uploadTime, String user, final int position){

            final java.io.File fileFromPhone = new java.io.File(Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName+"/"+fileName);

            TextView fileNameTv = mView.findViewById(R.id.file_name);
            fileNameTv.setText(fileName);
            fileNameTv.setSelected(true);

            TextView fileSizeTv = mView.findViewById(R.id.file_size);
            fileSizeTv.setText(readableFileSize((long)fileSize));

            TextView fileTypeTv = mView.findViewById(R.id.file_type);
            fileTypeTv.setText(fileType);

            TextView uploadTimeTv = mView.findViewById(R.id.upload_time);
            uploadTimeTv.setText(milisecondsToDate(uploadTime));

            ImageView fileIconIv = mView.findViewById(R.id.file_icon);
            fileIconIv.setImageResource(getIcon(fileType));
            fileIconIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            ImageView fileStatusIv = mView.findViewById(R.id.file_status);
            fileStatusIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if(!fileFromPhone.exists()){
                Drawable i = context.getResources().getDrawable(R.drawable.arrow_down_bold_circle);
                i.setTint(context.getResources().getColor(R.color.blue_file_download));
                fileStatusIv.setImageDrawable(i);
            }else{
                Drawable i = context.getResources().getDrawable(R.drawable.check_circle);
                i.setTint(context.getResources().getColor(R.color.green_file_download));
                fileStatusIv.setImageDrawable(i);
            }

            ImageButton btn = mView.findViewById(R.id.btn_menu);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();
                    popupMenu.getMenuInflater().inflate(R.menu.subgroup_files_dialog_fragment_admin_menu, menu);

                    popupMenu.getMenu().removeItem(R.id.publish);

                    if(!myRol.equals(Roles.ADMIN.toString())){
                        popupMenu.getMenu().removeItem(R.id.delete);
                    }

                    if(!fileFromPhone.exists()){
                        popupMenu.getMenu().removeItem(R.id.share);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.download:
                                    //DESCARGA
                                    overrideFile(fileUrl,fileName,fileType,fileFromPhone.exists(),position);
                                    break;
                                case R.id.share:
                                    //COMPARTIR
                                    shareFile(fileFromPhone);
                                    //saveFileToDrive(fileFromPhone);
                                    break;
                                case R.id.delete:
                                    //ELIMINAR
                                    deleteFile(fileKey,fileUrl,fileFromPhone,fileFromPhone.exists());
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }


        private int getIcon(String fileType) {
            switch (fileType){
                case "dat":
                    return R.mipmap.dat;
                case "dll":
                    return R.mipmap.dll;
                case "dmg":
                    return R.mipmap.dmg;
                case "fla":
                    return R.mipmap.fla;
                case "iso":
                    return R.mipmap.iso;
                case "raw":
                    return R.mipmap.raw;
                case "sql":
                    return R.mipmap.sql;
                case "3ds":
                    return R.mipmap.x3ds;
                case "zip":
                    return R.mipmap.zip;
                //Web
                case "css":
                    return R.mipmap.css;
                case "html":
                    return R.mipmap.html;
                case "js":
                    return R.mipmap.js;
                case "php":
                    return R.mipmap.php;
                case "svg":
                    return R.mipmap.svg;
                case "xml":
                    return R.mipmap.xml;
                //Softwares
                case "cdr":
                    return R.mipmap.cdr;
                case "cad":
                    return R.mipmap.cad;
                case "ai":
                    return R.mipmap.ai;
                case "eps":
                    return R.mipmap.eps;
                case "indd":
                    return R.mipmap.indd;
                case "ppt":
                    return R.mipmap.ppt;
                case "psd":
                    return R.mipmap.psd;
                //Imagenes
                case "jpg":
                    return R.mipmap.jpg;
                case "png":
                    return R.mipmap.png;
                case "bmp":
                    return R.mipmap.bmp;
                case "gif":
                    return R.mipmap.gif;
                case "tif":
                    return R.mipmap.tif;
                //Documentos
                case "pdf":
                    return R.mipmap.pdf;
                case "doc":
                    return R.mipmap.doc;
                case "docx":
                    return R.mipmap.doc;
                case "xls":
                    return R.mipmap.xls;
                case "xlsx":
                    return R.mipmap.xls;
                case "ps":
                    return R.mipmap.ps;
                case "txt":
                    return R.mipmap.txt;
                //Audio
                case "aac":
                    return R.mipmap.aac;
                case "midi":
                    return R.mipmap.midi;
                case "mpg":
                    return R.mipmap.mpg;
                //Video
                case "avi":
                    return R.mipmap.avi;
                case "flv":
                    return R.mipmap.flv;
                case "mov":
                    return R.mipmap.mov;
                case "wmv":
                    return R.mipmap.wmv;
                default:
                    return R.mipmap.txt;
            }
        }

        private String readableFileSize(long size) {
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private String milisecondsToDate(long miliseconds){
            @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(miliseconds);
            return formatter.format(calendar.getTime());
        }
    }

    private void deleteFile(final String fileKey, final String fileUrl, final java.io.File fileFromPhone, final boolean exists) {

        new AlertDialog.Builder(context,R.style.MyDialogTheme)
                .setTitle("¿Seguro desea eleminar el archivo compartido?")
                .setMessage("Este archivo ya no estará disponible ningún miembro del subgrupo")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(context, "Borrando...", Toast.LENGTH_SHORT).show();
                        //ELIMINO DE FIREBASE
                        StorageReference fileStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
                        fileStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                DatabaseReference fileDataBaseRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("files").child(fileKey);
                                fileDataBaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "El archivo ha sido eliminado del repositorio central", Toast.LENGTH_SHORT).show();
                                        if(exists){
                                            new AlertDialog.Builder(context,R.style.MyDialogTheme)
                                                    .setTitle("¿Desea eliminar la copia existente en su dispositivo?")
                                                    //.setMessage("Ya no estará disponib")
                                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            if(fileFromPhone.delete()){
                                                                Toast.makeText(context, "El archivo ha sido eliminado de su dispositivo", Toast.LENGTH_SHORT).show();
                                                            }else{
                                                                Toast.makeText(context, "Error al eliminar el archivo de su dispositivo", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Error al eliminar el archivo de la base de datos", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error al eliminar el archivo del repositorio", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void shareFile(java.io.File fileWithinMyDir) {

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType("*/*");
            String[] mimeTypes = {"image/*","audio/*","video/*","text/*","application/*"};
            //intentShareFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            intentShareFile.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+fileWithinMyDir));

            /*intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");*/

            ((AppCompatActivity)context).startActivity(Intent.createChooser(intentShareFile, "Compartir archivo"));
        }

    }

    private void overrideFile(final String fileUrl, final String fileName, final String fileType, boolean exists, final int position){

        if(exists){
            new AlertDialog.Builder(context,R.style.MyDialogTheme)
                    .setTitle("Este archivo ya existe en su dispositivo")
                    .setMessage("¿Desea sobreescribirlo?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadFile(fileUrl,fileName,fileType,position);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else{
            downloadFile(fileUrl,fileName,fileType,position);
        }
    }

    private void downloadFile(String fileUri, final String fileName, String fileType, final int position) {

        notificationChannel++;

        String[] fileData = {fileUri,fileName,fileType,groupName};

        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileData[0]);

        final String folderLocation = Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName;

        java.io.File localFile = null;
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        final StorageReference downloadRef;
        downloadRef = mStorageRef.getRoot().child(storageRef.getPath());

        try {
            java.io.File output = new java.io.File(folderLocation);
            if (!output.exists()) {
                output.mkdirs();
            }
            localFile = new java.io.File(output, fileData[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Download and get total bytes
        downloadRef.getFile(localFile)
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        Intent intent = new Intent();
                        final PendingIntent pendingIntent = PendingIntent.getActivity(
                                context, 17, intent, 0);
                        mBuilder =
                                new NotificationCompat.Builder(context,"GroupMeAppChannel")
                                        //.setSmallIcon(android.R.drawable.ic_menu_upload)
                                        .setOngoing(true)
                                        .setOnlyAlertOnce(true)
                                        .setSmallIcon(android.R.drawable.stat_sys_download)  // here is the animated icon
                                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.stat_sys_download))
                                        .setContentTitle("Descargando archivo");

                        mBuilder.setContentIntent(pendingIntent);
                        notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mBuilder.setProgress(100
                                , (int) Math.round(progress), false);
                        mBuilder.setContentText(fileName);

                        /*PROGRESS IS INTEGER VALUE THATS U GOT IT FROM IMPLEMENT METHOD. EXAMPLE IN AWS :

                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                        MAKE IT INTEGER,LIKE THIS :
                        int progress = (int) ((double) bytesCurrent * 100 / bytesTotal);
                        */

                        notificationManager.notify(notificationChannel, mBuilder.build());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.v("Success: ","File: "+storageRef.getPath());
                        Toast.makeText(context, "Archivo guardado en: " + folderLocation, Toast.LENGTH_SHORT).show();

                        mBuilder.setContentTitle("Descarga completada")
                                .setProgress(0,0,false);
                        mBuilder.setContentText(fileName);
                        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                        mBuilder.setOngoing(false);
                        notificationManager.notify(notificationChannel, mBuilder.build());

                        notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Error: ","File: "+storageRef.getPath()+" ||| " +exception.toString());
                    }
                });
    }
}

