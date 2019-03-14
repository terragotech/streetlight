package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "geozone")
public class GeozoneEntity {
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "parishzoneid")
    private int parishzoneId;
    @DatabaseField(columnName = "parishzonename")
    private String parishZoneName;
    @DatabaseField(columnName = "divisionzoneid")
    private int divisionZoneId;
    @DatabaseField(columnName = "divisionzonename")
    private String divisionZoneName;
    @DatabaseField(columnName = "streetzoneid")
    private int streetGeozoneId;
    @DatabaseField(columnName = "streetzonename")
    private String streetZoneName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParishzoneId() {
        return parishzoneId;
    }

    public void setParishzoneId(int parishzoneId) {
        this.parishzoneId = parishzoneId;
    }

    public String getParishZoneName() {
        return parishZoneName;
    }

    public void setParishZoneName(String parishZoneName) {
        this.parishZoneName = parishZoneName;
    }

    public int getDivisionZoneId() {
        return divisionZoneId;
    }

    public void setDivisionZoneId(int divisionZoneId) {
        this.divisionZoneId = divisionZoneId;
    }

    public String getDivisionZoneName() {
        return divisionZoneName;
    }

    public void setDivisionZoneName(String divisionZoneName) {
        this.divisionZoneName = divisionZoneName;
    }

    public int getStreetGeozoneId() {
        return streetGeozoneId;
    }

    public void setStreetGeozoneId(int streetGeozoneId) {
        this.streetGeozoneId = streetGeozoneId;
    }

    public String getStreetZoneName() {
        return streetZoneName;
    }

    public void setStreetZoneName(String streetZoneName) {
        this.streetZoneName = streetZoneName;
    }
}
