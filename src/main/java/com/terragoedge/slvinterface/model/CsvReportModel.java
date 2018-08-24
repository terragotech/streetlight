package com.terragoedge.slvinterface.model;

import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.vividsolutions.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

public class CsvReportModel {
    int noteID;
    String idOnController;
    String latitude;
    String longitude;
    String createddatetime;
    String createdBy;
    String revisionOfNoteId;
    String noteType;
    String baseParentNoteId;
    String isCurrent;
    String isTask;
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
    String name;
    String category;
    String isDelete;
    String noteBookName;
    String locationDescription;
    String altitude;
    String satellitesCount;
    String gpsTime;
    String locationProvider;
    String syncTime;
    String selectedRepair;

    public CsvReportModel(EdgeNoteView edgeNoteView) {
        this.noteID = edgeNoteView.getNoteId();
        this.idOnController = edgeNoteView.getTitle();
        this.createddatetime = String.valueOf(edgeNoteView.getCreatedDateTime());
        this.createdBy = edgeNoteView.getCreatedBy();
        this.baseParentNoteId = edgeNoteView.getParentNoteId();
        this.revisionOfNoteId = edgeNoteView.getRevisionfromNoteID();
        this.isCurrent = String.valueOf(edgeNoteView.isCurrent());
        this.isDelete = String.valueOf(edgeNoteView.isDeleted());
        this.noteGuid = edgeNoteView.getNoteGuid();

        //this.createdDate2 = edgeNoteEntity.get
        this.syncTime = String.valueOf(edgeNoteView.getSyncTime());
        this.isTask = String.valueOf(edgeNoteView.isTaskNote());
        this.groupName = edgeNoteView.getGroupName();
        this.groupGuid = edgeNoteView.getGroupGuid();
        this.colorName = edgeNoteView.getColorName();
        //this.noteBookName = edgeNoteView.getN
        this.locationDescription = edgeNoteView.getLocationDescription();
        this.altitude = edgeNoteView.getAltitude();
        this.satellitesCount = String.valueOf(edgeNoteView.getSatellitesCount());
        this.gpsTime = edgeNoteView.getGpsTime();
        this.locationProvider = edgeNoteView.getLocationProvider();
        try {
            if (edgeNoteView.getGeoJson() != null) {
                Feature feature = (Feature) GeoJSONFactory.create(edgeNoteView.getGeoJson());
                // parse Geometry from Feature
                GeoJSONReader reader = new GeoJSONReader();
                Geometry geom = reader.read(feature.getGeometry());
                this.latitude = String.valueOf(geom.getCoordinate().x);
                this.longitude = String.valueOf(geom.getCoordinate().x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public String getIdOnController() {
        return idOnController != null ? idOnController : "";
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
        return longitude != null ? longitude : "";
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCreateddatetime() {
        return createddatetime != null ? createddatetime : "";
    }

    public void setCreateddatetime(String createddatetime) {
        this.createddatetime = createddatetime;
    }

    public String getCreatedBy() {
        return createdBy != null ? createdBy : "";
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


    public String getBaseParentNoteId() {
        return baseParentNoteId;
    }

    public void setBaseParentNoteId(String baseParentNoteId) {
        this.baseParentNoteId = baseParentNoteId;
    }

    public String isCurrent() {
        return isCurrent;
    }

    public void setCurrent(String current) {
        isCurrent = current;
    }

    public String isTask() {
        return isTask;
    }

    public void setTask(String task) {
        isTask = task;
    }

    public String getGroupName() {
        return groupName != null ? groupName : "";
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupGuid() {
        return groupGuid != null ? groupGuid : "";
    }

    public void setGroupGuid(String groupGuid) {
        this.groupGuid = groupGuid;
    }

    public String getColorName() {
        return colorName != null ? colorName : "";
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getFormGuid() {
        return formGuid != null ? formGuid : "";
    }

    public void setFormGuid(String formGuid) {
        this.formGuid = formGuid;
    }

    public String getCreatedDate2() {
        return createdDate2 != null ? createdDate2 : "";
    }

    public void setCreatedDate2(String createdDate2) {
        this.createdDate2 = createdDate2;
    }

    public String getFormTemplateGuid() {
        return formTemplateGuid != null ? formTemplateGuid : "";
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public String getNoteGuid() {
        return noteGuid != null ? noteGuid : "";
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getExistingFixtureInformation() {
        return ExistingFixtureInformation != null ? ExistingFixtureInformation : "";
    }

    public void setExistingFixtureInformation(String existingFixtureInformation) {
        ExistingFixtureInformation = existingFixtureInformation;
    }

    public String getSL() {
        return SL != null ? SL : "";
    }

    public void setSL(String SL) {
        this.SL = SL;
    }

    public String getControllerStrId() {
        return controllerStrId != null ? controllerStrId : "";
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public String getGeoZoneId() {
        return geoZoneId != null ? geoZoneId : "";
    }

    public void setGeoZoneId(String geoZoneId) {
        this.geoZoneId = geoZoneId;
    }

    public String getGhildGeoZoneId() {
        return ghildGeoZoneId != null ? ghildGeoZoneId : "";
    }

    public void setGhildGeoZoneId(String ghildGeoZoneId) {
        this.ghildGeoZoneId = ghildGeoZoneId;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAtlasPhysicalPage() {
        return atlasPhysicalPage != null ? atlasPhysicalPage : "";
    }

    public void setAtlasPhysicalPage(String atlasPhysicalPage) {
        this.atlasPhysicalPage = atlasPhysicalPage;
    }

    public String getFixtureColor() {
        return fixtureColor != null ? fixtureColor : "";
    }

    public void setFixtureColor(String fixtureColor) {
        this.fixtureColor = fixtureColor;
    }

    public String getcDOTLampType() {
        return cDOTLampType != null ? cDOTLampType : "";
    }

    public void setcDOTLampType(String cDOTLampType) {
        this.cDOTLampType = cDOTLampType;
    }

    public String getColorCode() {
        return colorCode != null ? colorCode : "";
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getFixtureCode() {
        return fixtureCode != null ? fixtureCode : "";
    }

    public void setFixtureCode(String fixtureCode) {
        this.fixtureCode = fixtureCode;
    }

    public String getMastArmAngle() {
        return mastArmAngle != null ? mastArmAngle : "";
    }

    public void setMastArmAngle(String mastArmAngle) {
        this.mastArmAngle = mastArmAngle;
    }

    public String getMastArmLength() {
        return mastArmLength != null ? mastArmLength : "";
    }

    public void setMastArmLength(String mastArmLength) {
        this.mastArmLength = mastArmLength;
    }

    public String getMastArmsCount() {
        return mastArmsCount != null ? mastArmsCount : "";
    }

    public void setMastArmsCount(String mastArmsCount) {
        this.mastArmsCount = mastArmsCount;
    }

    public String getProposedContext() {
        return proposedContext != null ? proposedContext : "";
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public String getAction() {
        return action != null ? action : "";
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNodeMACAddress() {
        return nodeMACAddress != null ? nodeMACAddress : "";
    }

    public void setNodeMACAddress(String nodeMACAddress) {
        this.nodeMACAddress = nodeMACAddress;
    }

    public String getFixtureQRScan1() {
        return fixtureQRScan1 != null ? fixtureQRScan1 : "";
    }

    public void setFixtureQRScan1(String fixtureQRScan1) {
        this.fixtureQRScan1 = fixtureQRScan1;
    }

    public String getInstallStatus() {
        return installStatus != null ? installStatus : "";
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getSkippedFixtureReason() {
        return skippedFixtureReason != null ? skippedFixtureReason : "";
    }

    public void setSkippedFixtureReason(String skippedFixtureReason) {
        this.skippedFixtureReason = skippedFixtureReason;
    }

    public String getSkippedReason() {
        return skippedReason != null ? skippedReason : "";
    }

    public void setSkippedReason(String skippedReason) {
        this.skippedReason = skippedReason;
    }

    public String getRepairsAndOutages() {
        return repairsAndOutages != null ? repairsAndOutages : "";
    }

    public void setRepairsAndOutages(String repairsAndOutages) {
        this.repairsAndOutages = repairsAndOutages;
    }

    public String getExistingNodeMACAddress1() {
        return ExistingNodeMACAddress1 != null ? ExistingNodeMACAddress1 : "";
    }

    public void setExistingNodeMACAddress1(String existingNodeMACAddress1) {
        ExistingNodeMACAddress1 = existingNodeMACAddress1;
    }

    public String getNewNodeMACAddress1() {
        return newNodeMACAddress1 != null ? newNodeMACAddress1 : "";
    }

    public void setNewNodeMACAddress1(String newNodeMACAddress1) {
        this.newNodeMACAddress1 = newNodeMACAddress1;
    }

    public String getFixtureQRScan2() {
        return fixtureQRScan2 != null ? fixtureQRScan2 : "";
    }

    public void setFixtureQRScan2(String fixtureQRScan2) {
        this.fixtureQRScan2 = fixtureQRScan2;
    }

    public String getExistingNodeMACAddress2() {
        return ExistingNodeMACAddress2 != null ? ExistingNodeMACAddress2 : "";
    }

    public void setExistingNodeMACAddress2(String existingNodeMACAddress2) {
        ExistingNodeMACAddress2 = existingNodeMACAddress2;
    }

    public String getNewNodeMACAddress2() {
        return NewNodeMACAddress2 != null ? NewNodeMACAddress2 : "";
    }

    public void setNewNodeMACAddress2(String newNodeMACAddress2) {
        NewNodeMACAddress2 = newNodeMACAddress2;
    }

    public String getOldFixtureQRScan() {
        return oldFixtureQRScan != null ? oldFixtureQRScan : "";
    }

    public void setOldFixtureQRScan(String oldFixtureQRScan) {
        this.oldFixtureQRScan = oldFixtureQRScan;
    }

    public String getNewFixtureQRScan() {
        return newFixtureQRScan != null ? newFixtureQRScan : "";
    }

    public void setNewFixtureQRScan(String newFixtureQRScan) {
        this.newFixtureQRScan = newFixtureQRScan;
    }

    public String getReasonForReplacement() {
        return reasonForReplacement != null ? reasonForReplacement : "";
    }

    public void setReasonForReplacement(String reasonForReplacement) {
        this.reasonForReplacement = reasonForReplacement;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String isDelete() {
        return isDelete;
    }

    public void setDelete(String delete) {
        isDelete = delete;
    }

    public String getNoteBookName() {
        return noteBookName != null ? noteBookName : "";
    }

    public void setNoteBookName(String noteBookName) {
        this.noteBookName = noteBookName;
    }

    public String getLocationDescription() {
        return locationDescription != null ? locationDescription : "";
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getAltitude() {
        return altitude != null ? altitude : "";
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSatellitesCount() {
        return satellitesCount != null ? satellitesCount : "";
    }

    public void setSatellitesCount(String satellitesCount) {
        this.satellitesCount = satellitesCount;
    }

    public String getGpsTime() {
        return gpsTime != null ? gpsTime : "";
    }

    public void setGpsTime(String gpsTime) {
        this.gpsTime = gpsTime;
    }

    public String getLocationProvider() {
        return locationProvider != null ? locationProvider : "";
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

    public String getSelectedRepair() {
        return selectedRepair;
    }

    public void setSelectedRepair(String selectedRepair) {
        this.selectedRepair = selectedRepair;
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
