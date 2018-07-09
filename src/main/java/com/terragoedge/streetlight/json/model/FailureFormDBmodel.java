package com.terragoedge.streetlight.json.model;

public class FailureFormDBmodel {
    private String noteid;
    private String status;
    private String errorDetails;
    private String createdDatetime;
    private String noteName;
    private String modelJson;
    private String processDateTime;
    private String newNoteGuid;
    private String poleStatus;


    public String getPoleStatus() {
        return poleStatus;
    }

    public void setPoleStatus(String poleStatus) {
        this.poleStatus = poleStatus;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(String createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getModelJson() {
        return modelJson;
    }

    public void setModelJson(String modelJson) {
        this.modelJson = modelJson;
    }

    public String getProcessDateTime() {
        return processDateTime;
    }

    public void setProcessDateTime(String processDateTime) {
        this.processDateTime = processDateTime;
    }

    public String getNewNoteGuid() {
        return newNoteGuid;
    }

    public void setNewNoteGuid(String newNoteGuid) {
        this.newNoteGuid = newNoteGuid;
    }

    @Override
    public String toString() {
        return "FailureFormDBmodel{" +
                "noteid='" + noteid + '\'' +
                ", status='" + status + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", createdDatetime='" + createdDatetime + '\'' +
                ", noteName='" + noteName + '\'' +
                ", modelJson='" + modelJson + '\'' +
                ", processDateTime='" + processDateTime + '\'' +
                ", newNoteGuid='" + newNoteGuid + '\'' +
                '}';
    }
}
