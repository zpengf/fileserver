package com.zpf.dto;

import java.util.Date;



public class FileInfo {

    public FileInfo(long fileSize, String fileName) {
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public FileInfo() {

    }

    private Long id;

    private String fileName;

    private String fileCode;

    private Date createTime;

    private Long fileSize;

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
