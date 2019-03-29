package com.terragoedge.slvinterface.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;


@DatabaseTable(tableName = "installmaintenancereport")
public class InstallationReportModel extends CsvToBean {
    @CsvBindByName(column = "title")
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "noteid")
    private String noteid;
    @DatabaseField(columnName = "locationdescription")
    private String locationDescription;
    @DatabaseField(columnName = "createdby")
    private String createdBy;
    @DatabaseField(columnName = "createddatetime")
    private long createdDatetime;
    @DatabaseField(columnName = "atlas_physical_page")
    private String atlasPhysicalPage;
    @DatabaseField(columnName = "address")
    private String address;
    @DatabaseField(columnName = "proposed_context")
    private String proposedContext;
    @DatabaseField(columnName = "group_name")
    private String groupName;
    @DatabaseField(columnName = "lat")
    private String lat;
    @DatabaseField(columnName = "lng")
    private String lng;
    @DatabaseField(columnName = "slv_sync_status")
    private String slvSyncStatus;
    @DatabaseField(columnName = "failure_reason")
    private String slvFailureReason;
    @DatabaseField(columnName = "action")
    private String action;
    @DatabaseField(columnName = "nfr_existing_macaddress")
    private String existingNodeMacAddressNFR;
    @DatabaseField(columnName = "nfr_new_macaddress")
    private String newNodeMacAddressNFR;
    @DatabaseField(columnName = "nfr_fixture_qrscan")
    private String fixtureQrScanNFR;
    @DatabaseField(columnName = "rn_existing_macaddress")
    private String existingNodeMacAddressNR;
    @DatabaseField(columnName = "rn_new_macaddress")
    private String newNodeMacAddressNR;
    @DatabaseField(columnName = "rf_old_qrscan")
    private String oldFixtureQrScanFR;
    @DatabaseField(columnName = "rf_new_qrscan")
    private String newFixtureQrScanFR;
    @DatabaseField(columnName = "node_mac_address")
    private String nodeMacAddressNew;
    @DatabaseField(columnName = "fixture_qr_scan")
    private String fixtureQrScanNew;
    @DatabaseField(columnName = "repairs_action")
    private String repairsAction;
    @DatabaseField(columnName = "install_status")
    private String installStatus;

   /* @DatabaseField(columnName = "parentnoteid")
    private String parentnoteid;*/

    @DatabaseField(columnName = "syncTime")
    private String syncTime;

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExistingNodeMacAddressNFR() {
        return existingNodeMacAddressNFR;
    }

    public void setExistingNodeMacAddressNFR(String existingNodeMacAddressNFR) {
        this.existingNodeMacAddressNFR = existingNodeMacAddressNFR;
    }

    public String getNewNodeMacAddressNFR() {
        return newNodeMacAddressNFR;
    }

    public void setNewNodeMacAddressNFR(String newNodeMacAddressNFR) {
        this.newNodeMacAddressNFR = newNodeMacAddressNFR;
    }

    public String getFixtureQrScanNFR() {
        return fixtureQrScanNFR;
    }

    public void setFixtureQrScanNFR(String fixtureQrScanNFR) {
        this.fixtureQrScanNFR = fixtureQrScanNFR;
    }

    public String getExistingNodeMacAddressNR() {
        return existingNodeMacAddressNR;
    }

    public void setExistingNodeMacAddressNR(String existingNodeMacAddressNR) {
        this.existingNodeMacAddressNR = existingNodeMacAddressNR;
    }

    public String getNewNodeMacAddressNR() {
        return newNodeMacAddressNR;
    }

    public void setNewNodeMacAddressNR(String newNodeMacAddressNR) {
        this.newNodeMacAddressNR = newNodeMacAddressNR;
    }

    public String getOldFixtureQrScanFR() {
        return oldFixtureQrScanFR;
    }

    public void setOldFixtureQrScanFR(String oldFixtureQrScanFR) {
        this.oldFixtureQrScanFR = oldFixtureQrScanFR;
    }

    public String getNewFixtureQrScanFR() {
        return newFixtureQrScanFR;
    }

    public void setNewFixtureQrScanFR(String newFixtureQrScanFR) {
        this.newFixtureQrScanFR = newFixtureQrScanFR;
    }

    public String getNodeMacAddressNew() {
        return nodeMacAddressNew;
    }

    public void setNodeMacAddressNew(String nodeMacAddressNew) {
        this.nodeMacAddressNew = nodeMacAddressNew;
    }

    public String getFixtureQrScanNew() {
        return fixtureQrScanNew;
    }

    public void setFixtureQrScanNew(String fixtureQrScanNew) {
        this.fixtureQrScanNew = fixtureQrScanNew;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(long createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getAtlasPhysicalPage() {
        return atlasPhysicalPage;
    }

    public void setAtlasPhysicalPage(String atlasPhysicalPage) {
        this.atlasPhysicalPage = atlasPhysicalPage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProposedContext() {
        return proposedContext;
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRepairsAction() {
        return repairsAction;
    }

    public void setRepairsAction(String repairsAction) {
        this.repairsAction = repairsAction;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }



    public String getSlvSyncStatus() {
        return slvSyncStatus;
    }

    public void setSlvSyncStatus(String slvSyncStatus) {
        this.slvSyncStatus = slvSyncStatus;
    }

    public String getSlvFailureReason() {
        return slvFailureReason;
    }

    public void setSlvFailureReason(String slvFailureReason) {
        this.slvFailureReason = slvFailureReason;
    }

    @Override
    public String toString() {
        return "InstallationReportModel{" +
                "title='" + title + '\'' +
                ", noteid='" + noteid + '\'' +
                ", locationDescription='" + locationDescription + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdDatetime=" + createdDatetime +
                ", atlasPhysicalPage='" + atlasPhysicalPage + '\'' +
                ", address='" + address + '\'' +
                ", proposedContext='" + proposedContext + '\'' +
                ", groupName='" + groupName + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", slvSyncStatus='" + slvSyncStatus + '\'' +
                ", slvFailureReason='" + slvFailureReason + '\'' +
                ", action='" + action + '\'' +
                ", existingNodeMacAddressNFR='" + existingNodeMacAddressNFR + '\'' +
                ", newNodeMacAddressNFR='" + newNodeMacAddressNFR + '\'' +
                ", fixtureQrScanNFR='" + fixtureQrScanNFR + '\'' +
                ", existingNodeMacAddressNR='" + existingNodeMacAddressNR + '\'' +
                ", newNodeMacAddressNR='" + newNodeMacAddressNR + '\'' +
                ", oldFixtureQrScanFR='" + oldFixtureQrScanFR + '\'' +
                ", newFixtureQrScanFR='" + newFixtureQrScanFR + '\'' +
                ", nodeMacAddressNew='" + nodeMacAddressNew + '\'' +
                ", fixtureQrScanNew='" + fixtureQrScanNew + '\'' +
                ", repairsAction='" + repairsAction + '\'' +
                ", installStatus='" + installStatus + '\'' +
                ", syncTime='" + syncTime + '\'' +
                '}';
    }
}
