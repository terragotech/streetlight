package com.terragoedge.slvinterface.model;

public class InstallWorkflowFormId {
    private int feederName;
    private int streetName;
    private int parrish;
    private int newPoleNumber;
    private int retrofitStatus;
    private int newLampWatage;
    private int oldPoleNumber;
    private int poleType;
    private int macAddress;
    private int mastArmLength;
    private int mastArmLengthOther;
    private int conversionDate;
    private int feed;
    private int poleShape;
    private int condition;
    private int poleNumber;

    public int getFeederName() {
        return feederName;
    }

    public void setFeederName(int feederName) {
        this.feederName = feederName;
    }

    public int getStreetName() {
        return streetName;
    }

    public void setStreetName(int streetName) {
        this.streetName = streetName;
    }

    public int getParrish() {
        return parrish;
    }

    public void setParrish(int parrish) {
        this.parrish = parrish;
    }

    public int getNewPoleNumber() {
        return newPoleNumber;
    }

    public void setNewPoleNumber(int newPoleNumber) {
        this.newPoleNumber = newPoleNumber;
    }

    public int getRetrofitStatus() {
        return retrofitStatus;
    }

    public void setRetrofitStatus(int retrofitStatus) {
        this.retrofitStatus = retrofitStatus;
    }

    public int getNewLampWatage() {
        return newLampWatage;
    }

    public void setNewLampWatage(int newLampWatage) {
        this.newLampWatage = newLampWatage;
    }

    public int getOldPoleNumber() {
        return oldPoleNumber;
    }

    public void setOldPoleNumber(int oldPoleNumber) {
        this.oldPoleNumber = oldPoleNumber;
    }

    public int getPoleType() {
        return poleType;
    }

    public void setPoleType(int poleType) {
        this.poleType = poleType;
    }

    public int getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(int macAddress) {
        this.macAddress = macAddress;
    }

    public int getMastArmLength() {
        return mastArmLength;
    }

    public void setMastArmLength(int mastArmLength) {
        this.mastArmLength = mastArmLength;
    }

    public int getMastArmLengthOther() {
        return mastArmLengthOther;
    }

    public void setMastArmLengthOther(int mastArmLengthOther) {
        this.mastArmLengthOther = mastArmLengthOther;
    }

    public int getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(int conversionDate) {
        this.conversionDate = conversionDate;
    }

    public int getFeed() {
        return feed;
    }

    public void setFeed(int feed) {
        this.feed = feed;
    }

    public int getPoleShape() {
        return poleShape;
    }

    public void setPoleShape(int poleShape) {
        this.poleShape = poleShape;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public int getPoleNumber() {
        return poleNumber;
    }

    public void setPoleNumber(int poleNumber) {
        this.poleNumber = poleNumber;
    }

    @Override
    public String toString() {
        return "InstallWorkflowFormId{" +
                "feederName=" + feederName +
                ", streetName=" + streetName +
                ", parrish=" + parrish +
                ", newPoleNumber=" + newPoleNumber +
                ", retrofitStatus=" + retrofitStatus +
                ", newLampWatage=" + newLampWatage +
                ", oldPoleNumber=" + oldPoleNumber +
                ", poleType=" + poleType +
                ", macAddress=" + macAddress +
                ", mastArmLength=" + mastArmLength +
                ", mastArmLengthOther=" + mastArmLengthOther +
                ", conversionDate=" + conversionDate +
                ", feed=" + feed +
                ", poleShape=" + poleShape +
                ", condition=" + condition +
                ", poleNumber=" + poleNumber +
                '}';
    }
}
