package com.terragoedge.streetlight.installmaintain.json;

public class CsvStatus {
    private boolean isWritten;
    private boolean isChangeParent;

    public boolean isWritten() {
        return isWritten;
    }

    public void setWritten(boolean written) {
        isWritten = written;
    }

    public boolean isChangeParent() {
        return isChangeParent;
    }

    public void setChangeParent(boolean changeParent) {
        isChangeParent = changeParent;
    }

    @Override
    public String toString() {
        return "CsvStatus{" +
                "isWritten=" + isWritten +
                ", isChangeParent=" + isChangeParent +
                '}';
    }
}
