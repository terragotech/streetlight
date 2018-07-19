package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SlvSyncDetails")
public class SlvSyncDetails {
    public static final String TALQ_ADDRESS = "TalcAddress";
    public static final String NOTE_GUID = "NoteGuid";
    @DatabaseField(columnName = "Id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "NoteName")
    private String noteName;
    @DatabaseField(columnName = "NoteCreatedBy")
    private String noteCreatedBy;
    @DatabaseField(columnName = "NoteGuid")
    private String noteGuid;
    @DatabaseField(columnName = "NoteCreatedDateTime")
    private long noteCreatedDateTime;
    @DatabaseField(columnName = "ProcessedDateTime")
    private long processedDateTime;
    @DatabaseField(columnName = "Status")
    private String status;
    @DatabaseField(columnName = "ErrorDetails")
    private String errorDetails;
    @DatabaseField(columnName = "TalcAddress")
    private String talcAddress;
    @DatabaseField(columnName = "TalcAddressDateTime")
    private long talcAddressDateTime;
    @DatabaseField(columnName = "MacAddress")
    private String macAddress;

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

    public String getTalcAddress() {
        return talcAddress;
    }

    public void setTalcAddress(String talcAddress) {
        this.talcAddress = talcAddress;
    }

    public long getTalcAddressDateTime() {
        return talcAddressDateTime;
    }

    public void setTalcAddressDateTime(long talcAddressDateTime) {
        this.talcAddressDateTime = talcAddressDateTime;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "SlvSyncDetails{" +
                "id=" + id +
                ", noteName='" + noteName + '\'' +
                ", noteCreatedBy='" + noteCreatedBy + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", noteCreatedDateTime=" + noteCreatedDateTime +
                ", processedDateTime=" + processedDateTime +
                ", status='" + status + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", talcAddress='" + talcAddress + '\'' +
                ", talcAddressDateTime=" + talcAddressDateTime +
                '}';
    }
}
