package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "inbcomhistory")
public class CommStatusNBHistoryEntity {
    public static String FIXTURE_ID = "fixtureid";
    public static String PREVNBGUID = "prevnbguid";
    public static String PREVNLGUID = "prevlrguid";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "fixtureid")
    private String fixtureid;

    @DatabaseField(columnName = "prevnbguid")
    private String prevnbguid;

    @DatabaseField(columnName = "prevlrguid")
    private String prevlrguid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFixtureid() {
        return fixtureid;
    }

    public void setFixtureid(String fixtureid) {
        this.fixtureid = fixtureid;
    }

    public String getPrevnbguid() {
        return prevnbguid;
    }

    public void setPrevnbguid(String prevnbguid) {
        this.prevnbguid = prevnbguid;
    }

    public String getPrevlrguid() {
        return prevlrguid;
    }

    public void setPrevlrguid(String prevlrguid) {
        this.prevlrguid = prevlrguid;
    }
}
