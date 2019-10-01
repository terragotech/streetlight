package com.terragoedge.streetlight.logging;

import com.terragoedge.streetlight.json.model.DatesHolder;
import com.terragoedge.streetlight.json.model.SLVDates;

public class InstallMaintenanceLogModel extends LoggingModel {

    private boolean isInstallFormPresent;
    private boolean isFixtureOnly;
    private int deviceId;
    private String comment;

    private String atlasPhysicalPage;

    private boolean isActionNew;


    private boolean isReplace;

    private String communicationStatus;


    private boolean isInstallOnWrongFix;
    private boolean isPoleKnockDown;

    private boolean isAmerescoUser;
    private String proposedContext;


    private boolean isBulkImport;


    public boolean isBulkImport() {
        return isBulkImport;
    }

    public void setBulkImport(boolean bulkImport) {
        isBulkImport = bulkImport;
    }

    public String getProposedContext() {
        return proposedContext;
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public boolean isAmerescoUser() {
        return isAmerescoUser;
    }

    public void setAmerescoUser(boolean amerescoUser) {
        isAmerescoUser = amerescoUser;
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public boolean isInstallOnWrongFix() {
        return isInstallOnWrongFix;
    }

    public void setInstallOnWrongFix(boolean installOnWrongFix) {
        isInstallOnWrongFix = installOnWrongFix;
    }

    public boolean isPoleKnockDown() {
        return isPoleKnockDown;
    }

    public void setPoleKnockDown(boolean poleKnockDown) {
        isPoleKnockDown = poleKnockDown;
    }

    public String getCommunicationStatus() {
        return communicationStatus;
    }

    public void setCommunicationStatus(String communicationStatus) {
        this.communicationStatus = communicationStatus;
    }

    public String getAtlasPhysicalPage() {
        return atlasPhysicalPage;
    }

    public void setAtlasPhysicalPage(String atlasPhysicalPage) {
        this.atlasPhysicalPage = atlasPhysicalPage;
    }

    public boolean isActionNew() {
        return isActionNew;
    }

    public void setActionNew(boolean actionNew) {
        isActionNew = actionNew;
    }

    private DatesHolder datesHolder = new DatesHolder();


    public DatesHolder getDatesHolder() {
        return datesHolder;
    }

    public void setDatesHolder(DatesHolder datesHolder) {
        this.datesHolder = datesHolder;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isInstallFormPresent() {
        return isInstallFormPresent;
    }

    public void setInstallFormPresent(boolean installFormPresent) {
        isInstallFormPresent = installFormPresent;
    }

    public boolean isFixtureOnly() {
        return isFixtureOnly;
    }

    public void setFixtureOnly(boolean fixtureOnly) {
        isFixtureOnly = fixtureOnly;
    }

    @Override
    public String toString() {
        return "InstallMaintenanceLogModel{" +
                "isInstallFormPresent=" + isInstallFormPresent +
                ", isFixtureOnly=" + isFixtureOnly +
                ", deviceId=" + deviceId +
                ", comment='" + comment + '\'' +
                ", atlasPhysicalPage='" + atlasPhysicalPage + '\'' +
                ", isActionNew=" + isActionNew +
                ", isReplace=" + isReplace +
                ", communicationStatus='" + communicationStatus + '\'' +
                ", isInstallOnWrongFix=" + isInstallOnWrongFix +
                ", isPoleKnockDown=" + isPoleKnockDown +
                ", isAmerescoUser=" + isAmerescoUser +
                ", proposedContext='" + proposedContext + '\'' +
                ", datesHolder=" + datesHolder +
                '}';
    }
}
