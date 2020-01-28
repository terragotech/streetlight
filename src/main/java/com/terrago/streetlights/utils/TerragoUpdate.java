package com.terrago.streetlights.utils;

import com.terragoedge.edgeserver.EdgeFormData;

import java.util.List;

public class TerragoUpdate {
    public static void updateEdgeForm(List<EdgeFormData> formComponents, int id, String value)
    {
        EdgeFormData cur = new EdgeFormData();
        cur.setId(id);
        int pos = formComponents.indexOf(cur);
        if(pos != -1)
        {
            EdgeFormData tmp1 = formComponents.get(pos);
            tmp1.setValue(tmp1.getLabel() + "#" + value);
        }
    }
}
