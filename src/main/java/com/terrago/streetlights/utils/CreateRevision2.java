package com.terrago.streetlights.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.service.RESTService;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateRevision2 {
    private static Logger logger = Logger.getLogger(CreateRevision.class);
    public void createRevision(String pathToCsv) {
        BufferedReader csvReader = null;
        TextFileWriter textFileWriter = new TextFileWriter();
        textFileWriter.openFile("./updateresults.csv");
        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));

            String row = null;
            int processCount = 0;
            List<String> lstProcessed = new ArrayList<String>();
            List<String> lstNotProcessed = new ArrayList<String>();
            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                String []columns = row.split(",");
                String noteguid = TerragoDAO.getNoteGUIDForTitle(columns[1]);
                if(!noteguid.equals(""))
                {
                    lstProcessed.add(columns[1]);
                    processCount = processCount + 1;
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

                    logger.info("Processing " + columns[1]);
                    boolean bMustUpdate = false;
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
                        formDefJson = formDefJson.replace("u0026","\\u0026");
                        System.out.println(formDefJson);
                        System.out.println("End Comparision ...");
                        //if(formTemplate.equals("")) {
                        List<EdgeFormData> formComponents = null;
                        try {
                            formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                            }.getType());
                            bMustUpdate = true;
                        }
                        catch (Exception e)
                        {
                            textFileWriter.writeData("note processed:" + columns[1]);
                            bMustUpdate = false;
                            continue;

                        }
                            try {
                                ///Controller Str ID
                                TerragoUpdate.updateEdgeForm(formComponents, 1165, columns[2]);
                                //CommunicationStatus
                                TerragoUpdate.updateEdgeForm(formComponents, 1175, columns[3]);
                                //location_locationtype
                                TerragoUpdate.updateEdgeForm(formComponents, 1163, columns[4]);
                                //Identifier
                                TerragoUpdate.updateEdgeForm(formComponents, 1162, columns[1]);
                                //MacAddress
                                TerragoUpdate.updateEdgeForm(formComponents, 1160, columns[5]);
                                //TalqAddress
                                TerragoUpdate.updateEdgeForm(formComponents, 1158, columns[6]);
                                //device_node_serialnumber
                                TerragoUpdate.updateEdgeForm(formComponents, 1161, columns[7]);
                                //device_node_hwversion
                                TerragoUpdate.updateEdgeForm(formComponents, 1167, columns[8]);
                                //device_node_hwType
                                TerragoUpdate.updateEdgeForm(formComponents, 1155, columns[9]);
                                //device_nic_serialnumber
                                TerragoUpdate.updateEdgeForm(formComponents, 1169, columns[10]);
                                //device_nic_swversion
                                TerragoUpdate.updateEdgeForm(formComponents, 1170, columns[11]);
                                //device_nic_hwversion
                                TerragoUpdate.updateEdgeForm(formComponents, 1171, columns[12]);
                                //device_nic_hwModel
                                TerragoUpdate.updateEdgeForm(formComponents, 1172, columns[13]);
                                //device_uiqid
                                TerragoUpdate.updateEdgeForm(formComponents, 1173, columns[14]);
                                //device_meter_programid
                                TerragoUpdate.updateEdgeForm(formComponents, 1153, columns[15]);
                                //location_utillocationid
                                TerragoUpdate.updateEdgeForm(formComponents, 1156, columns[16]);
                                //device_nic_catalog
                                TerragoUpdate.updateEdgeForm(formComponents, 1152, columns[17]);
                                if (columns.length < 19) {
                                    //dimmingGroupName
                                    TerragoUpdate.updateEdgeForm(formComponents, 1174, "");
                                } else {
                                    //dimmingGroupName
                                    TerragoUpdate.updateEdgeForm(formComponents, 1174, columns[18]);
                                }
                            }
                            catch (ArrayIndexOutOfBoundsException e)
                            {
                                e.printStackTrace();
                            }

                        serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                        serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                        //}
                    }
                    edgenoteJson.add("formData", serverForms);
                    edgenoteJson.addProperty("createdBy", "slvinterface");
                    long ntime = System.currentTimeMillis();

                    edgenoteJson.addProperty("createdDateTime", ntime);
                    if(bMustUpdate) {
                        ResponseEntity<String> responseEntity = RESTService.updateNoteDetails(edgenoteJson.toString(), noteguid, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                            String newNoteGUID = responseEntity.getBody();
                            TerragoDAO.updateModifiedUserName(newNoteGUID);
                        }
                        textFileWriter.writeData("processed:" + columns[1]);
                    }
                }
                else
                {
                    //lstNotProcessed.add(row);
                    textFileWriter.writeData("notprocessed:"+columns[1]);
                }
            }//End While
            textFileWriter.closeFile();
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
