package com.slvinterface.utils;

import java.util.List;

public class DataOperations {
    public static String [] convertListToArray(List<String> lstData)
    {
        int tc = lstData.size();
        String []result = new String[tc];
        int idx = 0;
        for(String cur:lstData)
        {
            result[idx] = cur;
            idx = idx + 1;
        }
        return result;
    }
    public static int getIndex(String []fields,String fieldName)
    {
        int result = -1;
        int fc = fields.length;
        for(int idx=0;idx<fc;idx++)
        {
            if(fields[idx].equals(fieldName))
            {
                result = idx;
                break;
            }
        }
        return result;
    }
    public static String getValue(String []fields,String fieldName,String []rowData)
    {
        String result = "";
        int idx = getIndex(fields,fieldName);
        if(idx != -1)
        {
            result = rowData[idx];
            if(result == null)
            {
                result = "";
            }
        }
        return result;
    }
}
