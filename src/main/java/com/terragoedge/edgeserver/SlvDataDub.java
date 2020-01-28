package com.terragoedge.edgeserver;

import java.util.Objects;

public class SlvDataDub extends  SlvData {

    public SlvDataDub(String title,String noteGuid,String layerName){
        this.title = title;
        this.setGuid(noteGuid);
        this.setLayerName(layerName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlvData slvData = (SlvData) o;
        return Objects.equals(title, slvData.title);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title);
    }
}
