package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "lookup_data")
public class LookupEntity {

    public static final String LANTERN_TYPE= "lanterntype";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "partdescription")
    private String partDescription;
    @DatabaseField(columnName = "model")
    private String model;
    @DatabaseField(columnName = "lumenoutput")
    private String lumenOutput;
    @DatabaseField(columnName = "color")
    private String color;
    @DatabaseField(columnName = "lightsource")
    private String lightSource;
    @DatabaseField(columnName = "distributiontype")
    private String distributionType;
    @DatabaseField(columnName = "style")
    private String style;
    @DatabaseField(columnName = "elexonchargecode")
    private String elexonChargeCode;
    @DatabaseField(columnName = "power")
    private String power;
    @DatabaseField(columnName = "lanterntype")
    private String lanternType;

    public LookupEntity() {
    }

    public LookupEntity( String partDescription, String model, String lumenOutput, String color, String lightSource, String distributionType, String style, String elexonChargeCode, String power, String lanternType) {
        this.partDescription = partDescription;
        this.model = model;
        this.lumenOutput = lumenOutput;
        this.color = color;
        this.lightSource = lightSource;
        this.distributionType = distributionType;
        this.style = style;
        this.elexonChargeCode = elexonChargeCode;
        this.power = power;
        this.lanternType = lanternType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartDescription() {
        return partDescription;
    }

    public void setPartDescription(String partDescription) {
        this.partDescription = partDescription;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLumenOutput() {
        return lumenOutput;
    }

    public void setLumenOutput(String lumenOutput) {
        this.lumenOutput = lumenOutput;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLightSource() {
        return lightSource;
    }

    public void setLightSource(String lightSource) {
        this.lightSource = lightSource;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(String distributionType) {
        this.distributionType = distributionType;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getElexonChargeCode() {
        return elexonChargeCode;
    }

    public void setElexonChargeCode(String elexonChargeCode) {
        this.elexonChargeCode = elexonChargeCode;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getLanternType() {
        return lanternType;
    }

    public void setLanternType(String lanternType) {
        this.lanternType = lanternType;
    }

    @Override
    public String toString() {
        return "LookupEntity{" +
                "id=" + id +
                ", partDescription='" + partDescription + '\'' +
                ", model='" + model + '\'' +
                ", lumenOutput='" + lumenOutput + '\'' +
                ", color='" + color + '\'' +
                ", lightSource='" + lightSource + '\'' +
                ", distributionType='" + distributionType + '\'' +
                ", style='" + style + '\'' +
                ", elexonChargeCode='" + elexonChargeCode + '\'' +
                ", power='" + power + '\'' +
                ", lanternType='" + lanternType + '\'' +
                '}';
    }
}
