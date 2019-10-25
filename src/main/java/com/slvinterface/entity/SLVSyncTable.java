package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvsyncdetails")
public class SLVSyncTable {

    public static final String NOTE_GUID = "noteguid";
    public static final String STATUS="status";


    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "title")
    private String noteName;
    @DatabaseField(columnName = "notecreatedby")
    private String noteCreatedBy;
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "notecreateddatetime")
    private long noteCreatedDateTime;
    @DatabaseField(columnName = "processeddatetime")
    private long processedDateTime;
    @DatabaseField(columnName = "synctime")
    private long syncTime;
    @DatabaseField(columnName = "status")
    private String status;

    @DatabaseField(columnName = "errordetails")
    private String errorDetails;


    @DatabaseField(columnName = "parentnoteid")
    private String parentNoteId;

    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getNoteCreatedBy() {
        return noteCreatedBy;
    }

    public void setNoteCreatedBy(String noteCreatedBy) {
        this.noteCreatedBy = noteCreatedBy;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public long getNoteCreatedDateTime() {
        return noteCreatedDateTime;
    }

    public void setNoteCreatedDateTime(long noteCreatedDateTime) {
        this.noteCreatedDateTime = noteCreatedDateTime;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    public long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
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

    public String getParentNoteId() {
        return parentNoteId;
    }

    public void setParentNoteId(String parentNoteId) {
        this.parentNoteId = parentNoteId;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    @Override
    public String toString() {
        return "SLVSyncTable{" +
                "id=" + id +
                ", noteName='" + noteName + '\'' +
                ", noteCreatedBy='" + noteCreatedBy + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", noteCreatedDateTime=" + noteCreatedDateTime +
                ", processedDateTime=" + processedDateTime +
                ", syncTime=" + syncTime +
                ", status='" + status + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", parentNoteId='" + parentNoteId + '\'' +
                ", idOnController='" + idOnController + '\'' +
                '}';
    }
}
