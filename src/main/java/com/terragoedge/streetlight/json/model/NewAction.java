package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class NewAction {
    private int installStatus;
    private List<Integer> completeIds = new ArrayList<>();
    private List<Integer> photocellIds = new ArrayList<>();
    private List<Integer> couldNotCompleteIds = new ArrayList<>();

    public int getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(int installStatus) {
        this.installStatus = installStatus;
    }

    public List<Integer> getCompleteIds() {
        return completeIds;
    }

    public void setCompleteIds(List<Integer> completeIds) {
        this.completeIds = completeIds;
    }

    public List<Integer> getPhotocellIds() {
        return photocellIds;
    }

    public void setPhotocellIds(List<Integer> photocellIds) {
        this.photocellIds = photocellIds;
    }

    public List<Integer> getCouldNotCompleteIds() {
        return couldNotCompleteIds;
    }

    public void setCouldNotCompleteIds(List<Integer> couldNotCompleteIds) {
        this.couldNotCompleteIds = couldNotCompleteIds;
    }

    @Override
    public String toString() {
        return "NewAction{" +
                "installStatus=" + installStatus +
                ", completeIds=" + completeIds +
                ", photocellIds=" + photocellIds +
                ", couldNotCompleteIds=" + couldNotCompleteIds +
                '}';
    }
}
