package com.slvinterface.json;

import com.slvinterface.enumeration.FilterType;

public class Filter {

    private String sourceObject;
    private FilterType filterType;
    private String sourceComponent;
    private String destinationComponent;
    private String objectKey;
    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(String sourceObject) {
        this.sourceObject = sourceObject;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public String getDestinationComponent() {
        return destinationComponent;
    }

    public void setDestinationComponent(String destinationComponent) {
        this.destinationComponent = destinationComponent;
    }


    @Override
    public String toString() {
        return "Filter{" +
                "sourceObject='" + sourceObject + '\'' +
                ", filterType=" + filterType +
                ", sourceComponent='" + sourceComponent + '\'' +
                ", destinationComponent='" + destinationComponent + '\'' +
                ", objectKey='" + objectKey + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
