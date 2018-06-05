package com.terragoedge.edgeserver;

public class EdgeNotebook {

    private String notebookName = null;
    private String notebookGuid = null;

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public String getNotebookGuid() {
        return notebookGuid;
    }

    public void setNotebookGuid(String notebookGuid) {
        this.notebookGuid = notebookGuid;
    }

    @Override
    public String toString() {
        return "EdgeNotebook{" +
                "notebookName='" + notebookName + '\'' +
                ", notebookGuid='" + notebookGuid + '\'' +
                '}';
    }
}
