package com.terragoedge.slvinterface.kingcity;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

public class DefaultData {
    @CsvBindByName(column = "IdOnController")
    private String idOnController;
    @CsvBindByName(column = "Wattage")
    private String wattage;
    @CsvBindByName(column = "DimmingGroupValue")
    private String dimmingGroupValue;

    @CsvBindByName(column = "GeoZoneValue")
    private String geoZoneValue;

    public String getGeoZoneValue() {
        return geoZoneValue;
    }

    public void setGeoZoneValue(String geoZoneValue) {
        this.geoZoneValue = geoZoneValue;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getWattage() {
        return wattage;
    }

    public void setWattage(String wattage) {
        this.wattage = wattage;
    }

    public String getDimmingGroupValue() {
        return dimmingGroupValue;
    }

    public void setDimmingGroupValue(String dimmingGroupValue) {
        this.dimmingGroupValue = dimmingGroupValue;
    }

    @Override
    public String toString() {
        return "DefaultData{" +
                "idOnController='" + idOnController + '\'' +
                ", wattage='" + wattage + '\'' +
                ", dimmingGroupValue='" + dimmingGroupValue + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultData that = (DefaultData) o;
        return Objects.equals(idOnController, that.idOnController);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOnController);
    }
}
