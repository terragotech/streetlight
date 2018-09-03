package com.terragoedge.automation.model;

import java.util.Objects;

public class MacValidationModel {
    String macaddress;
    String fixtureid;
    String municipality;
    String modifieddate;
    String user;
    String assigneduser;
    String installStatus;
    String inventoryStatus;

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

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

    public String getAssigneduser() {
        return assigneduser;
    }

    public void setAssigneduser(String assigneduser) {
        this.assigneduser = assigneduser;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        if(this.installStatus != null){
            this.installStatus = this.installStatus + "|"+installStatus;
        }else{
            this.installStatus = installStatus;
        }

    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        if(this.inventoryStatus != null){
            this.inventoryStatus = this.inventoryStatus + "|"+inventoryStatus;
        }else{
            this.inventoryStatus = inventoryStatus;
        }
    }

    @Override
    public String toString() {
        return "MacValidationModel{" +
                "macaddress='" + macaddress + '\'' +
                ", fixtureid='" + fixtureid + '\'' +
                ", municipality='" + municipality + '\'' +
                ", modifieddate='" + modifieddate + '\'' +
                ", user='" + user + '\'' +
                ", assigneduser='" + assigneduser + '\'' +
                ", installStatus='" + installStatus + '\'' +
                ", inventoryStatus='" + inventoryStatus + '\'' +
                '}';
    }
}
