package com.terrago.jsoncsvconvertor;

import java.util.ArrayList;
import java.util.List;

public class EdgeForm {

    private String noteGuid;
    private List<Data> dataList = new ArrayList<>();

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "EdgeForm{" +
                "noteGuid='" + noteGuid + '\'' +
                ", dataList=" + dataList +
                '}';
    }
}
