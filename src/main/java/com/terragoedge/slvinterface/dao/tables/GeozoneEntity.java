package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "geozone")
public class GeozoneEntity {
    public static String NOTEBOOKNAME="geozonename";
    public static String STREETNAME="childgeozonename";
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "geozoneid")
    private int geozoneId;
    @DatabaseField(columnName = "geozonename")
    private String geozoneName;
    @DatabaseField(columnName = "childgeozoneid")
    private int childgeozoneId;
    @DatabaseField(columnName = "childgeozonename")
    private String childgeozoneName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGeozoneId() {
        return geozoneId;
    }

    public void setGeozoneId(int geozoneId) {
        this.geozoneId = geozoneId;
    }

    public String getGeozoneName() {
        return geozoneName;
    }

    public void setGeozoneName(String geozoneName) {
        this.geozoneName = geozoneName;
    }

    public int getChildgeozoneId() {
        return childgeozoneId;
    }

    public void setChildgeozoneId(int childgeozoneId) {
        this.childgeozoneId = childgeozoneId;
    }

    public String getChildgeozoneName() {
        return childgeozoneName;
    }

    public void setChildgeozoneName(String childgeozoneName) {
        this.childgeozoneName = childgeozoneName;
    }
}
