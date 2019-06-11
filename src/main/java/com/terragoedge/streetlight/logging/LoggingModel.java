package com.terragoedge.streetlight.logging;

import com.terragoedge.streetlight.json.model.SLVDates;

import java.util.Objects;

public class LoggingModel {

    private String processedNoteId;
    private String status;
    private String errorDetails;
    private String createdDatetime;
    private String noteName;
    private String existingNodeMACaddress;
    private String newNodeMACaddress;
    private String isReplaceNode;
    private boolean isQuickNote;
    private String idOnController;
    private String macAddress;
    private boolean isNoteAlreadySynced;
    private String talqAddressnoteGuid;
    private String parentNoteId;
    private String slvMacaddress;

    private String controllerSrtId;

    private long talqCreatedTime;
    private String talqAddress;

    private long lastSyncTime;
    private String layerType;

    private long installedDate;
    private long replacedDate;

    private boolean isMacAddressUsed;
    private boolean isFixtureQRSame;
    private boolean isNigthRideSame;
    private boolean isButtonPhotoCell;
    private boolean isDroppedPinWorkflow;


    private boolean isCouldNotComplete;

    private String repairsOption;

    private String slvLuminaireSerialNumber;





    public boolean isCouldNotComplete() {
        return isCouldNotComplete;
    }

    public void setCouldNotComplete(boolean couldNotComplete) {
        isCouldNotComplete = couldNotComplete;
    }

    public String getSlvLuminaireSerialNumber() {
        return slvLuminaireSerialNumber;
    }



    public void setSlvLuminaireSerialNumber(String slvLuminaireSerialNumber) {
        int pos = slvLuminaireSerialNumber.lastIndexOf(":");
        if(pos != -1){
          String val =  slvLuminaireSerialNumber.substring(pos,slvLuminaireSerialNumber.length());
          if(!val.trim().isEmpty()){
              this.slvLuminaireSerialNumber = val.trim();
          }
        }else{
            this.slvLuminaireSerialNumber = slvLuminaireSerialNumber;
        }

    }


    public String getParentNoteId() {
        return parentNoteId;
    }

    public void setParentNoteId(String parentNoteId) {
        this.parentNoteId = parentNoteId;
    }

    public boolean isNigthRideSame() {
        return isNigthRideSame;
    }

    public void setNigthRideSame(boolean nigthRideSame) {
        isNigthRideSame = nigthRideSame;
    }

    public String getRepairsOption() {
        return repairsOption;
    }

    public void setRepairsOption(String repairsOption) {
        this.repairsOption = repairsOption;
    }

    public boolean isFixtureQRSame() {
        return isFixtureQRSame;
    }

    public void setFixtureQRSame(boolean fixtureQRSame) {
        isFixtureQRSame = fixtureQRSame;
    }

    public boolean isMacAddressUsed() {
        return isMacAddressUsed;
    }

    public void setMacAddressUsed(boolean macAddressUsed) {
        isMacAddressUsed = macAddressUsed;
    }

    public String getControllerSrtId() {
        return controllerSrtId;
    }

    public void setControllerSrtId(String controllerSrtId) {
        this.controllerSrtId = controllerSrtId;
    }

    public boolean isNoteAlreadySynced() {
        return isNoteAlreadySynced;
    }

    public void setNoteAlreadySynced(boolean flag) {
        this.isNoteAlreadySynced = flag;
    }

    public String getProcessedNoteId() {
        return processedNoteId;
    }

    public void setProcessedNoteId(String processedNoteId) {
        this.processedNoteId = processedNoteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(String createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getExistingNodeMACaddress() {
        return existingNodeMACaddress;
    }

    public void setExistingNodeMACaddress(String existingNodeMACaddress) {
        this.existingNodeMACaddress = existingNodeMACaddress;
    }

    public String getNewNodeMACaddress() {
        return newNodeMACaddress;
    }

    public void setNewNodeMACaddress(String newNodeMACaddress) {
        this.newNodeMACaddress = newNodeMACaddress;
    }

    public String getIsReplaceNode() {
        return isReplaceNode;
    }

    public void setIsReplaceNode(String isReplaceNode) {
        this.isReplaceNode = isReplaceNode;
    }

    public boolean getIsQuickNote() {
        return isQuickNote;
    }

    public void setIsQuickNote(boolean isQuickNote) {
        this.isQuickNote = isQuickNote;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isQuickNote() {
        return isQuickNote;
    }

    public void setQuickNote(boolean quickNote) {
        isQuickNote = quickNote;
    }

    public long getTalqCreatedTime() {
        return talqCreatedTime;
    }

    public void setTalqCreatedTime(long talqCreatedTime) {
        this.talqCreatedTime = talqCreatedTime;
    }

    public String getTalqAddress() {
        return talqAddress;
    }

    public void setTalqAddress(String talqAddress) {
        this.talqAddress = talqAddress;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getTalqAddressnoteGuid() {
        return talqAddressnoteGuid;
    }

    public void setTalqAddressnoteGuid(String talqAddressnoteGuid) {
        this.talqAddressnoteGuid = talqAddressnoteGuid;
    }

    public long getInstalledDate() {
        return installedDate;
    }

    public void setInstalledDate(long installedDate) {
        this.installedDate = installedDate;
    }

    public long getReplacedDate() {
        return replacedDate;
    }

    public void setReplacedDate(long replacedDate) {
        this.replacedDate = replacedDate;
    }

    public boolean isButtonPhotoCell() {
        return isButtonPhotoCell;
    }

    public void setButtonPhotoCell(boolean buttonPhotoCell) {
        isButtonPhotoCell = buttonPhotoCell;
    }

    public String getSlvMacaddress() {
        return slvMacaddress;
    }

    public void setSlvMacaddress(String slvMacaddress) {
        this.slvMacaddress = slvMacaddress;
    }


    public boolean isDroppedPinWorkflow() {
        return isDroppedPinWorkflow;
    }

    public void setDroppedPinWorkflow(boolean droppedPinWorkflow) {
        isDroppedPinWorkflow = droppedPinWorkflow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoggingModel that = (LoggingModel) o;
        return Objects.equals(processedNoteId, that.processedNoteId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(processedNoteId);
    }

    public String getLayerType() {
        return layerType;
    }

    public void setLayerType(String layerType) {
        this.layerType = layerType;
    }

    @Override
    public String toString() {
        return "LoggingModel{" +
                "processedNoteId='" + processedNoteId + '\'' +
                ", status='" + status + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", createdDatetime='" + createdDatetime + '\'' +
                ", noteName='" + noteName + '\'' +
                ", existingNodeMACaddress='" + existingNodeMACaddress + '\'' +
                ", newNodeMACaddress='" + newNodeMACaddress + '\'' +
                ", isReplaceNode='" + isReplaceNode + '\'' +
                ", isQuickNote=" + isQuickNote +
                ", idOnController='" + idOnController + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", isNoteAlreadySynced=" + isNoteAlreadySynced +
                ", talqAddressnoteGuid='" + talqAddressnoteGuid + '\'' +
                ", parentNoteId='" + parentNoteId + '\'' +
                ", slvMacaddress='" + slvMacaddress + '\'' +
                ", controllerSrtId='" + controllerSrtId + '\'' +
                ", talqCreatedTime=" + talqCreatedTime +
                ", talqAddress='" + talqAddress + '\'' +
                ", lastSyncTime=" + lastSyncTime +
                ", layerType='" + layerType + '\'' +
                ", installedDate=" + installedDate +
                ", replacedDate=" + replacedDate +
                ", isMacAddressUsed=" + isMacAddressUsed +
                ", isFixtureQRSame=" + isFixtureQRSame +
                ", isNigthRideSame=" + isNigthRideSame +
                ", isButtonPhotoCell=" + isButtonPhotoCell +
                ", isDroppedPinWorkflow=" + isDroppedPinWorkflow +
                ", isCouldNotComplete=" + isCouldNotComplete +
                ", repairsOption='" + repairsOption + '\'' +
                ", slvLuminaireSerialNumber='" + slvLuminaireSerialNumber + '\'' +
                '}';
    }
}
