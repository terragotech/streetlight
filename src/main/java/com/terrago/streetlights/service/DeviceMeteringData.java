package com.terrago.streetlights.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.utils.JsonDataParser;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terrago.streetlights.utils.TerragoUpdate;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;

public class DeviceMeteringData implements Runnable{
    private Logger logger = Logger.getLogger(DeviceMeteringData.class);
    private String fixtureID;
    private String strID;
    private String nguid;
    Thread t;
    public DeviceMeteringData(String fixtureID,String strID,String nguid){
        this.fixtureID = fixtureID;
        this.strID = strID;
        t = new Thread(this);
        this.nguid = nguid;
        t.start();
    }
    public void run(){
        try {
            Thread.sleep(2000);
            JsonObject result1 = UbicquiaLightsInterface.getNodes(fixtureID);
            if(result1 != null) {
                Gson gson = new Gson();
                String noteGUID = nguid;//TerragoDAO.getCurrentNoteGUID(fixtureID);
                String notesJson = RESTService.getNoteDetails(noteGUID);
                EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
                JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                    }.getType());
                    if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                        logger.info("Matching template found");

                        String strPolecurrent = JsonDataParser.checkDataNull(result1, "CState");
                        String strFixturecurrent = JsonDataParser.checkDataNull(result1, "C1State");
                        String strPoleVoltage = JsonDataParser.checkDataNull(result1, "VState");
                        String strFixVoltage = JsonDataParser.checkDataNull(result1, "V1State");
                        String strDimValue = JsonDataParser.checkDataNull(result1, "LD1State");
                        //Pole Current
                        int idpolecurrent = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_polecurrent"));
                        //TerragoUpdate.updateEdgeForm(formComponents, idpolecurrent, strPolecurrent);
                        //Fixture Current
                        int idfixcurrent = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixturecurrent"));
                        //TerragoUpdate.updateEdgeForm(formComponents, idfixcurrent, strFixturecurrent);
                        //Pole Voltage
                        int idpolevoltage = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_polevoltage"));
                        //TerragoUpdate.updateEdgeForm(formComponents, idpolevoltage, strPoleVoltage);
                        //Fixture Voltage
                        int idfixvoltage = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixvoltage"));
                        //TerragoUpdate.updateEdgeForm(formComponents, idfixvoltage, strFixVoltage);
                        //Dim Value
                        int iddimvalue = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_mdimvalue"));
                        //TerragoUpdate.updateEdgeForm(formComponents, iddimvalue, strDimValue);

                        String sfc = PropertiesReader.getProperties().getProperty("ubicquia_sdc");
                        int idsfc = Integer.parseInt(sfc);
                        TerragoUpdate.updateEdgeForm(formComponents, idsfc, "No");

                    }
                    serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    long ntime = System.currentTimeMillis();
                    long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime();
                    if (lastMaxUpdatedTime <= ntime) {
                        TerragoDAO.writeLastUpdateTime(ntime);
                    }
                    edgenoteJson.addProperty("createdDateTime", ntime);
                    RESTService.updateNoteDetails(edgenoteJson.toString(), noteGUID, restEdgeNote.getEdgeNotebook().getNotebookGuid());

                }
                Thread.sleep(300000);
                UbicquiaLightsInterface.requestDynamicToken();
                UbicquiaLightsInterface.SetDevice(strID,false);
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
