package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.SLVFields;
import com.slvinterface.model.HistoryModel;
import com.slvinterface.model.SLVDataInfo;
import com.slvinterface.utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LondonInBoundInterface extends InBoundInterface{
    private EdgeRestService edgeRestService;
    public LondonInBoundInterface() throws Exception
    {
        edgeRestService = new EdgeRestService();
    }
    @Override
    public void addNewDevices() {
        List<String[]> results = slvDataQueryExecutor.getNewDeviceList(inBoundConfig);
        String slvDataFields = inBoundConfig.getSlvquery();
        String []fields = slvDataFields.split(",");

    }
    @Override
    public void deleteDevices() {
        String folderPath = ResourceDetails.INBOUND_FILE_STORE+ File.separator+ FileOperationUtils.getCurrentDate();
        if(!FileOperationUtils.doesFolderExists(folderPath))
        {
            FileOperationUtils.createFolder(folderPath);
        }
        String fileName = folderPath + File.separator + "deleted_" + FileOperationUtils.getTime() + ".csv";
        List<String[]> results = slvDataQueryExecutor.getDelDeviceList(inBoundConfig);
        int tc = results.size();
        try {
            FileWriter outputfile = new FileWriter(fileName);
            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            List<String> headers = new ArrayList<>();
            headers.add("idoncontroller");
            headers.add("name");

            String []csvheader = DataOperations.convertListToArray(headers);

            writer.writeNext(csvheader);

            for(int jdx=0;jdx<tc;jdx++)
            {
                String []rowData = results.get(jdx);
                writer.writeNext(rowData);
            }

            writer.close();
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }

    }

    @Override
    public void updateDevices() {
        String folderPath = ResourceDetails.INBOUND_FILE_STORE+ File.separator+ FileOperationUtils.getCurrentDate();
        if(!FileOperationUtils.doesFolderExists(folderPath))
        {
            FileOperationUtils.createFolder(folderPath);
        }
        String fileName = folderPath + File.separator + "updates_" + FileOperationUtils.getTime() + ".csv";
        List<String[]> results = slvDataQueryExecutor.getUpdatedDeviceList(inBoundConfig);
        int tc = results.size();
        try {
            FileWriter outputfile = new FileWriter(fileName);
            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            List<String> headers = new ArrayList<>();
            headers.add("idoncontroller");
            headers.add("name");
            headers.add("name_y");
            headers.add("macaddress");
            headers.add("macaddress_y");
            headers.add("mustUpdateMac");
            String []csvheader = DataOperations.convertListToArray(headers);

            writer.writeNext(csvheader);

            for(int jdx=0;jdx<tc;jdx++)
            {
                String []rowData = results.get(jdx);
                processUpdate(rowData,writer);
            }

            writer.close();
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
    }
    public void startProcessing() throws Exception{
        super.startProcessing();
    }
    public void updateNotes(String pathToCsv) {
        BufferedReader csvReader = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row = null;
            boolean headerProcessed = false;
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
                    String macAddress = rowData[3];
                    String macAddress_y = rowData[4];
                    String macAddressUpdateStatus = rowData[5];
                    String noteGUID = slvDataQueryExecutor.getCurrentNoteGUIDFromIDOnController(idoncontroller, "UC Reference", inBoundConfig.getFormtemplateguid());
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

                                /* Update here */
                                if (!name.equals(name_y)) {
                                    String title = edgenoteJson.get("title").getAsString();
                                    edgenoteJson.addProperty("title", name);
                                    mustUpdate = true;
                                }
                                if (macAddressUpdateStatus.equals("true")) {
                                    if(macAddress_y.equals("") && !macAddress.equals(""))
                                    {
                                        //Install
                                        System.out.println("Install Section");
                                        int id = Integer.parseInt(inBoundConfig.getInstallmacaddress_id());
                                        String existingMacAddress = FormValueUtil.getValue(formComponents,id);
                                        if(existingMacAddress.equals(""))
                                        {
                                            FormValueUtil.updateEdgeForm(formComponents,id,macAddress);
                                        }
                                        else
                                        {
                                            //Do Replace work flow
                                        }

                                    }
                                    else if(!macAddress_y.equals("") && macAddress.equals(""))
                                    {
                                        //Remove
                                        System.out.println("Remove");
                                    }
                                    else
                                    {
                                        //Replace
                                        System.out.println("Replace");
                                    }
                                    mustUpdate = true;

                                    List<SLVFields> lstSLVChange = inBoundConfig.getSlvchangefields();
                                    for (SLVFields cur : lstSLVChange) {

                                        if (cur.getSlvfield().equals("macaddress")) {
                                            int id = Integer.parseInt(cur.getId());
                                            FormValueUtil.updateEdgeForm(formComponents, id, macAddress);
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
                } else {
                    headerProcessed = true;
                }
            }

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
