package com.terragoedge.streetlight.comparator;

import com.terragoedge.edgeserver.EdgeFormData;

import java.util.Comparator;

public class GroupRepeatableComparator implements Comparator<EdgeFormData> {
    public int compare(EdgeFormData edgeFormDataFirst, EdgeFormData edgeFormDataSecond) {
        if (edgeFormDataFirst.getGroupRepeatableCount() == edgeFormDataSecond.getGroupRepeatableCount())
            return 0;
        else if (edgeFormDataFirst.getGroupRepeatableCount() > edgeFormDataSecond.getGroupRepeatableCount())
            return 1;
        else
            return -1;
    }
}