package com.slvinterface.utils;

import com.slvinterface.json.FormValues;

import java.util.List;

public class FormValueUtil {
    public static void updateEdgeForm(List<FormValues> formComponents, int id, String value)
    {
        FormValues cur = new FormValues();
        cur.setId(id);
        int pos = formComponents.indexOf(cur);
        if(pos != -1)
        {
            FormValues tmp1 = formComponents.get(pos);
            tmp1.setValue(tmp1.getLabel() + "#" + value);
        }
    }
}
