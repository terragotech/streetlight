package com.terrago.streetlights.dao.model;

public class UbiInterfaceLog {
    String notegui;
    String title;
    String urlrequest;
    String requestBody;
    String requestResponse;
    long eventtime;

    public String getNotegui() {
        return notegui;
    }

    public void setNotegui(String notegui) {
        this.notegui = notegui;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlrequest() {
        return urlrequest;
    }

    public void setUrlrequest(String urlrequest) {
        this.urlrequest = urlrequest;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }

    public long getEventtime() {
        return eventtime;
    }

    public void setEventtime(long eventtime) {
        this.eventtime = eventtime;
    }
}
