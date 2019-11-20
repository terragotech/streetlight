package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

public class Priority {

    private SLVProcess type;
    private  int order;

    public SLVProcess getType() {
        return type;
    }

    public void setType(SLVProcess type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "Priority{" +
                "type=" + type +
                ", order=" + order +
                '}';
    }
}
