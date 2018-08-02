package com.terragoedge.slvinterface.model;

import com.terragoedge.slvinterface.entity.EdgeNoteEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;

public class CsvReportModel {
    int noteID;
    String idOnController;
    String latitude;
    String longitude;
    long createddatetime;
    String createdBy;
    String revisionOfNoteId;
    String noteType;
    String baseParentNoteId;
    boolean isCurrent;
    boolean isTask;
    String groupName;
    String groupGuid;
    String colorName;
    String formGuid;
    String createdDate2;
    String formTemplateGuid;
    String noteGuid;
    String ExistingFixtureInformation;
    String SL;
    String controllerStrId;
    String geoZoneId;
    String ghildGeoZoneId;
    String address;
    String atlasPhysicalPage;
    String fixtureColor;
    String cDOTLampType;
    String colorCode;
    String fixtureCode;
    String mastArmAngle;
    String mastArmLength;
    String mastArmsCount;
    String proposedContext;
    String action;
    String nodeMACAddress;
    String fixtureQRScan1;
    String installStatus;
    String skippedFixtureReason;
    String skippedReason;
    String repairsAndOutages;
    String ExistingNodeMACAddress1;
    String newNodeMACAddress1;
    String fixtureQRScan2;
    String ExistingNodeMACAddress2;
    String NewNodeMACAddress2;
    String oldFixtureQRScan;
    String newFixtureQRScan;
    String reasonForReplacement;
    String issue1;
    String dayburner;
    String addComment1;
    String scanExistingMACIfWrong1;
    String reasonForRemoval;
    String issue2;
    String addComment2;
    String scanExistingMACIfWrong2;
    String issue3;
    String last_Updated;
    String name;
    String category;
    boolean isDelete;
    String noteBookName;
    String locationDescription;
    String altitude;
    String satellitesCount;
    String gpsTime;
    String locationProvider;
    String syncTime;

    public CsvReportModel(EdgeNoteView edgeNoteView) {
        this.noteID = edgeNoteView.getNoteId();
        this.createddatetime = edgeNoteView.getCreatedDateTime();
        this.createdBy = edgeNoteView.getCreatedBy();
        this.noteType = edgeNoteView.getNotesType().toString();
        this.baseParentNoteId = edgeNoteView.getParentNoteId();
        this.revisionOfNoteId = edgeNoteView.getRevisionfromNoteID();
        this.isCurrent = edgeNoteView.isCurrent();
        this.isDelete = edgeNoteView.isDeleted();
        this.noteGuid = edgeNoteView.getNoteGuid();

        //this.createdDate2 = edgeNoteEntity.get
        this.syncTime = String.valueOf(edgeNoteView.getSyncTime());
        this.isTask = edgeNoteView.isTaskNote();
        this.groupName = edgeNoteView.getGroupName();
        this.groupGuid = edgeNoteView.getGroupGuid();
        this.colorName = edgeNoteView.getColorName();
        //this.noteBookName = edgeNoteView.getN
        this.locationDescription = edgeNoteView.getLocationDescription();
        this.altitude = edgeNoteView.getAltitude();
        this.satellitesCount = String.valueOf(edgeNoteView.getSatellitesCount());
        this.gpsTime = edgeNoteView.getGpsTime();
        this.locationProvider = edgeNoteView.getLocationProvider();


    }

    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public long getCreateddatetime() {
        return createddatetime;
    }

    public void setCreateddatetime(long createddatetime) {
        this.createddatetime = createddatetime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRevisionOfNoteId() {
        return revisionOfNoteId;
    }

    public void setRevisionOfNoteId(String revisionOfNoteId) {
        this.revisionOfNoteId = revisionOfNoteId;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getBaseParentNoteId() {
        return baseParentNoteId;
    }

    public void setBaseParentNoteId(String baseParentNoteId) {
        this.baseParentNoteId = baseParentNoteId;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public boolean isTask() {
        return isTask;
    }

    public void setTask(boolean task) {
        isTask = task;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupGuid() {
        return groupGuid;
    }

    public void setGroupGuid(String groupGuid) {
        this.groupGuid = groupGuid;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getFormGuid() {
        return formGuid;
    }

    public void setFormGuid(String formGuid) {
        this.formGuid = formGuid;
    }

    public String getCreatedDate2() {
        return createdDate2;
    }

    public void setCreatedDate2(String createdDate2) {
        this.createdDate2 = createdDate2;
    }

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getExistingFixtureInformation() {
        return ExistingFixtureInformation;
    }

    public void setExistingFixtureInformation(String existingFixtureInformation) {
        ExistingFixtureInformation = existingFixtureInformation;
    }

    public String getSL() {
        return SL;
    }

    public void setSL(String SL) {
        this.SL = SL;
    }

    public String getControllerStrId() {
        return controllerStrId;
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public String getGeoZoneId() {
        return geoZoneId;
    }

    public void setGeoZoneId(String geoZoneId) {
        this.geoZoneId = geoZoneId;
    }

    public String getGhildGeoZoneId() {
        return ghildGeoZoneId;
    }

    public void setGhildGeoZoneId(String ghildGeoZoneId) {
        this.ghildGeoZoneId = ghildGeoZoneId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAtlasPhysicalPage() {
        return atlasPhysicalPage;
    }

    public void setAtlasPhysicalPage(String atlasPhysicalPage) {
        this.atlasPhysicalPage = atlasPhysicalPage;
    }

    public String getFixtureColor() {
        return fixtureColor;
    }

    public void setFixtureColor(String fixtureColor) {
        this.fixtureColor = fixtureColor;
    }

    public String getcDOTLampType() {
        return cDOTLampType;
    }

    public void setcDOTLampType(String cDOTLampType) {
        this.cDOTLampType = cDOTLampType;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getFixtureCode() {
        return fixtureCode;
    }

    public void setFixtureCode(String fixtureCode) {
        this.fixtureCode = fixtureCode;
    }

    public String getMastArmAngle() {
        return mastArmAngle;
    }

    public void setMastArmAngle(String mastArmAngle) {
        this.mastArmAngle = mastArmAngle;
    }

    public String getMastArmLength() {
        return mastArmLength;
    }

    public void setMastArmLength(String mastArmLength) {
        this.mastArmLength = mastArmLength;
    }

    public String getMastArmsCount() {
        return mastArmsCount;
    }

    public void setMastArmsCount(String mastArmsCount) {
        this.mastArmsCount = mastArmsCount;
    }

    public String getProposedContext() {
        return proposedContext;
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNodeMACAddress() {
        return nodeMACAddress;
    }

    public void setNodeMACAddress(String nodeMACAddress) {
        this.nodeMACAddress = nodeMACAddress;
    }

    public String getFixtureQRScan1() {
        return fixtureQRScan1;
    }

    public void setFixtureQRScan1(String fixtureQRScan1) {
        this.fixtureQRScan1 = fixtureQRScan1;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getSkippedFixtureReason() {
        return skippedFixtureReason;
    }

    public void setSkippedFixtureReason(String skippedFixtureReason) {
        this.skippedFixtureReason = skippedFixtureReason;
    }

    public String getSkippedReason() {
        return skippedReason;
    }

    public void setSkippedReason(String skippedReason) {
        this.skippedReason = skippedReason;
    }

    public String getRepairsAndOutages() {
        return repairsAndOutages;
    }

    public void setRepairsAndOutages(String repairsAndOutages) {
        this.repairsAndOutages = repairsAndOutages;
    }

    public String getExistingNodeMACAddress1() {
        return ExistingNodeMACAddress1;
    }

    public void setExistingNodeMACAddress1(String existingNodeMACAddress1) {
        ExistingNodeMACAddress1 = existingNodeMACAddress1;
    }

    public String getNewNodeMACAddress1() {
        return newNodeMACAddress1;
    }

    public void setNewNodeMACAddress1(String newNodeMACAddress1) {
        this.newNodeMACAddress1 = newNodeMACAddress1;
    }

    public String getFixtureQRScan2() {
        return fixtureQRScan2;
    }

    public void setFixtureQRScan2(String fixtureQRScan2) {
        this.fixtureQRScan2 = fixtureQRScan2;
    }

    public String getExistingNodeMACAddress2() {
        return ExistingNodeMACAddress2;
    }

    public void setExistingNodeMACAddress2(String existingNodeMACAddress2) {
        ExistingNodeMACAddress2 = existingNodeMACAddress2;
    }

    public String getNewNodeMACAddress2() {
        return NewNodeMACAddress2;
    }

    public void setNewNodeMACAddress2(String newNodeMACAddress2) {
        NewNodeMACAddress2 = newNodeMACAddress2;
    }

    public String getOldFixtureQRScan() {
        return oldFixtureQRScan;
    }

    public void setOldFixtureQRScan(String oldFixtureQRScan) {
        this.oldFixtureQRScan = oldFixtureQRScan;
    }

    public String getNewFixtureQRScan() {
        return newFixtureQRScan;
    }

    public void setNewFixtureQRScan(String newFixtureQRScan) {
        this.newFixtureQRScan = newFixtureQRScan;
    }

    public String getReasonForReplacement() {
        return reasonForReplacement;
    }

    public void setReasonForReplacement(String reasonForReplacement) {
        this.reasonForReplacement = reasonForReplacement;
    }

    public String getIssue1() {
        return issue1;
    }

    public void setIssue1(String issue1) {
        this.issue1 = issue1;
    }

    public String getDayburner() {
        return dayburner;
    }

    public void setDayburner(String dayburner) {
        this.dayburner = dayburner;
    }

    public String getAddComment1() {
        return addComment1;
    }

    public void setAddComment1(String addComment1) {
        this.addComment1 = addComment1;
    }

    public String getScanExistingMACIfWrong1() {
        return scanExistingMACIfWrong1;
    }

    public void setScanExistingMACIfWrong1(String scanExistingMACIfWrong1) {
        this.scanExistingMACIfWrong1 = scanExistingMACIfWrong1;
    }

    public String getReasonForRemoval() {
        return reasonForRemoval;
    }

    public void setReasonForRemoval(String reasonForRemoval) {
        this.reasonForRemoval = reasonForRemoval;
    }

    public String getIssue2() {
        return issue2;
    }

    public void setIssue2(String issue2) {
        this.issue2 = issue2;
    }

    public String getAddComment2() {
        return addComment2;
    }

    public void setAddComment2(String addComment2) {
        this.addComment2 = addComment2;
    }

    public String getScanExistingMACIfWrong2() {
        return scanExistingMACIfWrong2;
    }

    public void setScanExistingMACIfWrong2(String scanExistingMACIfWrong2) {
        this.scanExistingMACIfWrong2 = scanExistingMACIfWrong2;
    }

    public String getIssue3() {
        return issue3;
    }

    public void setIssue3(String issue3) {
        this.issue3 = issue3;
    }

    public String getLast_Updated() {
        return last_Updated;
    }

    public void setLast_Updated(String last_Updated) {
        this.last_Updated = last_Updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public String getNoteBookName() {
        return noteBookName;
    }

    public void setNoteBookName(String noteBookName) {
        this.noteBookName = noteBookName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSatellitesCount() {
        return satellitesCount;
    }

    public void setSatellitesCount(String satellitesCount) {
        this.satellitesCount = satellitesCount;
    }

    public String getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(String gpsTime) {
        this.gpsTime = gpsTime;
    }

    public String getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(String locationProvider) {
        this.locationProvider = locationProvider;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    @Override
    public String toString() {
        return "CsvReportModel{" +
                "noteID='" + noteID + '\'' +
                ", idOnController='" + idOnController + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", createddatetime='" + createddatetime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", revisionOfNoteId='" + revisionOfNoteId + '\'' +
                ", noteType='" + noteType + '\'' +
                ", baseParentNoteId='" + baseParentNoteId + '\'' +
                ", isCurrent=" + isCurrent +
                ", isTask=" + isTask +
                ", groupName='" + groupName + '\'' +
                ", groupGuid='" + groupGuid + '\'' +
                ", colorName='" + colorName + '\'' +
                ", formGuid='" + formGuid + '\'' +
                ", createdDate2='" + createdDate2 + '\'' +
                ", formTemplateGuid='" + formTemplateGuid + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", ExistingFixtureInformation='" + ExistingFixtureInformation + '\'' +
                ", SL='" + SL + '\'' +
                ", controllerStrId='" + controllerStrId + '\'' +
                ", geoZoneId='" + geoZoneId + '\'' +
                ", ghildGeoZoneId='" + ghildGeoZoneId + '\'' +
                ", address='" + address + '\'' +
                ", atlasPhysicalPage='" + atlasPhysicalPage + '\'' +
                ", fixtureColor='" + fixtureColor + '\'' +
                ", cDOTLampType='" + cDOTLampType + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", fixtureCode='" + fixtureCode + '\'' +
                ", mastArmAngle='" + mastArmAngle + '\'' +
                ", mastArmLength='" + mastArmLength + '\'' +
                ", mastArmsCount='" + mastArmsCount + '\'' +
                ", proposedContext='" + proposedContext + '\'' +
                ", action='" + action + '\'' +
                ", nodeMACAddress='" + nodeMACAddress + '\'' +
                ", fixtureQRScan1='" + fixtureQRScan1 + '\'' +
                ", installStatus='" + installStatus + '\'' +
                ", skippedFixtureReason='" + skippedFixtureReason + '\'' +
                ", skippedReason='" + skippedReason + '\'' +
                ", repairsAndOutages='" + repairsAndOutages + '\'' +
                ", ExistingNodeMACAddress1='" + ExistingNodeMACAddress1 + '\'' +
                ", newNodeMACAddress1='" + newNodeMACAddress1 + '\'' +
                ", fixtureQRScan2='" + fixtureQRScan2 + '\'' +
                ", ExistingNodeMACAddress2='" + ExistingNodeMACAddress2 + '\'' +
                ", NewNodeMACAddress2='" + NewNodeMACAddress2 + '\'' +
                ", oldFixtureQRScan='" + oldFixtureQRScan + '\'' +
                ", newFixtureQRScan='" + newFixtureQRScan + '\'' +
                ", reasonForReplacement='" + reasonForReplacement + '\'' +
                ", issue1='" + issue1 + '\'' +
                ", dayburner='" + dayburner + '\'' +
                ", addComment1='" + addComment1 + '\'' +
                ", scanExistingMACIfWrong1='" + scanExistingMACIfWrong1 + '\'' +
                ", reasonForRemoval='" + reasonForRemoval + '\'' +
                ", issue2='" + issue2 + '\'' +
                ", addComment2='" + addComment2 + '\'' +
                ", scanExistingMACIfWrong2='" + scanExistingMACIfWrong2 + '\'' +
                ", issue3='" + issue3 + '\'' +
                ", last_Updated='" + last_Updated + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", isDelete=" + isDelete +
                ", noteBookName='" + noteBookName + '\'' +
                ", locationDescription='" + locationDescription + '\'' +
                ", altitude='" + altitude + '\'' +
                ", satellitesCount='" + satellitesCount + '\'' +
                ", gpsTime='" + gpsTime + '\'' +
                ", locationProvider='" + locationProvider + '\'' +
                ", syncTime='" + syncTime + '\'' +
                '}';
    }
}
