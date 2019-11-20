package com.slvinterface.json;

import com.slvinterface.enumeration.Conditions;

public class DeviceCreation {

    private int id;
    private Conditions conditions;
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DeviceCreation{" +
                "id=" + id +
                ", conditions=" + conditions +
                ", value='" + value + '\'' +
                '}';
    }
}
