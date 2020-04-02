package com.terragoedge.slvinterface.model;

public class OutageData {
    String fixtureid;
    String label;
    String devicecategory;
    String deviceid;
    String warning;
    String outage;
    String iscomplete;
    String failure_reason;
    String lastupdate;
    String failedsince;
    String burninghours;
    String lifetime;

    public String getFixtureid() {
        return fixtureid;
    }

    public void setFixtureid(String fixtureid) {
        this.fixtureid = fixtureid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDevicecategory() {
        return devicecategory;
    }

    public void setDevicecategory(String devicecategory) {
        this.devicecategory = devicecategory;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getOutage() {
        return outage;
    }

    public void setOutage(String outage) {
        this.outage = outage;
    }

    public String getIscomplete() {
        return iscomplete;
    }

    public void setIscomplete(String iscomplete) {
        this.iscomplete = iscomplete;
    }

    public String getFailure_reason() {
        return failure_reason;
    }

    public void setFailure_reason(String failure_reason) {
        this.failure_reason = failure_reason;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getFailedsince() {
        return failedsince;
    }

    public void setFailedsince(String failedsince) {
        this.failedsince = failedsince;
    }

    public String getBurninghours() {
        return burninghours;
    }

    public void setBurninghours(String burninghours) {
        this.burninghours = burninghours;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }
}

