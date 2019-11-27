package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "droppinremoveevent")
public class DroppedPinRemoveEvent {

    public  static final String IDONCONTROLLER="idoncontroller";
    public  static final String NOTEGUID="noteguid";
    public  static final String EVENTTIME="eventtime";

    @DatabaseField(columnName = "idoncontroller")
    private String idoncontroller;

    @DatabaseField(columnName = "noteguid")
    private String noteguid;

    @DatabaseField(columnName = "eventtime")
    private long eventTime;

    public String getIdoncontroller() {
        return idoncontroller;
    }

    public void setIdoncontroller(String idoncontroller) {
        this.idoncontroller = idoncontroller;
    }

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "DroppedPinRemoveEvent{" +
                "idoncontroller='" + idoncontroller + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", eventTime=" + eventTime +'}';
    }

}
