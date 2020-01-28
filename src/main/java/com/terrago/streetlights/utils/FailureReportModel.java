package com.terrago.streetlights.utils;


public class FailureReportModel {
    private int syncid;
    private String fixtureId;
    private String label;
    private String properties;
    private boolean warning;
    private boolean outage;
    private boolean isComplete;
    private String failureReason;
    private String lastUpdate;
    private String failedSince;
    private String burningHours;
    private String lifeTime;

    public String getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(String fixtureId) {
        this.fixtureId = fixtureId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean isOutage() {
        return outage;
    }

    public void setOutage(boolean outage) {
        this.outage = outage;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getFailedSince() {
        return failedSince;
    }

    public void setFailedSince(String failedSince) {
        this.failedSince = failedSince;
    }

    public String getBurningHours() {
        return burningHours;
    }

    public void setBurningHours(String burningHours) {
        this.burningHours = burningHours;
    }

    public String getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(String lifeTime) {
        this.lifeTime = lifeTime;
    }

    public int getSyncid() {
        return syncid;
    }

    public void setSyncid(int syncid) {
        this.syncid = syncid;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    @Override
    public String toString() {
        return "FailureReportModel{" +
                "syncid=" + syncid +
                ", fixtureId='" + fixtureId + '\'' +
                ", label='" + label + '\'' +
                ", properties='" + properties + '\'' +
                ", warning=" + warning +
                ", outage=" + outage +
                ", isComplete=" + isComplete +
                ", failureReason='" + failureReason + '\'' +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", failedSince='" + failedSince + '\'' +
                ", burningHours='" + burningHours + '\'' +
                ", lifeTime='" + lifeTime + '\'' +
                '}';
    }
}
