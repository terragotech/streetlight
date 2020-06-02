package com.terragoedge.streetlight.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "commission_failure_data")
public class CommissionFailureData {

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "macaddress")
    private String macaddress;
    @DatabaseField(columnName = "idoncontroller")
    private String idoncontroller;
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "parentnoteguid")
    private String parentnoteguid;
    @DatabaseField(columnName = "createddatetime")
    private long createddatetime;
    @DatabaseField(columnName = "synctime")
    private long synctime;
    @DatabaseField(columnName = "eventtime")
    private long eventtime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getIdoncontroller() {
        return idoncontroller;
    }

    public void setIdoncontroller(String idoncontroller) {
        this.idoncontroller = idoncontroller;
    }

    public String getParentnoteguid() {
        return parentnoteguid;
    }

    public void setParentnoteguid(String parentnoteguid) {
        this.parentnoteguid = parentnoteguid;
    }

    public long getCreateddatetime() {
        return createddatetime;
    }

    public void setCreateddatetime(long createddatetime) {
        this.createddatetime = createddatetime;
    }

    public long getSynctime() {
        return synctime;
    }

    public void setSynctime(long synctime) {
        this.synctime = synctime;
    }

    public long getEventtime() {
        return eventtime;
    }

    public void setEventtime(long eventtime) {
        this.eventtime = eventtime;
    }


    @Override
    public String toString() {
        return "CommissionFailureData{" +
                "id=" + id +
                ", macaddress='" + macaddress + '\'' +
                ", idoncontroller='" + idoncontroller + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", parentnoteguid='" + parentnoteguid + '\'' +
                ", createddatetime=" + createddatetime +
                ", synctime=" + synctime +
                ", eventtime=" + eventtime +
                '}';
    }
}
