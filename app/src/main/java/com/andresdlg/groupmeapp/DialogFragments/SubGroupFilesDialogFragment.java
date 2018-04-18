package com.andresdlg.groupmeapp.DialogFragments;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
import com.andresdlg.groupmeapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by andresdlg on 15/04/18.
 */

public class SubGroupFilesDialogFragment extends DialogFragment {

    private static final int RESULT_LOAD_FILES = 1;
    private String subGroupName;
    private String subGroupUrlPhoto;
    private String subGroupKey;
    private String groupKey;

    List<File> files;
    RVFilesAdapter rvFilesAdapter;

    public SubGroupFilesDialogFragment(String subGroupName, String subGroupUrlPhoto, String subGroupKey, String groupKey) {
        this.subGroupName = subGroupName;
        this.subGroupUrlPhoto = subGroupUrlPhoto;
        this.subGroupKey = subGroupKey;
        this.groupKey = groupKey;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        RecyclerView rv = v.findViewById(R.id.rvFiles);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        rvFilesAdapter = new RVFilesAdapter(files,getContext());

        rv.setAdapter(rvFilesAdapter);

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

                    String[] fileData = getFileData(fileUri);

                    File file = new File(fileData[0],null,fileData[2], Float.valueOf(fileData[1]),0,null);

                    files.add(file);

                    rvFilesAdapter.notifyDataSetChanged();

                }

                Toast.makeText(getContext(), "Ha seleccionado varios archivos!", Toast.LENGTH_SHORT).show();
            }else if(data.getData() != null){
                Toast.makeText(getContext(), "Ha seleccionado un solo archivo!", Toast.LENGTH_SHORT).show();
            }
        }
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

        String fileType = getFileType(uri);

        return new String[]{fileName,fileSize,fileType};
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

}