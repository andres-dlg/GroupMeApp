package com.andresdlg.groupmeapp.Entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by andresdlg on 28/12/17.
 */

public class File implements Parcelable {

    private String fileKey;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private float fileSize;
    private long uploadTime;
    private String user;

    private File() {}

    public File(String fileKey, String fileName, String fileUrl, String fileType, float fileSize ,long uploadTime, String user){
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.user = user;
    }

    public String getFilename() {
        return fileName;
    }

    public void setFilename(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public float getFileSize() {
        return fileSize;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    //METODOS AGREGADOS POR LA INTERFACE
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
    }
}
