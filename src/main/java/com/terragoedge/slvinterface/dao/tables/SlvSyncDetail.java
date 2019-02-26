package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.terragoedge.slvinterface.enumeration.Status;

@DatabaseTable(tableName = "slvsyncdetails")
public class SlvSyncDetail {
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "noteguid", index = true)
    private String noteGuid;
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "pole_number")
    private String poleNumber;
    @DatabaseField(columnName = "device_details")
    private String deviceDetails;
    @DatabaseField(columnName = "created_date_time")
    private long createdDateTime;
    @DatabaseField(columnName = "processed_date_time")
    private long processedDateTime;
    @DatabaseField(columnName = "status",dataType = DataType.ENUM_STRING)
    private Status status;
    @DatabaseField(columnName = "slv_device_detail_reponse")
    private String slvDeviceDetailsResponse;
    @DatabaseField(columnName = "slv_replace_olc_response")
    private String slvReplaceOLCResponse;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoleNumber() {
        return poleNumber;
    }

    public void setPoleNumber(String poleNumber) {
        this.poleNumber = poleNumber;
    }

    public String getDeviceDetails() {
        return deviceDetails;
    }

    public void setDeviceDetails(String deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSlvDeviceDetailsResponse() {
        return slvDeviceDetailsResponse;
    }

    public void setSlvDeviceDetailsResponse(String slvDeviceDetailsResponse) {
        this.slvDeviceDetailsResponse = slvDeviceDetailsResponse;
    }

    public String getSlvReplaceOLCResponse() {
        return slvReplaceOLCResponse;
    }

    public void setSlvReplaceOLCResponse(String slvReplaceOLCResponse) {
        this.slvReplaceOLCResponse = slvReplaceOLCResponse;
    }

    @Override
    public String toString() {
        return "SlvSyncDetails{" +
                "id=" + id +
                ", noteGuid='" + noteGuid + '\'' +
                ", title='" + title + '\'' +
                ", poleNumber='" + poleNumber + '\'' +
                ", deviceDetails='" + deviceDetails + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", processedDateTime=" + processedDateTime +
                ", status='" + status + '\'' +
                ", slvDeviceDetailsResponse='" + slvDeviceDetailsResponse + '\'' +
                ", slvReplaceOLCResponse='" + slvReplaceOLCResponse + '\'' +
                '}';
    }
}
