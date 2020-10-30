package com.slvinterface.json;

public class Condition extends Mapping{
    private String conditionComponent;
    private String conditionObject;
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


    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "conditionComponent='" + conditionComponent + '\'' +
                ", conditionObject='" + conditionObject + '\'' +
                ", conditionType='" + conditionType + '\'' +
                '}';
    }
}
