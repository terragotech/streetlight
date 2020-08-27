package com.terragoedge.streetlight.json.model;

import com.terragoedge.streetlight.enumeration.CallType;

public class SLVTransactionLogs {

    private String parentNoteGuid;
    private String noteGuid;
    private String title;
    private String requestDetails;
    private String requestUrl;
    private String responseBody;
    private long eventTime;
    private long createdDateTime;
    private CallType typeOfCall;
    private boolean isDroppedPinWorkflow;

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

    public boolean isDroppedPinWorkflow() {
        return isDroppedPinWorkflow;
    }

    public void setDroppedPinWorkflow(boolean droppedPinWorkflow) {
        isDroppedPinWorkflow = droppedPinWorkflow;
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
