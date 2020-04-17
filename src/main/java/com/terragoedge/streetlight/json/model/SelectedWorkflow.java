package com.terragoedge.streetlight.json.model;

public class SelectedWorkflow {
    private String selectedAction;
    private String selectedSubAction;
    private String actualAction;
    private String actualSubaction;
    private String value;
    private int id;

    public String getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(String selectedAction) {
        this.selectedAction = selectedAction;
    }

    public String getSelectedSubAction() {
        return selectedSubAction;
    }

    public void setSelectedSubAction(String selectedSubAction) {
        this.selectedSubAction = selectedSubAction;
    }

    public String getActualAction() {
        return actualAction;
    }

    public void setActualAction(String actualAction) {
        this.actualAction = actualAction;
    }

    public String getActualSubaction() {
        return actualSubaction;
    }

    public void setActualSubaction(String actualSubaction) {
        this.actualSubaction = actualSubaction;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SelectedWorkflow{" +
                "selectedAction='" + selectedAction + '\'' +
                ", selectedSubAction='" + selectedSubAction + '\'' +
                ", actualAction='" + actualAction + '\'' +
                ", actualSubaction='" + actualSubaction + '\'' +
                ", value='" + value + '\'' +
                ", id=" + id +
                '}';
    }
}
