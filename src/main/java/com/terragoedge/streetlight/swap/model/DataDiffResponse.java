package com.terragoedge.streetlight.swap.model;

import java.util.ArrayList;
import java.util.List;

public class DataDiffResponse {
    private int statusCode;
    private String message;
    private boolean isChanged;
    private List<DataDiffValueHolder> dataDiff = new ArrayList<>();

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public List<DataDiffValueHolder> getDataDiff() {
        return dataDiff;
    }

    public void setDataDiff(List<DataDiffValueHolder> dataDiff) {
        this.dataDiff = dataDiff;
    }

    @Override
    public String toString() {
        return "DataDiffResponse{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", isChanged=" + isChanged +
                ", dataDiff=" + dataDiff +
                '}';
    }
}
