package com.terrago.streetlights.utils;

public class LastUpdated {
    String noteguid;
    long synctime;
    String title;
    String parentnoteguid;
    String createdBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getParentnoteguid() {
        return parentnoteguid;
    }

    public void setParentnoteguid(String parentnoteguid) {
        this.parentnoteguid = parentnoteguid;
    }

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public long getSynctime() {
        return synctime;
    }

    public void setSynctime(long synctime) {
        this.synctime = synctime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
