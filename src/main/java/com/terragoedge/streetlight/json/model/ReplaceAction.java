package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class ReplaceAction {
    private int repairAndOutages;
    private List<Integer> RNFIds = new ArrayList<>();
    private List<Integer> RNIds = new ArrayList<>();
    private List<Integer> RFIds = new ArrayList<>();
    private List<Integer> resolvedIds = new ArrayList<>();
    private List<Integer> unableToRepairIds = new ArrayList<>();
    private List<Integer> CIMCONIds = new ArrayList<>();

    public int getRepairAndOutages() {
        return repairAndOutages;
    }

    public void setRepairAndOutages(int repairAndOutages) {
        this.repairAndOutages = repairAndOutages;
    }

    public List<Integer> getRNFIds() {
        return RNFIds;
    }

    public void setRNFIds(List<Integer> RNFIds) {
        this.RNFIds = RNFIds;
    }

    public List<Integer> getRNIds() {
        return RNIds;
    }

    public void setRNIds(List<Integer> RNIds) {
        this.RNIds = RNIds;
    }

    public List<Integer> getRFIds() {
        return RFIds;
    }

    public void setRFIds(List<Integer> RFIds) {
        this.RFIds = RFIds;
    }

    public List<Integer> getResolvedIds() {
        return resolvedIds;
    }

    public void setResolvedIds(List<Integer> resolvedIds) {
        this.resolvedIds = resolvedIds;
    }

    public List<Integer> getUnableToRepairIds() {
        return unableToRepairIds;
    }

    public void setUnableToRepairIds(List<Integer> unableToRepairIds) {
        this.unableToRepairIds = unableToRepairIds;
    }

    public List<Integer> getCIMCONIds() {
        return CIMCONIds;
    }

    public void setCIMCONIds(List<Integer> CIMCONIds) {
        this.CIMCONIds = CIMCONIds;
    }

    @Override
    public String toString() {
        return "ReplaceAction{" +
                "repairAndOutages=" + repairAndOutages +
                ", RNFIds=" + RNFIds +
                ", RNIds=" + RNIds +
                ", RFIds=" + RFIds +
                ", resolvedIds=" + resolvedIds +
                ", unableToRepairIds=" + unableToRepairIds +
                ", CIMCONIds=" + CIMCONIds +
                '}';
    }
}
