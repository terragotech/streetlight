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

    private DatesHolder datesHolder;


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
                ", isActionNew=" + isActionNew +
                ", isFixtureOnly=" + isFixtureOnly +
                ", deviceId=" + deviceId +
                ", comment='" + comment + '\'' +
                '}';
    }
}
