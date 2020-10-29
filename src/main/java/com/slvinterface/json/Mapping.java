package com.slvinterface.json;

public class Mapping {
    private String sourceComponent;
    private String sourceObject;
    private String destinationComponent;
    private String defaultValue;

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public String getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(String sourceObject) {
        this.sourceObject = sourceObject;
    }

    public String getDestinationComponent() {
        return destinationComponent;
    }

    public void setDestinationComponent(String destinationComponent) {
        this.destinationComponent = destinationComponent;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "sourceComponent='" + sourceComponent + '\'' +
                ", sourceObject='" + sourceObject + '\'' +
                ", destinationComponent='" + destinationComponent + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
