package com.terragoedge.streetlight.installmaintain;

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
                '}';
    }


    public boolean hasVal(){
        if(macAddress != null && macAddress.length() > 1){
            return true;
        }
        if(macAddressRNF != null && macAddressRNF.length() > 1){
            return true;
        }
        if(macAddressRN != null && macAddressRN.length() > 1){
            return true;
        }

        if(fixtureQRScan != null && fixtureQRScan.length() > 1){
            return true;
        }

        if(fixtureQRScanRF != null && fixtureQRScanRF.length() > 1){
            return true;
        }

        if(fixtureQRScanRNF != null && fixtureQRScanRNF.length() > 1){
            return true;
        }
        return false;
    }
}
