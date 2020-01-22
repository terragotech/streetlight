package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class DataComparatorRes {
    private List unMatchedFormGuids = new ArrayList();
    private boolean isMatched;

    public List getUnMatchedFormGuids() {
        return unMatchedFormGuids;
    }

    public void setUnMatchedFormGuids(List unMatchedFormGuids) {
        this.unMatchedFormGuids = unMatchedFormGuids;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    @Override
    public String toString() {
        return "DataComparatorRes{" +
                "unMatchedFormGuids=" + unMatchedFormGuids +
                ", isMatched=" + isMatched +
                '}';
    }
}
