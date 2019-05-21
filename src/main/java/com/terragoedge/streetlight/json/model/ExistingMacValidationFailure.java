package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "existing_mac_validation_failure")
public class ExistingMacValidationFailure {
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "createddatetime")
    private long createdDateTime;
    @DatabaseField(columnName = "slvmacaddress")
    private String slvMacaddress;
    @DatabaseField(columnName = "createdby")
    private String createdBy;
    @DatabaseField(columnName = "edge_existing_macaddress")
    private String edgeExistingMacaddress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getSlvMacaddress() {
        return slvMacaddress;
    }

    public void setSlvMacaddress(String slvMacaddress) {
        this.slvMacaddress = slvMacaddress;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getEdgeExistingMacaddress() {
        return edgeExistingMacaddress;
    }

    public void setEdgeExistingMacaddress(String edgeExistingMacaddress) {
        this.edgeExistingMacaddress = edgeExistingMacaddress;
    }

    @Override
    public String toString() {
        return "ExistingMacValidationFailure{" +
                "id=" + id +
                ", idOnController='" + idOnController + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", slvMacaddress='" + slvMacaddress + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", edgeExistingMacaddress='" + edgeExistingMacaddress + '\'' +
                '}';
    }
}
