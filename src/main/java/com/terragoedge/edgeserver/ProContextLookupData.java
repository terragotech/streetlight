package com.terragoedge.edgeserver;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "pro_context_lookup")
public class ProContextLookupData {

    public static String LUM_BRAND = "lum_brand";
    public static String LUM_MODEL = "lum_model";
    public static String LUM_PART_NUM = "lum_part_no";
    public static String LUM_WATTAGE = "lum_wattage";

    @DatabaseField(columnName = "proposed_context")
    private String proposedContext;
    @DatabaseField(columnName = "lum_brand")
    private String lumBrand;
    @DatabaseField(columnName = "lum_model")
    private String lumModel;
    @DatabaseField(columnName = "lum_part_no")
    private String lumPartNumber;
    @DatabaseField(columnName = "lum_wattage")
    private String lumWattage;

    public String getProposedContext() {
        return proposedContext;
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public String getLumBrand() {
        return lumBrand;
    }

    public void setLumBrand(String lumBrand) {
        this.lumBrand = lumBrand;
    }

    public String getLumModel() {
        return lumModel;
    }

    public void setLumModel(String lumModel) {
        this.lumModel = lumModel;
    }

    public String getLumPartNumber() {
        return lumPartNumber;
    }

    public void setLumPartNumber(String lumPartNumber) {
        this.lumPartNumber = lumPartNumber;
    }

    public String getLumWattage() {
        return lumWattage;
    }

    public void setLumWattage(String lumWattage) {
        this.lumWattage = lumWattage;
    }

    @Override
    public String toString() {
        return "ProContextLookupData{" +
                "proposedContext='" + proposedContext + '\'' +
                ", lumBrand='" + lumBrand + '\'' +
                ", lumModel='" + lumModel + '\'' +
                ", lumPartNumber='" + lumPartNumber + '\'' +
                ", lumWattage='" + lumWattage + '\'' +
                '}';
    }
}
