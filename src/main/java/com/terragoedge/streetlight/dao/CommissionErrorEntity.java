package com.terragoedge.streetlight.dao;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "commission_error")
public class CommissionErrorEntity {
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "note_created_date_time")
    private long noteCreteatedDateTime;
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "processed_time")
    private long processedTime;
    @DatabaseField(columnName = "request",dataType = DataType.LONG_STRING)
    private String request;
    @DatabaseField(columnName = "response",dataType = DataType.LONG_STRING)
    private String response;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getNoteCreteatedDateTime() {
        return noteCreteatedDateTime;
    }

    public void setNoteCreteatedDateTime(long noteCreteatedDateTime) {
        this.noteCreteatedDateTime = noteCreteatedDateTime;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public long getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(long processedTime) {
        this.processedTime = processedTime;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "CommissionErrorEntity{" +
                "title='" + title + '\'' +
                ", noteCreteatedDateTime=" + noteCreteatedDateTime +
                ", noteGuid='" + noteGuid + '\'' +
                ", processedTime=" + processedTime +
                ", request='" + request + '\'' +
                ", response='" + response + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
