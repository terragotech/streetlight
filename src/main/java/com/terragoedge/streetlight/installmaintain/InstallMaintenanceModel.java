package com.terragoedge.streetlight.installmaintain;

import java.util.Objects;

public class InstallMaintenanceModel {

    private String macAddress;
    private String macAddressRNF;
    private String exMacAddressRNF;
    private String macAddressRN;
    private String exMacAddressRN;

    private String fixtureQRScan;
    private String fixtureQRScanRNF;
    private String exFixtureQRScanRNF;
    private String fixtureQRScanRF;
    private String exFixtureQRScanRF;

    private String isReplaceNode = "No";



    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddressRNF() {
        return macAddressRNF;
    }

    public void setMacAddressRNF(String macAddressRNF) {
        this.macAddressRNF = macAddressRNF;
    }

    public String getMacAddressRN() {
        return macAddressRN;
    }

    public void setMacAddressRN(String macAddressRN) {
        this.macAddressRN = macAddressRN;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    public String getFixtureQRScanRNF() {
        return fixtureQRScanRNF;
    }

    public void setFixtureQRScanRNF(String fixtureQRScanRNF) {
        this.fixtureQRScanRNF = fixtureQRScanRNF;
    }

    public String getFixtureQRScanRF() {
        return fixtureQRScanRF;
    }

    public void setFixtureQRScanRF(String fixtureQRScanRF) {
        this.fixtureQRScanRF = fixtureQRScanRF;
    }

    public String getExMacAddressRNF() {
        return exMacAddressRNF;
    }

    public void setExMacAddressRNF(String exMacAddressRNF) {
        this.exMacAddressRNF = exMacAddressRNF;
    }

    public String getExMacAddressRN() {
        return exMacAddressRN;
    }

    public void setExMacAddressRN(String exMacAddressRN) {
        this.exMacAddressRN = exMacAddressRN;
    }

    public String getExFixtureQRScanRNF() {
        return exFixtureQRScanRNF;
    }

    public void setExFixtureQRScanRNF(String exFixtureQRScanRNF) {
        this.exFixtureQRScanRNF = exFixtureQRScanRNF;
    }

    public String getExFixtureQRScanRF() {
        return exFixtureQRScanRF;
    }

    public void setExFixtureQRScanRF(String exFixtureQRScanRF) {
        this.exFixtureQRScanRF = exFixtureQRScanRF;
    }

    private void loadExistingMACAddress(){
        if(exMacAddressRN != null && !exMacAddressRN.trim().isEmpty()){
            exMacAddressRNF = exMacAddressRN;
        }
        if(exMacAddressRNF == null || exMacAddressRNF.trim().isEmpty()){
            exMacAddressRNF = macAddress;
        }
    }



   public void checkReplacedDetails(){
       if(macAddressRN != null && !macAddressRN.trim().isEmpty()){
           macAddressRNF = macAddressRN;
           isReplaceNode = "Yes";
           loadExistingMACAddress();
           return;

       }

        if(macAddressRNF != null && !macAddressRNF.trim().isEmpty()){
            isReplaceNode = "Yes";
            loadExistingMACAddress();
            return;
        }

   }

   public boolean hasVal(){
        if(macAddress != null && !macAddress.trim().isEmpty()){
            return true;
        }
       if(macAddressRN != null && !macAddressRN.trim().isEmpty()){
           return true;
       }
       if(macAddressRNF != null && !macAddressRNF.trim().isEmpty()){
           return true;
       }
       if(fixtureQRScan != null && !fixtureQRScan.trim().isEmpty()){
           return true;
       }
       if(fixtureQRScanRF != null && !fixtureQRScanRF.trim().isEmpty()){
           return true;
       }
       if(fixtureQRScanRNF != null && !fixtureQRScanRNF.trim().isEmpty()){
           return true;
       }
       return false;
   }

    public String getIsReplaceNode() {
        return isReplaceNode;
    }

    public void setIsReplaceNode(String isReplaceNode) {
        this.isReplaceNode = isReplaceNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstallMaintenanceModel that = (InstallMaintenanceModel) o;
        return Objects.equals(macAddress, that.macAddress) &&
                Objects.equals(macAddressRNF, that.macAddressRNF) &&
                Objects.equals(macAddressRN, that.macAddressRN) &&
                Objects.equals(fixtureQRScan, that.fixtureQRScan) &&
                Objects.equals(fixtureQRScanRNF, that.fixtureQRScanRNF) &&
                Objects.equals(fixtureQRScanRF, that.fixtureQRScanRF);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress, macAddressRNF, macAddressRN, fixtureQRScan, fixtureQRScanRNF, fixtureQRScanRF);
    }

    @Override
    public String toString() {
        return "InstallMaintenanceModel{" +
                "macAddress='" + macAddress + '\'' +
                ", macAddressRNF='" + macAddressRNF + '\'' +
                ", exMacAddressRNF='" + exMacAddressRNF + '\'' +
                ", macAddressRN='" + macAddressRN + '\'' +
                ", exMacAddressRN='" + exMacAddressRN + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                ", fixtureQRScanRNF='" + fixtureQRScanRNF + '\'' +
                ", exFixtureQRScanRNF='" + exFixtureQRScanRNF + '\'' +
                ", fixtureQRScanRF='" + fixtureQRScanRF + '\'' +
                ", exFixtureQRScanRF='" + exFixtureQRScanRF + '\'' +
                ", isReplaceNode='" + isReplaceNode + '\'' +
                '}';
    }
}
