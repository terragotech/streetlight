package com.terragoedge.slvinterface.model;

import java.util.ArrayList;
import java.util.List;

public class ExceptionLocation {
    private List<String> selectedLoc = new ArrayList<>();
    private List<String> destinationLoc = new ArrayList<>();

    public List<String> getSelectedLoc() {
        return selectedLoc;
    }

    public void setSelectedLoc(List<String> selectedLoc) {
        this.selectedLoc = selectedLoc;
    }

    public List<String> getDestinationLoc() {
        return destinationLoc;
    }

    public void setDestinationLoc(List<String> destinationLoc) {
        this.destinationLoc = destinationLoc;
    }

    @Override
    public String toString() {
        return "ExceptionLocation{" +
                "selectedLoc=" + selectedLoc +
                ", destinationLoc=" + destinationLoc +
                '}';
    }
}
