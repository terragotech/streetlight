package com.terrago.streetlights.utils;

import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.json.TerragoData;
import com.terrago.streetlights.json.UbiData;
import com.terrago.streetlights.service.RESTService;
import com.terrago.streetlights.service.UbicquiaLightsInterface;
import com.terragoedge.edgeserver.EdgeFormData;

import java.util.ArrayList;
import java.util.List;

public class TerragoUtils {
    public static List<UbiData> filterNearbyLights(List<UbiData> lstUbiData,String groupID)
    {
        List<UbiData> result = new ArrayList<UbiData>();
        for(UbiData ubiDataCur:lstUbiData)
        {
            if(ubiDataCur.getGroupid().equals(groupID))
            {
                result.add(ubiDataCur);
            }
        }
        return result;
    }
    public static List<UbiData> getAllNearByLights(LastUpdated lastUpdated,String latitude,
                                                   String longitude,
                                                   String distanceInMeters){
        List<String> lstGUID = TerragoDAO.getNearByFixtures(latitude,longitude,distanceInMeters);
        //lstGUID.remove(currentLightNoteguid);
        List<UbiData> lstUbiData = new ArrayList<UbiData>();
        String formTempGUID = TerragoUtils.getPropertyValue("formtemplatetoprocess");
        for(String nguid:lstGUID)
        {
            String notesJson =  RESTService.getNoteDetails(nguid);
            TerragoData terragoData = new TerragoData(notesJson);
            if(terragoData.hasForm(formTempGUID))
            {
                String devui = "";

                List<EdgeFormData> lstFormComp = terragoData.getFormComponents(formTempGUID);
                String strid = TerragoUtils.getPropertyValue("ubicquia_replacedeveui");
                int id = TerragoUtils.getIdFromString(strid);
                String strFormCompValue = TerragoUtils.getEdgeFormValue(lstFormComp,id);
                if(!strFormCompValue.equals(""))
                {
                    devui = TerragoUtils.get_devui(strFormCompValue);
                }
                else
                {
                    strid = TerragoUtils.getPropertyValue("ubicquia_deveui");
                    id = TerragoUtils.getIdFromString(strid);
                    strFormCompValue = TerragoUtils.getEdgeFormValue(lstFormComp,id);
                    if(!strFormCompValue.equals(""))
                    {
                        devui = TerragoUtils.get_devui(strFormCompValue);
                    }
                }
                if(!devui.equals(""))
                {
                    UbicquiaLightsInterface.requestDynamicToken();
                    JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lastUpdated,devui);
                    if(jobj1 != null)
                    {
                        //Get id and Group id here
                        String ubiId = jobj1.get("id").getAsString();


                        String ubiGroupId = checkDataNull(jobj1,"groupId");
                        String ubiLat = checkDataNull(jobj1,"latitude");
                        String ubiLng = checkDataNull(jobj1,"longitude");
                        if(ubiGroupId == null)
                        {
                            ubiGroupId = "";
                        }
                        UbiData ubiData = new UbiData();

                        ubiData.setDevui(devui);
                        ubiData.setId(ubiId);
                        ubiData.setGroupid(ubiGroupId);
                        ubiData.setLatitude(ubiLat);
                        ubiData.setLongitude(ubiLng);
                        ubiData.setNoteguid(nguid);
                        lstUbiData.add(ubiData);


                    }
                }
            }
        }
        return  lstUbiData;
    }
    public static String getPropertyValue(String propertyName){
        String propertyValue = PropertiesReader.getProperties().getProperty(propertyName);
        return propertyValue;
    }
    public static String getOffsetValue(String offest)
    {
        String result = "";
        String []values = offest.split(" ");
        if(values.length > 0)
        {
            result = values[0];
        }
        else
        {
            result = offest;
        }
        return result;
    }
    public static String get_devui(String qrstring)
    {
        String result = "";
        String []values = qrstring.split(",");
        if(values.length > 0)
        {
            result = values[0];
        }
        else
        {
            result = qrstring;
        }
        return result;
    }
    public static String parseDevUI(String devui)
    {
        String result = "";
        int idx = -1;
        if(devui == null || devui.equals("") )
        {
            return result;
        }
        idx = devui.lastIndexOf(";");
        if(idx != - 1 && idx <= devui.length()-1)
        {
            result = devui.substring(idx+1,devui.length());
        }
        return  result;
    }
    public static String parseDevUIAll(String devui)
    {
        String result = "";
        int idx = -1;
        if(devui == null || devui.equals("") )
        {
            return result;
        }
        idx = devui.lastIndexOf(";");
        if(idx != - 1 && idx <= devui.length()-1)
        {
            result = devui.substring(idx+1,devui.length());
        }
        if(idx == -1)
        {
            result = devui;
        }
        return  result;
    }
    public static String getEdgeFormValue(List<EdgeFormData> formComponents, int id)
    {
        String result = "";
        if(id != -1)
        {
            EdgeFormData cur = new EdgeFormData();
            cur.setId(id);
            int pos = formComponents.indexOf(cur);
            if (pos != -1) {
                EdgeFormData tmp1 = formComponents.get(pos);
                result = tmp1.getValue();
                if (result == null) {
                    result = "";
                }
            }
        }
        else
        {
            //Throw Exception
        }
        return result;
    }
    public static int getIdFromString(String strValue)
    {
        int result = -1;
        try {
            result = Integer.parseInt(strValue);
        }
        catch (Exception e)
        {
            result = -1;
        }
        return result;
    }
    public static String checkDataNull(JsonObject jsonObject,String key)
    {
        if(!jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null)
        {
            return jsonObject.get(key).getAsString();
        }
        return "";
    }
}
