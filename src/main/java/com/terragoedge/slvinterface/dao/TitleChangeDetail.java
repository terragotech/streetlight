package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "title_change_detail")
public class TitleChangeDetail {
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "synctime")
    private long synctime;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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


    @Override
    public String toString() {
        return "TitleChangeDetail{" +
                "title='" + title + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", synctime=" + synctime +
                '}';
    }
}
