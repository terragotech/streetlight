package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvsyncdetails")
public class SlvSyncDetails {
    public static final String TALQ_ADDRESS = "talcaddress";
    public static final String NOTE_GUID = "noteguid";
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;
    @DatabaseField(columnName = "notename")
    private String noteName;
    @DatabaseField(columnName = "notecreatedby")
    private String noteCreatedBy;
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "notecreateddatetime")
    private long noteCreatedDateTime;
    @DatabaseField(columnName = "processeddatetime")
    private long processedDateTime;
    @DatabaseField(columnName = "status")
    private String status;
    @DatabaseField(columnName = "errordetails")
    private String errorDetails;
    @DatabaseField(columnName = "talcaddress")
    private String talcAddress;
    @DatabaseField(columnName = "talcaddressdatetime")
    private long talcAddressDateTime;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    @DatabaseField(columnName = "devicecreationstatus")
    private String deviceCreationStatus;

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
        this.errorDetails = this.errorDetails +".\n"+errorDetails;
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

    public String getDeviceCreationStatus() {
        return deviceCreationStatus;
    }

    public void setDeviceCreationStatus(String deviceCreationStatus) {
        this.deviceCreationStatus = deviceCreationStatus;
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
                ", macAddress='" + macAddress + '\'' +
                ", deviceCreationStatus='" + deviceCreationStatus + '\'' +
                '}';
    }
}
