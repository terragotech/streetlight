package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "comedinstallsync")
public class ComedInstallSyncEntity {
    @DatabaseField(columnName = "syncid")
    private int syncid;
    @DatabaseField(columnName = "noteid")
    private String noteid;

    public int getSyncid() {
        return syncid;
    }

    public void setSyncid(int syncid) {
        this.syncid = syncid;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    @Override
    public String toString() {
        return "ComedInstallSyncEntity{" +
                "syncid=" + syncid +
                ", noteid='" + noteid + '\'' +
                '}';
    }
}
