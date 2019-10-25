package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

public class Edge2SLVData {

    private String title;
    private String idOnController;
    private String controllerStrId;
    private String calendar;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getControllerStrId() {
        return controllerStrId;
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    @Override
    public String toString() {
        return "Edge2SLVData{" +
                "title='" + title + '\'' +
                ", idOnController='" + idOnController + '\'' +
                ", controllerStrId='" + controllerStrId + '\'' +
                ", calendar='" + calendar + '\'' +
                '}';
    }
}
