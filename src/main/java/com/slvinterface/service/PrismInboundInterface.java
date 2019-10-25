package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.SLVFields;
import com.slvinterface.utils.DataOperations;
import com.slvinterface.utils.FileOperationUtils;
import com.slvinterface.utils.FormValueUtil;
import com.slvinterface.utils.ResourceDetails;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrismInboundInterface extends InBoundInterface {
    public PrismInboundInterface() throws Exception {

    }

    public void addNewDevices() {
        List<String[]> results = slvDataQueryExecutor.getNewDeviceList(inBoundConfig);
        String slvDataFields = inBoundConfig.getSlvquery();
    }

    public void deleteDevices() {

    }

    public void updateDevices() {
        String folderPath = ResourceDetails.INBOUND_FILE_STORE + File.separator + FileOperationUtils.getCurrentDate();
        if (!FileOperationUtils.doesFolderExists(folderPath)) {
            FileOperationUtils.createFolder(folderPath);
        }
        String fileName = folderPath + File.separator + "updates_" + FileOperationUtils.getTime() + ".csv";
        List<String[]> results = slvDataQueryExecutor.getUpdatedDeviceListForPrism(inBoundConfig);
        int tc = results.size();
        try {
            FileWriter outputfile = new FileWriter(fileName);
            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            List<String> headers = new ArrayList<>();
            headers.add("idOnController");
            headers.add("idOnController_y");
            headers.add("name");
            headers.add("name_y");
            headers.add("controllerStrId");
            headers.add("controllerStrId_y");
            headers.add("CommunicationStatus");
            headers.add("CommunicationStatus_y");
            headers.add("location_locationtype");
            headers.add("location_locationtype_y");
            headers.add("macAddress");
            headers.add("macAddress_y");
            headers.add("TalqAddress");
            headers.add("TalqAddress_y");

            headers.add("device_node_serialnumber");
            headers.add("device_node_serialnumber_y");
            headers.add("device_node_hwversion");
            headers.add("device_node_hwversion_y");
            headers.add("device_node_hwType");
            headers.add("device_node_hwType_y");
            headers.add("device_nic_serialnumber");
            headers.add("device_nic_serialnumber_y");
            headers.add("device_nic_swversion");
            headers.add("device_nic_swversion_y");
            headers.add("device_nic_hwversion");
            headers.add("device_nic_hwversion_y");

            headers.add("device_nic_hwModel");
            headers.add("device_nic_hwModel_y");
            headers.add("device_uiqid");
            headers.add("device_uiqid_y");
            headers.add("device_meter_programid");
            headers.add("device_meter_programid_y");
            headers.add("location_utillocationid");
            headers.add("location_utillocationid_y");

            headers.add("device_nic_catalog");
            headers.add("device_nic_catalog_y");
            headers.add("dimmingGroupName");
            headers.add("dimmingGroupName_y");
            headers.add("lat");
            headers.add("lat_y");
            headers.add("lng");
            headers.add("lng_y");


            String[] csvheader = DataOperations.convertListToArray(headers);

            writer.writeNext(csvheader);

            for (int jdx = 0; jdx < tc; jdx++) {
                String[] rowData = results.get(jdx);

                writer.writeNext(rowData);
            }

            writer.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }

    public void updateNotes(String pathToCsv) {
        BufferedReader csvReader = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row = null;
            boolean headerProcessed = false;
            String headers[] = null;
            while ((row = csvReader.readLine()) != null) {
                if (headerProcessed) {
                    File f = new File("./stop.txt");
                    if (f.exists()) {
                        break;
                    }
                    String[] rowData = row.split(",");
                    String idoncontroller = rowData[0];
                    String name = rowData[1];
                    String name_y = rowData[2];
                    boolean mustProcess = false;
                    List<SLVFields> lstSLVFields = inBoundConfig.getSlvchangefields();
                    for (SLVFields cur : lstSLVFields) {
                        int idx = DataOperations.getIndex(headers, cur.getSlvfield());
                        if (idx != -1) {
                            if (idx + 1 < rowData.length) {
                                if (rowData[idx] == null) {
                                    rowData[idx] = "";
                                }
                                if (rowData[idx + 1] == null) {
                                    rowData[idx + 1] = "";
                                }
                                if (!rowData[idx].equals(rowData[idx + 1])) {
                                    mustProcess = true;
                                }
                            }
                        }
                    }
                    if (mustProcess) {
                        String noteGUID = slvDataQueryExecutor.getCurrentNoteGUIDFromIDOnController(idoncontroller, "Identifier", inBoundConfig.getFormtemplateguid());
                        String noteJson = getNoteDetails(noteGUID);
                        boolean mustUpdate = false;
                        if (!noteJson.equals("")) {
                            EdgeNote restEdgeNote = gson.fromJson(noteJson, EdgeNote.class);
                            JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
                            JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                            int size = serverForms.size();
                            for (int i = 0; i < size; i++) {
                                JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                                String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                                formDefJson = formDefJson.replaceAll("\\\\", "");
                                formDefJson = formDefJson.replace("u0026", "\\u0026");
                                List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                                }.getType());
                                if (formTemplate.equals(inBoundConfig.getFormtemplateguid())) {
                                    lstSLVFields = inBoundConfig.getSlvchangefields();
                                    for (SLVFields cur : lstSLVFields) {
                                        int idx = DataOperations.getIndex(headers, cur.getSlvfield());
                                        if (idx != -1) {
                                            if (idx + 1 < rowData.length) {
                                                if (rowData[idx] == null) {
                                                    rowData[idx] = "";
                                                }
                                                if (rowData[idx + 1] == null) {
                                                    rowData[idx + 1] = "";
                                                }
                                                if (!rowData[idx].equals(rowData[idx + 1])) {
                                                    int formID = Integer.parseInt(cur.getId());
                                                    FormValueUtil.updateEdgeForm(formComponents, formID, rowData[idx]);
                                                    mustUpdate = true;
                                                }
                                            }
                                        }
                                    }
                                }


                                serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                            }
                            edgenoteJson.add("formData", serverForms);
                            edgenoteJson.addProperty("createdBy", "admin");
                            long ntime = System.currentTimeMillis();

                            edgenoteJson.addProperty("createdDateTime", ntime);
                            if (mustUpdate) {
                                //Call Rest CAll to Update
                                updateNoteDetails(edgenoteJson.toString(), noteGUID, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                            }

                        } else {

                        }
                    }
                } else {
                    headerProcessed = true;
                    headers = row.split(",");
                }
                ////////////////////////////////////////////////////////////////////////


            }

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
