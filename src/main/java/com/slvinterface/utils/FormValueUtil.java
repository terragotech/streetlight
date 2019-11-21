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

    public static String getValue(List<FormValues> formComponents, int id)
    {
        String result = "";
        if(id != -1)
        {
            FormValues cur = new FormValues();
            cur.setId(id);
            int pos = formComponents.indexOf(cur);
            if (pos != -1) {
                FormValues tmp1 = formComponents.get(pos);
                result = checkNullValues(tmp1.getValue());
            }
        }
        return result;
    }

    public static String checkNullValues(String input)
    {
        String result = "";
        if(input == null)
        {
            result = "";
        }
        else if(input.equals("(null)"))
        {
            result = "";
        }
        else
        {
            result = input;
        }
        return result;
    }
}

