package com.slvinterface.json;

public class Condition {
    private String sourceComponent;
    private String sourceObject;
    private String conditionComponent;
    private String conditionObject;
    private String defaultValue;
    private String conditionType;


    public String getConditionComponent() {
        return conditionComponent;
    }

    public void setConditionComponent(String conditionComponent) {
        this.conditionComponent = conditionComponent;
    }

    public String getConditionObject() {
        return conditionObject;
    }

    public void setConditionObject(String conditionObject) {
        this.conditionObject = conditionObject;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

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

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "sourceComponent='" + sourceComponent + '\'' +
                ", sourceObject='" + sourceObject + '\'' +
                ", conditionComponent='" + conditionComponent + '\'' +
                ", conditionObject='" + conditionObject + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", conditionType='" + conditionType + '\'' +
                '}';
    }
}
