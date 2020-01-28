package com.terrago.streetlights.utils;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.security.InvalidParameterException;

public class JsonDataParser {
    private static JsonParser jsonParser = null;
    public static String checkDataNull(JsonObject jsonObject,String key)
    {
        if(jsonObject.get(key) != null && !jsonObject.get(key).isJsonNull()  )
        {
            return jsonObject.get(key).getAsString();
        }
        return "";
    }
    private static void createParser(){
        if(jsonParser == null)
        {
            jsonParser = new JsonParser();
        }
    }
    public static JsonObject getJsonObject(String jsonString) throws Exception
    {
        JsonObject result = null;
        if(jsonString != null) {
            if (jsonParser == null)
            {
                createParser();
            }
            result =  jsonParser.parse(jsonString).getAsJsonObject();
        }
        else
        {
            throw new Exception("Invalid Parameters");
        }
        return result;
    }

}
