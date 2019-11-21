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
    public static int convertFormIDToInt(String inputString){
        int result = 0;
        if(inputString != null)
        {
            try {
                result = Integer.parseInt(inputString);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = -1;
            }
        }
        else
        {
            result = -1;
        }
        return result;
    }
    public static String removeSemicolonAndInvalidCharacters(String inputString)
    {
        String result = "";
        if(inputString != null)
        {
            result = inputString.replaceAll(";",".");
        }
        return result;
    }
}
