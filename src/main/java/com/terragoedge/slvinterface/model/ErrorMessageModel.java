package com.terragoedge.slvinterface.model;

public class ErrorMessageModel {
    private String label;
    private String properties;
    private String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ErrorMessageModel{" +
                "label='" + label + '\'' +
                ", properties='" + properties + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
