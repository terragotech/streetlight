package com.slvinterface.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.slvinterface.enumeration.CallType;

@DatabaseTable(tableName = "slvtransactionlogs")
public class SLVTransactionLogs {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "parentnoteguid")
    private String parentNoteGuid;
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "requestdetails",dataType = DataType.LONG_STRING)
    private String requestDetails;
    @DatabaseField(columnName = "responsebody",dataType = DataType.LONG_STRING)
    private String responseBody;
    @DatabaseField(columnName = "eventtime")
    private long eventTime;
    @DatabaseField(columnName = "createddatetime")
    private long createdDateTime;
    @DatabaseField(columnName = "typeofcall")
    private CallType typeOfCall;
    @DatabaseField(columnName = "requesturl")
    private String requestUrl;

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getParentNoteGuid() {
        return parentNoteGuid;
    }

    public void setParentNoteGuid(String parentNoteGuid) {
        this.parentNoteGuid = parentNoteGuid;
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

    public String getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(String requestDetails) {
        this.requestDetails = requestDetails;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public CallType getTypeOfCall() {
        return typeOfCall;
    }

    public void setTypeOfCall(CallType typeOfCall) {
        this.typeOfCall = typeOfCall;
    }

    @Override
    public String toString() {
        return "SLVTransactionLogs{" +
                "parentNoteGuid='" + parentNoteGuid + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", title='" + title + '\'' +
                ", requestDetails='" + requestDetails + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", eventTime=" + eventTime +
                ", createdDateTime=" + createdDateTime +
                ", typeOfCall=" + typeOfCall +
                '}';
    }
}
