package com.terragoedge.slv2edge.metering.model;

public class MeteringEntity {
    private int id;
    private String meteredPower;
    private String lampVoltage;
    private String dimmingLevel;
    private String idOnController;
    private String errorStatus;
    private long createDateTime;


    public long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(long createDateTime) {
        this.createDateTime = createDateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMeteredPower() {
        return meteredPower;
    }

    public void setMeteredPower(String meteredPower) {
        this.meteredPower = meteredPower;
    }

    public String getLampVoltage() {
        return lampVoltage;
    }

    public void setLampVoltage(String lampVoltage) {
        this.lampVoltage = lampVoltage;
    }

    public String getDimmingLevel() {
        return dimmingLevel;
    }

    public void setDimmingLevel(String dimmingLevel) {
        this.dimmingLevel = dimmingLevel;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String errorStatus) {
        this.errorStatus = errorStatus;
    }

    @Override
    public String toString() {
        return "MeteringEntity{" +
                "id=" + id +
                ", meteredPower='" + meteredPower + '\'' +
                ", lampVoltage='" + lampVoltage + '\'' +
                ", dimmingLevel='" + dimmingLevel + '\'' +
                ", idOnController='" + idOnController + '\'' +
                ", errorStatus='" + errorStatus + '\'' +
                '}';
    }
}
