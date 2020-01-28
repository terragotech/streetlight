package com.terrago.streetlights.utils;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.App;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.service.DeviceMeteringData;
import com.terrago.streetlights.service.RESTService;
import com.terrago.streetlights.service.UbicquiaLightsInterface;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateOutageData {
    private static Logger logger = Logger.getLogger(CreateRevision.class);
    private void updateData(OutageData outageData)
    {
        if(outageData.getFailureReportModel().isOutage() || outageData.getFailureReportModel().isWarning())
        {
            //count = count + 1;

            String noteguid = TerragoDAO.getNoteGUIDForTitle(outageData.getTitle());
            if (!noteguid.equals("")) {
                boolean mustupdate = false;
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(noteguid);
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                String notesJson = RESTService.getNoteDetails(noteguid);
                System.out.println(notesJson);
                Type listType = new TypeToken<ArrayList<EdgeNote>>() {
                }.getType();
                Gson gson = new Gson();
                List<EdgeNote> edgeNoteList = new ArrayList<>();
                //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
                EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);

                notesJson = gson.toJson(restEdgeNote, EdgeNote.class);
                JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();
                for (int i = 0; i < size; i++) {

                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    System.out.println(formDefJson);
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    System.out.println(formTemplate);
                    System.out.println("Comparision ...");
                    System.out.println(formDefJson);
                    formDefJson = formDefJson.replaceAll("\\\\", "");
                    System.out.println(formDefJson);
                    formDefJson = formDefJson.replace("u0026", "\\u0026");
                    System.out.println(formDefJson);
                    System.out.println("End Comparision ...");
                    List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                    }.getType());

                    if (formTemplate.equals("3d7d83ce-2212-4729-9321-e613e99bd061"))
                    {


                        mustupdate = true;
                        String type = "";
                        if (outageData.getFailureReportModel().isWarning()) {
                            type = "Warning";
                        }
                        if (outageData.getFailureReportModel().isOutage()) {
                            type = type + "," + "Outage";
                        }
                        FailureReportModel failureReportModel = outageData.getFailureReportModel();
                        TerragoUpdate.updateEdgeForm(formComponents, 50, type);
                        TerragoUpdate.updateEdgeForm(formComponents, 46, failureReportModel.getFailureReason());
                        TerragoUpdate.updateEdgeForm(formComponents, 47, failureReportModel.getLastUpdate());
                        TerragoUpdate.updateEdgeForm(formComponents, 45, failureReportModel.getFailedSince());
                        TerragoUpdate.updateEdgeForm(formComponents, 48, failureReportModel.getBurningHours());
                        TerragoUpdate.updateEdgeForm(formComponents, 49, failureReportModel.getLifeTime());


                    }
                    serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                }
                edgenoteJson.add("formData", serverForms);
                edgenoteJson.addProperty("createdBy", "admin");
                long ntime = System.currentTimeMillis();

                edgenoteJson.addProperty("createdDateTime", ntime);
                if (mustupdate) {
                    ResponseEntity<String> responseEntity = RESTService.updateNoteDetails(edgenoteJson.toString(), noteguid, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                    if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                        //String newNoteGUID = responseEntity.getBody();
                        //TerragoDAO.updateModifiedUserName(newNoteGUID);
                    }
                }
            }
        }

    }
    public void createRevision2()
    {
        List<OutageData> lstOutageData = TerragoDAO.getAllOutageData();
        Long count = 0L;
        for(OutageData outageData:lstOutageData)
        {
           //updateData(outageData);
            if(outageData.getFailureReportModel().isOutage())
            {
                count = count + 1;
            }
        }
        System.out.println("Total Count "+ count);
    }
    public void createRevision(String pathToCsv) {
        BufferedReader csvReader = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));

            String row = null;
            int processCount = 0;
            List<String> lstProcessed = new ArrayList<String>();
            List<String> lstNotProcessed = new ArrayList<String>();
            while ((row = csvReader.readLine()) != null) {
                File f = new File("./stop.txt");
                if(f.exists())
                {
                    break;
                }

            }//End While

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(csvReader != null)
            {
                try {
                    csvReader.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}

