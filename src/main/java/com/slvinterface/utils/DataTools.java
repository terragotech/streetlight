package com.slvinterface.utils;

import java.net.URLEncoder;

public class DataTools {
    public static boolean checkForValidMacAddress(String macAddress){
        if(macAddress != null)
        {
            if(macAddress.equals("") || macAddress.equals("null") || macAddress.equals("(null)") || macAddress.equals("undefined") || macAddress.equals("NaN"))
            {
                return false;
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    public static String URLEncoder(String value)throws Exception{
        return   URLEncoder.encode(value, "UTF-8");
    }
}
