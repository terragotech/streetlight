package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edgenote")
public class EdgeNoteEntity {
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "noteid")
    private int noteid;

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public int getNoteid() {
        return noteid;
    }

    public void setNoteid(int noteid) {
        this.noteid = noteid;
    }

    @Override
    public String toString() {
        return "EdgeNoteEntity{" +
                "noteguid='" + noteguid + '\'' +
                ", noteid=" + noteid +
                '}';
    }
}
