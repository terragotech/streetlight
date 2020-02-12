package com.terrago.streetlights.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LatLongUtils {
    public static LatLong getLatLngFromGeoJson(String strGeoJson)
    {
        LatLong result = null;
        try {
            JsonObject jsonObject = JsonDataParser.getJsonObject(strGeoJson);
            JsonObject geomObj = jsonObject.get("geometry").getAsJsonObject();
            if(geomObj != null)
            {
                String strGeom = geomObj.get("type").getAsString();
                if(strGeom != null)
                {
                    if(strGeom.equals("Point"))
                    {
                        String strLatLong = "";
                         JsonArray jsonArray =  geomObj.get("coordinates").getAsJsonArray();
                         int idx = 0;
                         for(JsonElement jsonElement:jsonArray)
                         {
                             String str1 = jsonElement.getAsString();
                             if(str1 != null)
                             {
                                 str1 = str1.trim();
                                 if(idx == 0)
                                 {
                                     result = new LatLong();
                                     result.setLng(str1);
                                 }
                                 if(idx == 1)
                                 {
                                     result.setLat(str1);
                                 }
                                 idx = idx + 1;
                             }

                         }

                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
