package com.slvinterface.json;

import com.slvinterface.enumeration.SourceObjectType;

import java.util.Objects;

public class Mapping {
    private String sourceComponent;
    private String sourceObject;
    private String destinationComponent;
    private String defaultValue;
    private Concat concat;
    private boolean isValueParam;
    private SourceObjectType sourceType;
    private DateConfig dateConfig;

    public DateConfig getDateConfig() {
        return dateConfig;
    }

    public void setDateConfig(DateConfig dateConfig) {
        this.dateConfig = dateConfig;
    }

    public boolean isValueParam() {
        return isValueParam;
    }

    public void setValueParam(boolean valueParam) {
        isValueParam = valueParam;
    }

    public SourceObjectType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceObjectType sourceType) {
        this.sourceType = sourceType;
    }

    public Concat getConcat() {
        return concat;
    }

    public void setConcat(Concat concat) {
        this.concat = concat;
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
                ", concat=" + concat +
                ", isValueParam=" + isValueParam +
                ", sourceType=" + sourceType +
                ", dateConfig=" + dateConfig +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mapping mapping = (Mapping) o;
        return Objects.equals(destinationComponent, mapping.destinationComponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationComponent);
    }
}
