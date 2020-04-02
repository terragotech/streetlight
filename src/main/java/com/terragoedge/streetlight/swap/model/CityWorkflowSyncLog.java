package com.terragoedge.streetlight.swap.model;

public class CityWorkflowSyncLog {
    private String noteGuid;
    private String eventTime;
    private String noteCreatedDateTime;
    private String title;
    private String macAddress;
    private String fixtureQRScan;
    private String macSyncStatus;
    private String fixtureQRScanSyncStatus;
    private boolean macAddressSynced;
    private boolean fixtureQRScanSynced;

    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getNoteCreatedDateTime() {
        return noteCreatedDateTime;
    }

    public void setNoteCreatedDateTime(String noteCreatedDateTime) {
        this.noteCreatedDateTime = noteCreatedDateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getMacSyncStatus() {
        return macSyncStatus;
    }

    public void setMacSyncStatus(String macSyncStatus) {
        this.macSyncStatus = macSyncStatus;
    }

    public String getFixtureQRScanSyncStatus() {
        return fixtureQRScanSyncStatus;
    }

    public void setFixtureQRScanSyncStatus(String fixtureQRScanSyncStatus) {
        this.fixtureQRScanSyncStatus = fixtureQRScanSyncStatus;
    }

    public boolean isMacAddressSynced() {
        return macAddressSynced;
    }

    public void setMacAddressSynced(boolean macAddressSynced) {
        this.macAddressSynced = macAddressSynced;
    }

    public boolean isFixtureQRScanSynced() {
        return fixtureQRScanSynced;
    }

    public void setFixtureQRScanSynced(boolean fixtureQRScanSynced) {
        this.fixtureQRScanSynced = fixtureQRScanSynced;
    }

    @Override
    public String toString() {
        return "CityWorkflowSyncLog{" +
                "noteGuid='" + noteGuid + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", noteCreatedDateTime='" + noteCreatedDateTime + '\'' +
                ", title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                ", macSyncStatus='" + macSyncStatus + '\'' +
                ", fixtureQRScanSyncStatus='" + fixtureQRScanSyncStatus + '\'' +
                ", macAddressSynced=" + macAddressSynced +
                ", fixtureQRScanSynced=" + fixtureQRScanSynced +
                ", action='" + action + '\'' +
                '}';
    }
}
