package com.terragoedge.streetlight.json.model;


public class SlvServerDataColumnPos {


    private int idOnController;
    private int macAddress;
    private int luminairePartNumber;
    private int luminaireModel;
    private int luminaireManufacturedate;
    private int luminaireColorTemp;
    private int lumenOutput;
    private int distributionType;
    private int colorCode;
    private int driverManufacturer;
    private int driverPartNumber;
    private int dimmingType;

    private int serialNumber;

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(int idOnController) {
        this.idOnController = idOnController;
    }

    public int getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(int macAddress) {
        this.macAddress = macAddress;
    }

    public int getLuminairePartNumber() {
        return luminairePartNumber;
    }

    public void setLuminairePartNumber(int luminairePartNumber) {
        this.luminairePartNumber = luminairePartNumber;
    }

    public int getLuminaireModel() {
        return luminaireModel;
    }

    public void setLuminaireModel(int luminaireModel) {
        this.luminaireModel = luminaireModel;
    }

    public int getLuminaireManufacturedate() {
        return luminaireManufacturedate;
    }

    public void setLuminaireManufacturedate(int luminaireManufacturedate) {
        this.luminaireManufacturedate = luminaireManufacturedate;
    }

    public int getLuminaireColorTemp() {
        return luminaireColorTemp;
    }

    public void setLuminaireColorTemp(int luminaireColorTemp) {
        this.luminaireColorTemp = luminaireColorTemp;
    }

    public int getLumenOutput() {
        return lumenOutput;
    }

    public void setLumenOutput(int lumenOutput) {
        this.lumenOutput = lumenOutput;
    }

    public int getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(int distributionType) {
        this.distributionType = distributionType;
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }

    public int getDriverManufacturer() {
        return driverManufacturer;
    }

    public void setDriverManufacturer(int driverManufacturer) {
        this.driverManufacturer = driverManufacturer;
    }

    public int getDriverPartNumber() {
        return driverPartNumber;
    }

    public void setDriverPartNumber(int driverPartNumber) {
        this.driverPartNumber = driverPartNumber;
    }

    public int getDimmingType() {
        return dimmingType;
    }

    public void setDimmingType(int dimmingType) {
        this.dimmingType = dimmingType;
    }

    @Override
    public String toString() {
        return "SlvServerDataColumnPos{" +
                "idOnController=" + idOnController +
                ", macAddress=" + macAddress +
                ", luminairePartNumber=" + luminairePartNumber +
                ", luminaireModel=" + luminaireModel +
                ", luminaireManufacturedate=" + luminaireManufacturedate +
                ", luminaireColorTemp=" + luminaireColorTemp +
                ", lumenOutput=" + lumenOutput +
                ", distributionType=" + distributionType +
                ", colorCode=" + colorCode +
                ", driverManufacturer=" + driverManufacturer +
                ", driverPartNumber=" + driverPartNumber +
                ", dimmingType=" + dimmingType +
                '}';
    }
}
