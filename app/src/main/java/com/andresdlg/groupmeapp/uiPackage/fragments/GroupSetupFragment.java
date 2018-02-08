package com.andresdlg.groupmeapp.uiPackage.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andresdlg.groupmeapp.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by andresdlg on 05/02/18.
 */

public class GroupSetupFragment extends Fragment {
    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */

    final int REQUEST_CODE = 200;

    ImageView mGroupPhoto;
    Uri mCropImageUri;
    Uri imageHoldUri;


    OnGroupImageSetListener mOnGroupImageSetListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups_dialog_setup, container, false);


        mGroupPhoto = view.findViewById(R.id.add_group_photo);
        mGroupPhoto.setColorFilter(ContextCompat.getColor(getContext(), R.color.add_photo));

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectImageClick(view);
            }
        });

        return view;
    }

    private void onSelectImageClick(View view) {
        Intent i = CropImage.getPickImageChooserIntent(getContext());
        startActivityForResult(i,REQUEST_CODE);
    }



    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(16,9)
                .setFixAspectRatio(true)
                .start(getContext(),this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getActivity(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageHoldUri = result.getUri();
                mGroupPhoto.setColorFilter(null);
                mGroupPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mGroupPhoto.setImageURI(imageHoldUri);

                mOnGroupImageSetListener.onGroupImageSet(imageHoldUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (imageHoldUri != null){
            mGroupPhoto.setColorFilter(null);
            mGroupPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mGroupPhoto.setImageURI(imageHoldUri);
        }
    }


    public interface OnGroupImageSetListener{
        public void onGroupImageSet(Uri imageUri);
    }

    public void onAttachToParentFragment(Fragment fragment){
        try {
            mOnGroupImageSetListener = (OnGroupImageSetListener) fragment;
        }
        catch (ClassCastException e){
            throw new ClassCastException(fragment.toString() + " must implement OnUserSelectionSetListener");
        }
    }

}
