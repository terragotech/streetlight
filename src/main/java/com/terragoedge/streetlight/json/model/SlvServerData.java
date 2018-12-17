package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.terragoedge.streetlight.enumeration.ProcessType;

import java.util.Objects;

@DatabaseTable(tableName = "slvserverdata")
public class SlvServerData {

    public static final String ID_ON_CONTROLLER = "idoncontroller";

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    //device.luminaire.partnumber
    @DatabaseField(columnName = "luminaire_part_number")
    private String luminairePartNumber;
    //luminaire.model
    @DatabaseField(columnName = "luminaire_model")
    private String luminaireModel;
    //device.luminaire.manufacturedate
    @DatabaseField(columnName = "luminaire_manufacture_date")
    private String luminaireManufacturedate;
    @DatabaseField(columnName = "luminaire_color_temp")
    private String luminaireColorTemp;
    @DatabaseField(columnName = "luminaire_output")
    private String lumenOutput;
    @DatabaseField(columnName = "distribution_type")
    private String distributionType;
    @DatabaseField(columnName = "color_code")
    private String colorCode;
    @DatabaseField(columnName = "driver_manufacturer")
    private String driverManufacturer;
    @DatabaseField(columnName = "driver_part_number")
    private String driverPartNumber;
    @DatabaseField(columnName = "dimming_type")
    private String dimmingType;


    @DatabaseField(columnName = "serial_number")
    private String  serialNumber;

    @DatabaseField(columnName = "createdatedtime")
    private long createDateTime;

    @DatabaseField(columnName = "lastupdatedattTime")
    private long lastUpdateDateTime;

    @DatabaseField(columnName = "process_type",dataType = DataType.ENUM_STRING)
    private ProcessType processType;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(ProcessType processType) {
        this.processType = processType;
    }

    public long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(long createDateTime) {
        this.createDateTime = createDateTime;
    }

    public long getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(long lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
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

    public String getLuminairePartNumber() {
        return luminairePartNumber;
    }

    public void setLuminairePartNumber(String luminairePartNumber) {
        this.luminairePartNumber = luminairePartNumber;
    }

    public String getLuminaireModel() {
        return luminaireModel;
    }

    public void setLuminaireModel(String luminaireModel) {
        this.luminaireModel = luminaireModel;
    }

    public String getLuminaireManufacturedate() {
        return luminaireManufacturedate;
    }

    public void setLuminaireManufacturedate(String luminaireManufacturedate) {
        this.luminaireManufacturedate = luminaireManufacturedate;
    }

    public String getLuminaireColorTemp() {
        return luminaireColorTemp;
    }

    public void setLuminaireColorTemp(String luminaireColorTemp) {
        this.luminaireColorTemp = luminaireColorTemp;
    }

    public String getLumenOutput() {
        return lumenOutput;
    }

    public void setLumenOutput(String lumenOutput) {
        this.lumenOutput = lumenOutput;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(String distributionType) {
        this.distributionType = distributionType;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getDriverManufacturer() {
        return driverManufacturer;
    }

    public void setDriverManufacturer(String driverManufacturer) {
        this.driverManufacturer = driverManufacturer;
    }

    public String getDriverPartNumber() {
        return driverPartNumber;
    }

    public void setDriverPartNumber(String driverPartNumber) {
        this.driverPartNumber = driverPartNumber;
    }

    public String getDimmingType() {
        return dimmingType;
    }

    public void setDimmingType(String dimmingType) {
        this.dimmingType = dimmingType;
    }

    @Override
    public String toString() {
        return "SlvServerData{" +
                "idOnController='" + idOnController + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", luminairePartNumber='" + luminairePartNumber + '\'' +
                ", luminaireModel='" + luminaireModel + '\'' +
                ", luminaireManufacturedate='" + luminaireManufacturedate + '\'' +
                ", luminaireColorTemp='" + luminaireColorTemp + '\'' +
                ", lumenOutput='" + lumenOutput + '\'' +
                ", distributionType='" + distributionType + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", driverManufacturer='" + driverManufacturer + '\'' +
                ", driverPartNumber='" + driverPartNumber + '\'' +
                ", dimmingType='" + dimmingType + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlvServerData that = (SlvServerData) o;
        return idOnController.equals(that.idOnController) &&
                macAddress.equals(that.macAddress) &&
                luminairePartNumber.equals(that.luminairePartNumber) &&
                luminaireModel.equals(that.luminaireModel) &&
                luminaireManufacturedate.equals(that.luminaireManufacturedate) &&
                luminaireColorTemp.equals(that.luminaireColorTemp) &&
                lumenOutput.equals(that.lumenOutput) &&
                distributionType.equals(that.distributionType) &&
                colorCode.equals(that.colorCode) &&
                driverManufacturer.equals(that.driverManufacturer) &&
                driverPartNumber.equals(that.driverPartNumber) &&
                dimmingType.equals(that.dimmingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOnController, macAddress, luminairePartNumber, luminaireModel, luminaireManufacturedate, luminaireColorTemp, lumenOutput, distributionType, colorCode, driverManufacturer, driverPartNumber, dimmingType);
    }


    public boolean isValPresent(){
      return   this.macAddress != null || (this.luminaireModel != null && this.luminairePartNumber != null);
    }


}
