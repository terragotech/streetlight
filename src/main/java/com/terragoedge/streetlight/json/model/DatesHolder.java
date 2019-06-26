package com.terragoedge.streetlight.json.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DatesHolder {
    // Date in SLV
    private SLVDates slvDates = new SLVDates();
    // Date present in Edge Form
    private SLVDates edgeDates;

    // Date Needs to send SLV
    private SLVDates syncEdgeDates;

    private boolean isCslpNodeDateSynced;
    private boolean isCslpLumDateSynced;
    private boolean isInstallDateSynced;
    private boolean isLumInstallDateSynced;

    private long edgeNoteDateTime;
    private boolean hasEdgeDate;


    public SLVDates getSlvDates() {
        return slvDates;
    }

    public void setSlvDates(SLVDates slvDates) {
        this.slvDates = slvDates;
    }

    public SLVDates getEdgeDates() {
        return edgeDates;
    }

    public void setEdgeDates(SLVDates edgeDates) {
        this.edgeDates = edgeDates;
    }

    public SLVDates getSyncEdgeDates() {
        return syncEdgeDates;
    }

    public void setSyncEdgeDates(SLVDates syncEdgeDates) {
        this.syncEdgeDates = syncEdgeDates;
    }

    public boolean isCslpNodeDateSynced() {
        return isCslpNodeDateSynced;
    }

    public void setCslpNodeDateSynced(boolean cslpNodeDateSynced) {
        isCslpNodeDateSynced = cslpNodeDateSynced;
    }

    public boolean isCslpLumDateSynced() {
        return isCslpLumDateSynced;
    }

    public void setCslpLumDateSynced(boolean cslpLumDateSynced) {
        isCslpLumDateSynced = cslpLumDateSynced;
    }

    public boolean isInstallDateSynced() {
        return isInstallDateSynced;
    }

    public void setInstallDateSynced(boolean installDateSynced) {
        isInstallDateSynced = installDateSynced;
    }

    public boolean isLumInstallDateSynced() {
        return isLumInstallDateSynced;
    }

    public void setLumInstallDateSynced(boolean lumInstallDateSynced) {
        isLumInstallDateSynced = lumInstallDateSynced;
    }




    public long getEdgeNoteDateTime() {
        return edgeNoteDateTime;
    }

    public void setEdgeNoteDateTime(long edgeNoteDateTime) {
        this.edgeNoteDateTime = edgeNoteDateTime;
    }

    public String compareSLVEdgeDate(String edgeDate, String slvDate, boolean isCslp){
        if(isCslp){
            if(slvDate == null || slvDate.trim().isEmpty()){
                if(edgeDate != null && !edgeDate.trim().isEmpty()){
                    return edgeDate;
                }else{
                    return null;
                }
            }else{
                if(edgeDate != null && !edgeDate.trim().isEmpty()){
                    if(edgeDate.equals(slvDate)){
                        return null;
                    }
                    return edgeDate;
                }else{
                    return null;
                }
            }
        }else {
            if(edgeDate != null && !edgeDate.trim().isEmpty()){
                if(slvDate != null && !slvDate.trim().isEmpty()){
                    if(edgeDate.equals(slvDate)){
                        return null;
                    }else{
                        return edgeDate;
                    }
                }else{
                    return edgeDate;
                }
            }else{
                return null;
            }
        }

    }


    protected String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        String dff = dateFormat.format(date);
        return dff;
    }

    @Override
    public String toString() {
        return "DatesHolder{" +
                "slvDates=" + slvDates +
                ", edgeDates=" + edgeDates +
                ", syncEdgeDates=" + syncEdgeDates +
                ", isCslpNodeDateSynced=" + isCslpNodeDateSynced +
                ", isCslpLumDateSynced=" + isCslpLumDateSynced +
                ", isInstallDateSynced=" + isInstallDateSynced +
                ", isLumInstallDateSynced=" + isLumInstallDateSynced +
                '}';
    }
}
