package com.terrago.streetlights.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.TerragoDAO;
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

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    private void updateLocationData(JsonObject job1, String longitude, String latitude)
    {
        String geomStr = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[";
        geomStr =  geomStr + longitude + "," + latitude;
        geomStr = geomStr + "]}}";
        job1.addProperty("geometry",geomStr);
        //edgeNote.setGeometry(geomStr);
    }
    private LatLong2 getLatLong(JsonObject jsonObject)
    {
        String strLat = jsonObject.get("latitude").getAsString();
        String strLng = jsonObject.get("longitude").getAsString();
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
    public void run(){
        //processLocationChange();
    }
    public void processLocationChange(){
        long lastMaxUpdatedTime = 0;
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
                            if(strValue.equals(completeLayer) && latLong != null)
                            {
                                String strAction = getValue(formComponents,"ubicquia_action");
                                long ctimestamp = System.currentTimeMillis();
                                if(UbicquiaLightsInterface.getTimeStamp() == 0)
                                {
                                    UbicquiaLightsInterface.requestDynamicToken();
                                }
                                else
                                {
                                    if(ctimestamp - UbicquiaLightsInterface.getTimeStamp() >= 300000)
                                    {
                                        UbicquiaLightsInterface.requestDynamicToken();
                                    }
                                }
                                if(strAction.equals(installAction))
                                {
                                    String strInstalldevui = getValue(formComponents,"ubicquia_deveui");
                                    JsonObject jobj1 = UbicquiaLightsInterface.getNodes(strInstalldevui);
                                    if(jobj1 != null)
                                    {
                                        LatLong2 lampLatLng = getLatLong(jobj1);
                                        LatLong2 noteLatLng = new LatLong2();
                                        noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                        noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                        double distance = DistanceCalculator.getDistance(lampLatLng,noteLatLng);
                                        if(distance > distth)
                                        {
                                            String lampLng = Double.toString(lampLatLng.getLng());
                                            String lampLat = Double.toString(lampLatLng.getLat());
                                            updateLocationData(edgenoteJson,lampLng,lampLat);
                                            mustUpdate = true;
                                        }
                                    }
                                }
                                else if(strAction.equals(replaceAction))
                                {
                                    String strReplacedevui = getValue(formComponents,"ubicquia_replacedeveui");
                                    UbicquiaLightsInterface.requestDynamicToken();
                                    JsonObject jobj1 = UbicquiaLightsInterface.getNodes(strReplacedevui);
                                    if(jobj1 != null)
                                    {
                                        LatLong2 lampLatLng = getLatLong(jobj1);
                                        LatLong2 noteLatLng = new LatLong2();
                                        noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                        noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                        double distance = DistanceCalculator.getDistance(lampLatLng,noteLatLng);
                                        if(distance > distth)
                                        {
                                            String lampLng = Double.toString(lampLatLng.getLng());
                                            String lampLat = Double.toString(lampLatLng.getLat());
                                            updateLocationData(edgenoteJson,lampLng,lampLat);
                                            mustUpdate = true;
                                        }
                                    }

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
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, ntime);
                edgenoteJson.addProperty("createdDateTime", ntime);
                if(mustUpdate) {
                    ResponseEntity<String> upresponse = RESTService.updateNoteDetails(edgenoteJson.toString(), noteguid, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                    String id1 = upresponse.getBody();
                    System.out.println(id1);
                    TerragoDAO.updateUser(id1,ignoreUser);
                }

            /*//Write
            long lntime = TerragoDAO.readLastUpdatedTime2();
            if(lntime >= lastMaxUpdatedTime)
            {
                lastMaxUpdatedTime = lntime;
            }
            TerragoDAO.writeLastUpdateTime2(lastMaxUpdatedTime);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }*/

    }
    public void run2() {
        //super.run();
        String completeLayer = PropertiesReader.getProperties().getProperty("ubicquia_notelayer");
        String installAction = PropertiesReader.getProperties().getProperty("ubicquia_actioninstall");
        String replaceAction = PropertiesReader.getProperties().getProperty("ubicquia_actionmaintain");
        String ignoreUser = PropertiesReader.getProperties().getProperty("ignoreuser");
        String distanceTh = PropertiesReader.getProperties().getProperty("ubicquia_distance");
        double distth = Double.parseDouble(distanceTh);
        do {
            System.out.println("Looking for Complete Changes ...");
            long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime2();
            List<String> lstTitles = TerragoDAO.getUpdatedTitles(Long.toString(lastMaxUpdatedTime));
            for(String cur:lstTitles)
            {
                boolean mustUpdate = false;
                String noteGUID = TerragoDAO.getNoteGUID(cur);
                String notesJson =  RESTService.getNoteDetails(noteGUID);
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
                            if(strValue.equals(completeLayer) && latLong != null)
                            {
                                String strAction = getValue(formComponents,"ubicquia_action");
                                long ctimestamp = System.currentTimeMillis();
                                if(UbicquiaLightsInterface.getTimeStamp() == 0)
                                {
                                    UbicquiaLightsInterface.requestDynamicToken();
                                }
                                else
                                {
                                    if(ctimestamp - UbicquiaLightsInterface.getTimeStamp() >= 300000)
                                    {
                                        UbicquiaLightsInterface.requestDynamicToken();
                                    }
                                }
                                if(strAction.equals(installAction))
                                {
                                    String strInstalldevui = getValue(formComponents,"ubicquia_deveui");
                                    JsonObject jobj1 = UbicquiaLightsInterface.getNodes(strInstalldevui);
                                    if(jobj1 != null)
                                    {
                                        LatLong2 lampLatLng = getLatLong(jobj1);
                                        LatLong2 noteLatLng = new LatLong2();
                                        noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                        noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                        double distance = DistanceCalculator.getDistance(lampLatLng,noteLatLng);
                                        if(distance > distth)
                                        {
                                            String lampLng = Double.toString(lampLatLng.getLng());
                                            String lampLat = Double.toString(lampLatLng.getLat());
                                            updateLocationData(edgenoteJson,lampLng,lampLat);
                                            mustUpdate = true;
                                        }
                                    }
                                }
                                else if(strAction.equals(replaceAction))
                                {
                                    String strReplacedevui = getValue(formComponents,"ubicquia_replacedeveui");
                                    JsonObject jobj1 = UbicquiaLightsInterface.getNodes(strReplacedevui);
                                    if(jobj1 != null)
                                    {
                                        LatLong2 lampLatLng = getLatLong(jobj1);
                                        LatLong2 noteLatLng = new LatLong2();
                                        noteLatLng.setLat(Double.parseDouble(latLong.getLat()));
                                        noteLatLng.setLng(Double.parseDouble(latLong.getLng()));
                                        double distance = DistanceCalculator.getDistance(lampLatLng,noteLatLng);
                                        if(distance > distth)
                                        {
                                            String lampLng = Double.toString(lampLatLng.getLng());
                                            String lampLat = Double.toString(lampLatLng.getLat());
                                            updateLocationData(edgenoteJson,lampLng,lampLat);
                                            mustUpdate = true;
                                        }
                                    }

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
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, ntime);
                edgenoteJson.addProperty("createdDateTime", ntime);
                if(mustUpdate) {
                    RESTService.updateNoteDetails(edgenoteJson.toString(), noteGUID, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                }
            }
            //Write
            long lntime = TerragoDAO.readLastUpdatedTime2();
            if(lntime >= lastMaxUpdatedTime)
            {
                lastMaxUpdatedTime = lntime;
            }
            TerragoDAO.writeLastUpdateTime2(lastMaxUpdatedTime);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }while(true);
    }
}
