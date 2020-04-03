package com.terragoedge.slvinterface.maintenanceworkflow.model;

import java.util.List;

public class ActionTypeJson {
    private String actionType;
    private List<Integer> ids = null;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

}
