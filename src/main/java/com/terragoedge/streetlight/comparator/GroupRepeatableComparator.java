package com.terragoedge.streetlight.comparator;

import com.terragoedge.edgeserver.EdgeFormData;

import java.util.Comparator;

public class GroupRepeatableComparator implements Comparator<EdgeFormData> {
    boolean isDecendingOrder;

    public GroupRepeatableComparator(boolean isDecendingOrder) {
        this.isDecendingOrder = isDecendingOrder;
    }

    public int compare(EdgeFormData edgeFormDataFirst, EdgeFormData edgeFormDataSecond) {
        if (isDecendingOrder) {
            if((edgeFormDataFirst.isGroup() && edgeFormDataSecond.isGroup()) && edgeFormDataFirst.getGroupId() == edgeFormDataSecond.getGroupId() ){
                return comparing(edgeFormDataFirst,edgeFormDataSecond);
            }else{
                return 0;
            }
        } else {
            return comparing(edgeFormDataFirst,edgeFormDataSecond);
        }
    }
    private int comparing(EdgeFormData edgeFormDataFirst, EdgeFormData edgeFormDataSecond){
        if (edgeFormDataFirst.getGroupRepeatableCount() == edgeFormDataSecond.getGroupRepeatableCount())
            return 0;
        else if (edgeFormDataFirst.getGroupRepeatableCount() > edgeFormDataSecond.getGroupRepeatableCount())
            return 1;
        else
            return -1;
    }
}