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
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "processed_date_time")
    private long processedDateTime;
    @DatabaseField(columnName = "edge_new_node_macaddrss")
    private String edgeNewNodeMacaddress;

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


    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    public String getEdgeNewNodeMacaddress() {
        return edgeNewNodeMacaddress;
    }

    public void setEdgeNewNodeMacaddress(String edgeNewNodeMacaddress) {
        this.edgeNewNodeMacaddress = edgeNewNodeMacaddress;
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
                ", noteGuid='" + noteGuid + '\'' +
                ", processedDateTime='" + processedDateTime + '\'' +
                ", edgeNewNodeMacaddress='" + edgeNewNodeMacaddress + '\'' +
                '}';
    }
}
