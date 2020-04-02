package com.terragoedge.slvinterface.utils;





public class DataChecker{
    public static String checkForNull(String value){
        if(value != null)
        {
            return value;
        }
        return "";
    }
    public static String convertToString(int value){
        return Integer.toString(value);
    }
}

