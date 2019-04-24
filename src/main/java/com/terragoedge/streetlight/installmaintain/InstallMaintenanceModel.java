package com.terragoedge.streetlight.installmaintain;

import com.terragoedge.streetlight.installmaintain.utills.Constants;

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

    private String removalReason;
    //Resolved
    private String resolvedIssue;
    private String resolvedComment;
    private String existingMacIfWrong;
    //Unable To Repair
    private String unableToRepairIssue;
    private String unableToRepairComment;
    //Install status
    private String installStatus;
    private String skippedFixtureReason;
    private String skippedReason;
    private String reasonforReplacement;
    private String action;


    public String getRemovalReason() {
        return removalReason;
    }

    public void setRemovalReason(String removalReason) {
        if (removalReason != null && !removalReason.equals("Not selected") && !removalReason.equals("Select From Below")) {
            this.removalReason = removalReason;
        }

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

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

    public String getResolvedIssue() {
        return resolvedIssue;
    }

    public void setResolvedIssue(String resolvedIssue) {
        this.resolvedIssue = resolvedIssue;
    }

    public String getResolvedComment() {
        return resolvedComment;
    }

    public void setResolvedComment(String resolvedComment) {
        this.resolvedComment = resolvedComment;
    }

    public String getExistingMacIfWrong() {
        return existingMacIfWrong;
    }

    public void setExistingMacIfWrong(String existingMacIfWrong) {
        this.existingMacIfWrong = existingMacIfWrong;
    }

    public String getUnableToRepairIssue() {
        return unableToRepairIssue;
    }

    public void setUnableToRepairIssue(String unableToRepairIssue) {
        this.unableToRepairIssue = unableToRepairIssue;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getReasonforReplacement() {
        return reasonforReplacement;
    }

    public void setReasonforReplacement(String reasonforReplacement) {
        this.reasonforReplacement = reasonforReplacement;
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

    private void loadExistingMACAddress() {
        if (exMacAddressRN != null && !exMacAddressRN.trim().isEmpty()) {
            exMacAddressRNF = exMacAddressRN;
        }
        if (exMacAddressRNF == null || exMacAddressRNF.trim().isEmpty()) {
            exMacAddressRNF = macAddress;
        }
    }


    public void checkReplacedDetails() {
        if (macAddressRN != null && !macAddressRN.trim().isEmpty()) {
            macAddressRNF = macAddressRN;
            isReplaceNode = "Yes";
            loadExistingMACAddress();
            return;

        }

        if (macAddressRNF != null && !macAddressRNF.trim().isEmpty()) {
            isReplaceNode = "Yes";
            loadExistingMACAddress();
            return;
        }

    }

    public void checkCouldNotComplete(){
        if(installStatus!=null && !installStatus.equals(Constants.COULD_NOT_COMPLETE)){
            skippedFixtureReason="";
        }
    }
    public void checkResolved(){

    }

    public boolean hasVal() {
        if (macAddress != null && !macAddress.trim().isEmpty()) {
            return true;
        }
        if (macAddressRN != null && !macAddressRN.trim().isEmpty()) {
            return true;
        }
        if (macAddressRNF != null && !macAddressRNF.trim().isEmpty()) {
            return true;
        }
        if (fixtureQRScan != null && !fixtureQRScan.trim().isEmpty()) {
            return true;
        }
        if (fixtureQRScanRF != null && !fixtureQRScanRF.trim().isEmpty()) {
            return true;
        }
        if (fixtureQRScanRNF != null && !fixtureQRScanRNF.trim().isEmpty()) {
            return true;
        }
        if (resolvedIssue != null && !resolvedIssue.trim().isEmpty()) {
            return true;
        }
        if (resolvedComment != null && !resolvedComment.trim().isEmpty()) {
            return true;
        }
        if (existingMacIfWrong != null && !existingMacIfWrong.trim().isEmpty()) {
            return true;
        }
        if (unableToRepairIssue != null && !unableToRepairIssue.trim().isEmpty()) {
            return true;
        }
        if (installStatus != null && !installStatus.trim().isEmpty()) {
            return true;
        }
        if (skippedFixtureReason != null && !skippedFixtureReason.trim().isEmpty()) {
            return true;
        }
        if (skippedReason != null && !skippedReason.trim().isEmpty()) {
            return true;
        }
        if (unableToRepairComment != null && !unableToRepairComment.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public String getUnableToRepairComment() {
        return unableToRepairComment;
    }

    public void setUnableToRepairComment(String unableToRepairComment) {
        this.unableToRepairComment = unableToRepairComment;
    }

    public String getIsReplaceNode() {
        return isReplaceNode;
    }

    public void setIsReplaceNode(String isReplaceNode) {
        this.isReplaceNode = isReplaceNode;
    }

    public String getNewFixtureQRScan() {
        if (fixtureQRScanRF != null && !fixtureQRScanRF.isEmpty() && !fixtureQRScanRF.contains("null")) {
            return fixtureQRScanRF;
        } else if (fixtureQRScanRNF != null && !fixtureQRScanRNF.isEmpty() && !fixtureQRScanRNF.contains("null")) {
            return fixtureQRScanRNF;
        }
        return "";
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
                Objects.equals(removalReason, that.removalReason) &&
                Objects.equals(fixtureQRScanRF, that.fixtureQRScanRF) &&
                Objects.equals(resolvedIssue, that.resolvedIssue) &&
                Objects.equals(resolvedComment, that.resolvedComment) &&
                Objects.equals(existingMacIfWrong, that.existingMacIfWrong) &&
                Objects.equals(unableToRepairIssue, that.unableToRepairIssue) &&
                Objects.equals(installStatus, that.installStatus) &&
                Objects.equals(skippedFixtureReason, that.skippedFixtureReason) &&
                Objects.equals(skippedReason, that.skippedReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress, macAddressRNF, macAddressRN, fixtureQRScan, fixtureQRScanRNF, fixtureQRScanRF, removalReason, resolvedIssue, resolvedComment, existingMacIfWrong, unableToRepairIssue, installStatus);
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
                ", removalReason='" + removalReason + '\'' +
                ", resolvedIssue='" + resolvedIssue + '\'' +
                ", resolvedComment='" + resolvedComment + '\'' +
                ", existingMacIfWrong='" + existingMacIfWrong + '\'' +
                ", unableToRepairIssue='" + unableToRepairIssue + '\'' +
                ", installStatus='" + installStatus + '\'' +
                '}';
    }
}
