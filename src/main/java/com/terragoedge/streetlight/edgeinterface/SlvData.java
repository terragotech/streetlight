package com.terragoedge.streetlight.edgeinterface;

public class SlvData {
    private String noteTitle;
    private String noteGuid;
    private String syncToSlvStatus;
    private String errorDetails;
    private String processedTime;

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

    @Override
    public String toString() {
        return "SlvData{" +
                "noteTitle='" + noteTitle + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", syncToSlvStatus='" + syncToSlvStatus + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", processedTime='" + processedTime + '\'' +
                '}';
    }
}
