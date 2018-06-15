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
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.andresdlg.groupmeapp.Entities.File;
import com.andresdlg.groupmeapp.Entities.SubGroup;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.Roles;
import com.andresdlg.groupmeapp.firebasePackage.StaticFirebaseSettings;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class RVGroupFilesSubgroupsAdapter extends ExpandableRecyclerViewAdapter<RVGroupFilesSubgroupsAdapter.SubGroupViewHolder, RVGroupFilesSubgroupsAdapter.SubGroupFileViewHolder> {

    Context context;
    private String groupName;
    private String groupKey;
    private int notificationChannel;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager notificationManager;

    private String myRol;

    public RVGroupFilesSubgroupsAdapter(List<? extends ExpandableGroup> groups, Context context, String groupName, String groupKey){
        super(groups);
        this.context = context;
        this.groupName = groupName;
        this.groupKey = groupKey;
        notificationChannel = 5963;
    }

    @Override
    public SubGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_group_files_list_item_sub_group, parent, false);
        return new SubGroupViewHolder(view);
    }

    @Override
    public SubGroupFileViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_subgroup_files_list_item, parent, false);
        return new SubGroupFileViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(SubGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setSubGroupData(group);
    }

    @Override
    public void onBindChildViewHolder(SubGroupFileViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        //flatPosition -> Representa la posicion del elemento contando tambien a los padres. Ejemplo:
        /*
        * Item       -> flatPostition = 0
        *   Child 1  -> flatPostition = 1
        *   Child 2  -> flatPostition = 2
        * Item       -> flatPostition = 3
        *   Child 1  -> flatPostition = 4
        * */
        final File file = ((SubGroup) group).getItems().get(childIndex);
        holder.setFileData(file,((SubGroup) group).getName(), flatPosition, ((SubGroup) group).getMembers(),((SubGroup) group).getSubGroupKey());
    }


    class SubGroupViewHolder extends GroupViewHolder {

        private TextView subGroupName;
        private CircleImageView subGroupPhoto;
        private ImageButton arrow;
        private ImageButton addFile;

        SubGroupViewHolder(View itemView) {
            super(itemView);
            subGroupName = itemView.findViewById(R.id.subgroup_name);
            subGroupPhoto = itemView.findViewById(R.id.subgroup_photo);
            arrow = itemView.findViewById(R.id.arrow);
            addFile = itemView.findViewById(R.id.add_file);
        }

        void setSubGroupData(final ExpandableGroup group) {
            subGroupName.setText(((SubGroup)group).getName());
            Glide.with(context).load(((SubGroup)group).getImageUrl()).into(subGroupPhoto);

            /*myRol = setMyRol(((SubGroup)group).getMembers());

            if(myRol.equals(Roles.SUBGROUP_ADMIN.toString())){
                addFile.setVisibility(View.VISIBLE);
            }*/

        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }
    }


    class SubGroupFileViewHolder extends ChildViewHolder {

        private TextView fileName;
        private TextView fileSizeTv;
        private TextView uploadTimeTv;
        private ImageView fileIconIv;
        private ImageView fileStatusIv;
        private TextView fileTypeTv;
        private ImageButton btn;

        SubGroupFileViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileSizeTv = itemView.findViewById(R.id.file_size);
            uploadTimeTv = itemView.findViewById(R.id.upload_time);
            fileIconIv = itemView.findViewById(R.id.file_icon);
            fileStatusIv = itemView.findViewById(R.id.file_status);
            fileTypeTv = itemView.findViewById(R.id.file_type);
            btn = itemView.findViewById(R.id.btn_menu);
        }

        void setFileData(final File file, final String subGroupName, final int subGroupPosition, Map<String, String> members, final String subGroupKey) {

            myRol = setMyRol(members);

            fileName.setText(file.getFilename());

            fileSizeTv.setText(readableFileSize((long)file.getFileSize()));

            uploadTimeTv.setText(milisecondsToDate(file.getUploadTime()));

            fileTypeTv.setText(file.getFileType());

            fileIconIv.setImageResource(getIcon(file.getFileType()));
            fileIconIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            fileStatusIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            final java.io.File fileFromPhone = new java.io.File(Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName+"/Sub Grupos/"+subGroupName+"/"+file.getFilename());

            if(!fileFromPhone.exists()){
                Drawable i = context.getResources().getDrawable(R.drawable.arrow_down_bold_circle);
                i.setTint(context.getResources().getColor(R.color.blue_file_download));
                fileStatusIv.setImageDrawable(i);
            }else{
                Drawable i = context.getResources().getDrawable(R.drawable.check_circle);
                i.setTint(context.getResources().getColor(R.color.green_file_download));
                fileStatusIv.setImageDrawable(i);
            }

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(context, view);
                    final Menu menu = popupMenu.getMenu();
                    popupMenu.getMenuInflater().inflate(R.menu.subgroup_files_dialog_fragment_admin_menu, menu);

                    //REVISAR SI A ESTO LE PONGO PERMISOS POR ROLES
                    //popupMenu.getMenu().removeItem(R.id.share);
                    //popupMenu.getMenu().removeItem(R.id.delete);

                    popupMenu.getMenu().removeItem(R.id.publish);

                    if(!myRol.equals(Roles.SUBGROUP_ADMIN.toString())){
                        popupMenu.getMenu().removeItem(R.id.delete);
                    }

                    /*if(!fileFromPhone.exists()){
                        popupMenu.getMenu().removeItem(R.id.share);
                    }*/

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            switch (id){
                                case R.id.download:
                                    //DESCARGA
                                    overrideFile(file.getFileUrl(),file.getFilename(),file.getFileType(),fileFromPhone.exists(),subGroupName, subGroupPosition);
                                    break;
                                case R.id.share:
                                    //COMPARTIR
                                    shareFile(fileFromPhone);
                                    //saveFileToDrive(fileFromPhone);
                                    break;
                                case R.id.delete:
                                    //ELIMINAR
                                    deleteFile(file.getFileKey(),file.getFileUrl(),fileFromPhone,fileFromPhone.exists(),subGroupKey);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
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

        private void overrideFile(final String fileUrl, final String fileName, final String fileType, boolean exists, final String subGroupName, final int subGroupPosition){

            if(exists){
                new AlertDialog.Builder(context,R.style.MyDialogTheme)
                        .setTitle("Este archivo ya existe en su dispositivo")
                        .setMessage("¿Desea sobreescribirlo?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadFile(fileUrl,fileName,fileType,subGroupName,subGroupPosition);
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
                downloadFile(fileUrl,fileName,fileType,subGroupName, subGroupPosition);
            }
        }

        private void downloadFile(String fileUri, final String fileName, String fileType, String subGroupName, final int flatPosition) {

            notificationChannel++;

            String[] fileData = {fileUri,fileName,fileType,subGroupName,groupName};

            final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileData[0]);

            final String folderLocation = Environment.getExternalStorageDirectory()+"/GroupMeApp/Grupos/"+groupName+"/Sub Grupos/"+subGroupName;

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
                            /*int flatChildPos = expandableList.getFlattenedChildIndex(subGroupPosition, filePosition);
                            notifyItemChanged(flatChildPos);*/
                            notifyItemChanged(flatPosition);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("Error: ","File: "+storageRef.getPath()+" ||| " +exception.toString());
                        }
                    });
        }

        private void shareFile(java.io.File fileWithinMyDir) {

            Intent intentShareFile = new Intent(Intent.ACTION_SEND);

            if(fileWithinMyDir.exists()) {
                intentShareFile.setType("*/*");
                String[] mimeTypes = {"image/*","audio/*","video/*","text/*","application/*"};
                //intentShareFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intentShareFile.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+fileWithinMyDir));

                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            /*intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");*/

                context.startActivity(Intent.createChooser(intentShareFile, "Compartir archivo"));
            }
        }

        private void deleteFile(final String fileKey, final String fileUrl, final java.io.File fileFromPhone, final boolean exists, final String subGroupKey) {

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
                                    DatabaseReference fileDataBaseRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupKey).child("subgroups").child(subGroupKey).child("files").child(fileKey);
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
    }

    private String setMyRol(Map<String, String> members) {
        myRol = "";
        for(Map.Entry<String, String> entry: members.entrySet()) {
            if(StaticFirebaseSettings.currentUserId.equals(entry.getKey())){
                myRol = entry.getValue();
                break;
            }
        }
        return myRol;
    }
}
