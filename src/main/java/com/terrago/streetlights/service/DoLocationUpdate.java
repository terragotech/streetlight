package com.terrago.streetlights.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.UbiTransactionLog;
import com.terrago.streetlights.utils.*;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.json.model.Dictionary;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoLocationUpdate {
    String noteguid;
    LastUpdated lastUpdated;
    public String getNoteguid() {
        return noteguid;
    }
    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }
    public DoLocationUpdate(LastUpdated lastUpdated){
        this.lastUpdated = lastUpdated;
    }
    private void updateLocationData(JsonObject job1, String longitude, String latitude)
    {
        String geomStr = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[";
        geomStr =  geomStr + longitude + "," + latitude;
        geomStr = geomStr + "]}}";
        job1.addProperty("geometry",geomStr);
        //edgeNote.setGeometry(geomStr);
    }
    private String checkDataNull(JsonObject jsonObject,String key)
    {
        if(!jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null)
        {
            return jsonObject.get(key).getAsString();
        }
        return "";
    }
    private LatLong2 getLatLong(JsonObject jsonObject)
    {
        String strLat = checkDataNull(jsonObject,"latitude");

        String strLng = checkDataNull(jsonObject,"longitude");
        if(strLat.equals("") || strLng.equals(""))
        {
            return  null;
        }
        LatLong2 latLong2 = new LatLong2();
        latLong2.setLat(Double.parseDouble(strLat));
        latLong2.setLng(Double.parseDouble(strLng));
        return latLong2;

    }
    private String getValue(List<EdgeFormData> formComponents,String propertyName)
    {
        int idAction = Integer.parseInt(PropertiesReader.getProperties().getProperty(propertyName));
        String actionString = "";
        EdgeFormData actionInstall = new EdgeFormData();
        actionInstall.setId(idAction);
        int pos = formComponents.indexOf(actionInstall);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            actionString = tmp1.getValue();
        }
        return actionString;
    }
    private String get_devui(String qrstring)
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

    public void processLocationChange(UbiTransactionLog ubiTransactionLog){
        System.out.println("Entering processLocationChange");
        //long lastMaxUpdatedTime = 0;
        String completeLayer = PropertiesReader.getProperties().getProperty("ubicquia_notelayer");
        String installAction = PropertiesReader.getProperties().getProperty("ubicquia_actioninstall");
        String replaceAction = PropertiesReader.getProperties().getProperty("ubicquia_actionmaintain");
        String ignoreUser = PropertiesReader.getProperties().getProperty("ignoreuser");
        String distanceTh = PropertiesReader.getProperties().getProperty("ubicquia_distance");
        double distth = Double.parseDouble(distanceTh);


                boolean mustUpdate = false;
                //String noteGUID = TerragoDAO.getNoteGUID(cur);
                String notesJson =  RESTService.getNoteDetails(noteguid);
                Type listType = new TypeToken<ArrayList<EdgeNote>>() {
                }.getType();
                Gson gson = new Gson();
                EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
                JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();
                List<EdgeFormData> formComponents = null;
                for (int i = 0; i < size; i++)
                {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                        try {
                            formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                            }.getType());
                            List<Dictionary> lstDictionary = restEdgeNote.getDictionary();
                            String strValue = "";
                            for(Dictionary dictionary:lstDictionary) {
                                String strKey = dictionary.getKey();
                                if (strKey.equals("groupGuid")) {
                                    strValue = dictionary.getValue();
                                    break;
                                }
                            }
                            String strGeom = restEdgeNote.getGeometry();
                            LatLong latLong = LatLongUtils.getLatLngFromGeoJson(strGeom);
                            System.out.println("Checking layer");
                            if(strValue.equals(completeLayer) && latLong != null)
                            {
                                System.out.println("layer : Complete");
                                String strAction = getValue(formComponents,"ubicquia_action");
                                long ctimestamp = System.currentTimeMillis();


                                if(strAction.equals(installAction)) {
                                    String strInstalldevui = getValue(formComponents, "ubicquia_deveui");
                                    if(strInstalldevui == null)
                                    {
                                        strInstalldevui = "";
                                    }
                                    strInstalldevui = get_devui(strInstalldevui);
                                    System.out.println("devui : strInstalldevui");
                                    if(!strInstalldevui.equals(""))
                                    {
                                        ubiTransactionLog.setDevui(strInstalldevui);
                                        UbicquiaLightsInterface.requestDynamicToken();
                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lastUpdated,strInstalldevui);
                                        if (jobj1 != null)
                                        {
                                            LatLong2 lampLatLng = getLatLong(jobj1);
                                            LatLong2 noteLatLng = new LatLong2();
                                            if(lampLatLng != null)
                                            {
                                                noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                                noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                                if ((lampLatLng.getLat() != 0.0) && (lampLatLng.getLng() != 0.0)) {
                                                    double distance = DistanceCalculator.getDistance(lampLatLng, noteLatLng);
                                                    if (distance > distth) {
                                                        String lampLng = Double.toString(lampLatLng.getLng());
                                                        String lampLat = Double.toString(lampLatLng.getLat());
                                                        updateLocationData(edgenoteJson, lampLng, lampLat);
                                                        mustUpdate = true;
                                                        ubiTransactionLog.setAction("LC_UPDATE");
                                                    } else {
                                                        ubiTransactionLog.setAction("LC_NO_UPDATE");
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                ubiTransactionLog.setAction("Bad location from Device");
                                            }
                                            ////
                                        }
                                    }
                                    ///////////////////////////////////////////////////
                                }
                                else if(strAction.equals(replaceAction))
                                {
                                    String strReplacedevui = getValue(formComponents,"ubicquia_replacedeveui");
                                    if(strReplacedevui == null)
                                    {
                                        strReplacedevui = "";
                                    }
                                    strReplacedevui = get_devui(strReplacedevui);

                                    if(strReplacedevui.equals(""))
                                    {
                                        String strInstalldevui = getValue(formComponents, "ubicquia_deveui");
                                        if(strInstalldevui == null)
                                        {
                                            strInstalldevui = "";
                                        }
                                        strInstalldevui = get_devui(strInstalldevui);
                                        strReplacedevui = strInstalldevui;
                                    }
                                    if(!strReplacedevui.equals(""))
                                    {
                                        ubiTransactionLog.setDevui(strReplacedevui);
                                        UbicquiaLightsInterface.requestDynamicToken();
                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lastUpdated,strReplacedevui);
                                        if (jobj1 != null) {
                                            LatLong2 lampLatLng = getLatLong(jobj1);
                                            if(lampLatLng != null) {
                                                LatLong2 noteLatLng = new LatLong2();
                                                noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                                noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                                if ((lampLatLng.getLat() != 0.0) && (lampLatLng.getLng() != 0.0)) {
                                                    double distance = DistanceCalculator.getDistance(lampLatLng, noteLatLng);
                                                    if (distance > distth) {
                                                        String lampLng = Double.toString(lampLatLng.getLng());
                                                        String lampLat = Double.toString(lampLatLng.getLat());
                                                        updateLocationData(edgenoteJson, lampLng, lampLat);
                                                        mustUpdate = true;
                                                        ubiTransactionLog.setAction("LC_UPDATE");
                                                    } else {
                                                        ubiTransactionLog.setAction("LC_NO_UPDATE");
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                ubiTransactionLog.setAction("Bad location from Device");
                                            }
                                            ////
                                        }
                                    }
                                    ///////////////////////////////////////////////////////////
                                }

                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            continue;
                        }

                    }
                    serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                }
                edgenoteJson.add("formData", serverForms);
                edgenoteJson.addProperty("createdBy", ignoreUser);
                long ntime = System.currentTimeMillis();
                //lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, ntime);
                edgenoteJson.addProperty("createdDateTime", ntime);
                if(mustUpdate) {
                    ResponseEntity<String> upresponse = RESTService.updateNoteDetails(edgenoteJson.toString(), noteguid, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                    String id1 = upresponse.getBody();
                    System.out.println(id1);
                    //TerragoDAO.updateUser(id1,ignoreUser);
                }
    }

}
