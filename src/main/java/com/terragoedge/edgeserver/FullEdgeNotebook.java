package com.terragoedge.edgeserver;

public class FullEdgeNotebook {

    private String createdby;
    private long lastupdatedtime;
    private String notebookdesc;
    private String notebookname;
    private int quicknoteformtemplateid;
    private String notenametype;
    private String customname;
    private boolean isdeleted;
    private boolean isincludedatetime;


    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public long getLastupdatedtime() {
        return lastupdatedtime;
    }

    public void setLastupdatedtime(long lastupdatedtime) {
        this.lastupdatedtime = lastupdatedtime;
    }

    public String getNotebookdesc() {
        return notebookdesc;
    }

    public void setNotebookdesc(String notebookdesc) {
        this.notebookdesc = notebookdesc;
    }

    public String getNotebookname() {
        return notebookname;
    }

    public void setNotebookname(String notebookname) {
        this.notebookname = notebookname;
    }

    public int getQuicknoteformtemplateid() {
        return quicknoteformtemplateid;
    }

    public void setQuicknoteformtemplateid(int quicknoteformtemplateid) {
        this.quicknoteformtemplateid = quicknoteformtemplateid;
    }

    public String getNotenametype() {
        return notenametype;
    }

    public void setNotenametype(String notenametype) {
        this.notenametype = notenametype;
    }

    public String getCustomname() {
        return customname;
    }

    public void setCustomname(String customname) {
        this.customname = customname;
    }

    public boolean isIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(boolean isdeleted) {
        this.isdeleted = isdeleted;
    }

    public boolean isIsincludedatetime() {
        return isincludedatetime;
    }

    public void setIsincludedatetime(boolean isincludedatetime) {
        this.isincludedatetime = isincludedatetime;
    }

    @Override
    public String toString() {
        return "FullEdgeNotebook{" +
                "createdby='" + createdby + '\'' +
                ", lastupdatedtime=" + lastupdatedtime +
                ", notebookdesc='" + notebookdesc + '\'' +
                ", notebookname='" + notebookname + '\'' +
                ", quicknoteformtemplateid=" + quicknoteformtemplateid +
                ", notenametype='" + notenametype + '\'' +
                ", customname='" + customname + '\'' +
                ", isdeleted=" + isdeleted +
                ", isincludedatetime=" + isincludedatetime +
                '}';
    }
}
