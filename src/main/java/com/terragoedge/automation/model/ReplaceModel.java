package com.terragoedge.automation.model;

public class ReplaceModel {
    String fixtureid;
    String municipality;
    String workflow;
    String installedmacaddress;
    String expectedmacaddress;
    String replacedmacaddress;
    String modifieddate;
    String user;
    String status;

    public String getFixtureid() {
        return fixtureid;
    }

    public void setFixtureid(String fixtureid) {
        this.fixtureid = fixtureid;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getInstalledmacaddress() {
        return installedmacaddress;
    }

    public void setInstalledmacaddress(String installedmacaddress) {
        this.installedmacaddress = installedmacaddress;
    }

    public String getExpectedmacaddress() {
        return expectedmacaddress;
    }

    public void setExpectedmacaddress(String expectedmacaddress) {
        this.expectedmacaddress = expectedmacaddress;
    }

    public String getReplacedmacaddress() {
        return replacedmacaddress;
    }

    public void setReplacedmacaddress(String replacedmacaddress) {
        this.replacedmacaddress = replacedmacaddress;
    }

    public String getModifieddate() {
        return modifieddate;
    }

    public void setModifieddate(String modifieddate) {
        this.modifieddate = modifieddate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ReplaceModel{" +
                "fixtureid='" + fixtureid + '\'' +
                ", municipality='" + municipality + '\'' +
                ", workflow='" + workflow + '\'' +
                ", installedmacaddress='" + installedmacaddress + '\'' +
                ", expectedmacaddress='" + expectedmacaddress + '\'' +
                ", replacedmacaddress='" + replacedmacaddress + '\'' +
                ", modifieddate='" + modifieddate + '\'' +
                ", user='" + user + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
