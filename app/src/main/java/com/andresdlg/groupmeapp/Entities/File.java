package com.andresdlg.groupmeapp.Entities;

/**
 * Created by andresdlg on 28/12/17.
 */

public class File {

    private String filename;
    private String fileUrl;
    private String fileType;
    private float fileSize;
    private long uploadTime;
    private String user;

    private File() {}

    public File(String filename, String fileUrl, String fileType, float fileSize ,long uploadTime, String user){

        this.filename = filename;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.user = user;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
}
