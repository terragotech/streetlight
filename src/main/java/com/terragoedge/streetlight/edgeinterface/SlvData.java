package com.terragoedge.streetlight.edgeinterface;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;

public class SlvData extends CsvToBean {
    @CsvBindByName(column = "Note Title")
    private String noteTitle;
    @CsvBindByName(column = "Note Guid")
    private String noteGuid;
    @CsvBindByName(column = "macAddress")
    private String macAddress;
    @CsvBindByName(column = "FixtureQrScan")
    private String fixtureQRScan;

    private String componentId;
    @CsvBindByName(column = "Component Value")
    private String componentValue;
    @CsvBindByName(column = "New Note Title")
    private String newNoteTitle;
    @CsvBindByName(column = "New Note Guid")
    private String newNoteGuid;
    @CsvBindByName(column = "Status")
    private String status;
    @CsvBindByName(column = "Error Details")
    private String errorDetails;
    @CsvBindByName(column = "Existingmunicipality")
    private String existingMunicipality;
    @CsvBindByName(column = "Municipality")
    private String municipality;


    public String getNewNoteTitle() {
        return newNoteTitle;
    }

    public void setNewNoteTitle(String newNoteTitle) {
        this.newNoteTitle = newNoteTitle;
    }

    public String getNewNoteGuid() {
        return newNoteGuid;
    }

    public void setNewNoteGuid(String newNoteGuid) {
        this.newNoteGuid = newNoteGuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public void setComponentValue(String componentValue) {
        this.componentValue = componentValue;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getExistingMunicipality() {
        return existingMunicipality;
    }

    public void setExistingMunicipality(String existingMunicipality) {
        this.existingMunicipality = existingMunicipality;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    @Override
    public String toString() {
        return "SlvData{" +
                "noteTitle='" + noteTitle + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", componentId='" + componentId + '\'' +
                ", componentValue='" + componentValue + '\'' +
                ", newNoteTitle='" + newNoteTitle + '\'' +
                ", newNoteGuid='" + newNoteGuid + '\'' +
                ", status='" + status + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", existingMunicipality='" + existingMunicipality + '\'' +
                ", municipality='" + municipality + '\'' +
                '}';
    }
}
