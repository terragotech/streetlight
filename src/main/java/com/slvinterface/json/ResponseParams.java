package com.slvinterface.json;

public class ResponseParams {
    private String sourceComponent;
    private String destinationComponent;

    public String getDestinationComponent() {
        return destinationComponent;
    }

    public void setDestinationComponent(String destinationComponent) {
        this.destinationComponent = destinationComponent;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    @Override
    public String toString() {
        return "ResponseParams{" +
                "sourceComponent='" + sourceComponent + '\'' +
                ", destinationComponent='" + destinationComponent + '\'' +
                '}';
    }
}
