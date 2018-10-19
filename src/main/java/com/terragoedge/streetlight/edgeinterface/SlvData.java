package com.terragoedge.streetlight.edgeinterface;

import com.terragoedge.streetlight.service.MessageConstants;

public class SlvData {
    private String noteTitle;
    private String noteGuid;
    private String syncToSlvStatus;
    private String errorDetails;
    private String processedTime;
    private long installedDate;
    private long replacedDate;

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getSyncToSlvStatus() {
        if (syncToSlvStatus.equals(MessageConstants.ERROR)) {
            return "Failure";
        } else if (syncToSlvStatus.equals(MessageConstants.SUCCESS)) {
            return "Success";
        }
        return syncToSlvStatus;
    }

    public void setSyncToSlvStatus(String syncToSlvStatus) {
        this.syncToSlvStatus = syncToSlvStatus;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(String processedTime) {
        this.processedTime = processedTime;
    }

    public long getInstalledDate() {
        return installedDate;
    }

    public void setInstalledDate(long installedDate) {
        this.installedDate = installedDate;
    }

    public long getReplacedDate() {
        return replacedDate;
    }

    public void setReplacedDate(long replacedDate) {
        this.replacedDate = replacedDate;
    }

    @Override
    public String toString() {
        return "SlvData{" +
                "noteTitle='" + noteTitle + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", syncToSlvStatus='" + syncToSlvStatus + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", processedTime='" + processedTime + '\'' +
                ", installedDate='" + installedDate + '\'' +
                ", replacedDate='" + replacedDate + '\'' +
                '}';
    }
}
