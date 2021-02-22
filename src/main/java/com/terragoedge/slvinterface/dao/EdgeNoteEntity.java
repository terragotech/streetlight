package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edgenote")
public class EdgeNoteEntity {
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "synctime")
    private String synctime;
    @DatabaseField(columnName = "createdby")
    private String createdby;


    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public String getSynctime() {
        return synctime;
    }

    public void setSynctime(String synctime) {
        this.synctime = synctime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    @Override
    public String toString() {
        return "EdgeNoteEntity{" +
                "noteguid='" + noteguid + '\'' +
                ", synctime='" + synctime + '\'' +
                ", createdby='" + createdby + '\'' +
                '}';
    }
}
