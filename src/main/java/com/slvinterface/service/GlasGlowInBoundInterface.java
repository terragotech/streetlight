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
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.slvinterface.utils.*;

import javax.xml.crypto.Data;

public class GlasGlowInBoundInterface extends InBoundInterface {


    public GlasGlowInBoundInterface() throws Exception{

    }
    private List<String> generateHeaders(String []fields,List<SLVFields> lstSLVFields){
        int fieldCount = fields.length;
        List<String> headers = new ArrayList<>();
       /* for(int idx=0;idx<fieldCount;idx++)
        {*/
        for(SLVFields cur:lstSLVFields)
        {
                /*if(fields[idx].equals(cur.getSlvfield()))
                {*/
            headers.add(cur.getId());
            /*}*/
        }
        /*}*/
        headers.add("title");
        headers.add("lat");
        headers.add("lng");
        headers.add("layerguid");
        headers.add("formtemplateguid");
        headers.add("notebookguid");
        headers.add("description");
        headers.add("location");


        return headers;
    }

    private List<List<String>> generateValues(String []fields,List<SLVFields> lstSLVFields,List<String[]> results ){

        List<List<String>> lstValues = new ArrayList<List<String>>();
        int rowDataSize =results.size();
        for(int jdx=0;jdx<rowDataSize;jdx++)
        {
            String []rowData = results.get(jdx);
            List<String> values = new ArrayList<>();
            boolean macAddressFound = false;
            for (SLVFields cur : lstSLVFields) {
                if (cur.getSlvfield().startsWith("CONST.VALUE.INSTALL_STATUS")) {
                    String macAddressField = inBoundConfig.getSlvnmacaddressfield();
                    int idx = DataOperations.getIndex(fields, macAddressField);
                    if (idx != -1) {
                        String optionValues = cur.getValue();
                        String[] option = optionValues.split(",");

                        if (!rowData[idx].equals("")) {
                            macAddressFound = true;
                            values.add(option[0]);
                        } else {
                            if(option.length > 1)
                            {
                                values.add(option[1]);
                            }
                            else
                            {
                                values.add("");
                            }
                        }
                    }
                } else if (cur.getSlvfield().startsWith("CONST.VALUE")) {
                    values.add(cur.getValue());
                }
                else if(cur.getSlvfield().startsWith("CUSTOM.VALUE.1"))
                {
                    int idx = DataOperations.getIndex(fields, "idoncontroller");
                    String idoncontroller = rowData[idx];
                    String []splitValues = idoncontroller.split(" ");
                    if(splitValues.length > 1) {
                        values.add(splitValues[1]);
                    }
                    else
                    {
                        values.add("");
                    }

                } else {
                    int idx = DataOperations.getIndex(fields, cur.getSlvfield());
                    //System.out.println(cur.getSlvfield());
                    if (idx != -1) {
                        if(rowData[idx] == null)
                        {
                            rowData[idx] = "";
                        }
                        values.add(rowData[idx]);
                    }

                }
            }
            values.add(DataOperations.getValue(fields,"name",rowData));
            values.add(DataOperations.getValue(fields,"lat",rowData));
            values.add(DataOperations.getValue(fields,"lng",rowData));
            if(macAddressFound)
            {
                values.add(inBoundConfig.getCompletenotelayerguid());
            }
            else
            {
                values.add(inBoundConfig.getNotcompletenotelayerguid());
            }
            values.add(inBoundConfig.getFormtemplateguid());
            String geoZone = DataOperations.getValue(fields,"geozonepath",rowData);
            String geoZoneValues[] = geoZone.split("/");
            String noteBookName = "";
            String noteBookGUID = "";
            if(geoZoneValues.length > 1)
            {
                noteBookName = geoZoneValues[1];
                noteBookGUID = slvDataQueryExecutor.getNoteBookGuid(noteBookName);
            }
            values.add(noteBookGUID);
            values.add(DataOperations.getValue(fields,"luminaire_brand",rowData) + "-" +  DataOperations.getValue(fields,"luminaire_model",rowData));
            values.add(DataOperations.getValue(fields,"address",rowData));

            lstValues.add(values);
        }
        return lstValues;


    }


    public  void addNewDevices()
    {
        List<String[]> results = slvDataQueryExecutor.getNewDeviceList(inBoundConfig);
        String slvDataFields = inBoundConfig.getSlvquery();
        String []fields = slvDataFields.split(",");

        List<SLVFields> lstSLVFields = inBoundConfig.getSlvfields();
        List<String> headers = generateHeaders(fields,lstSLVFields);
        List<List<String>> lstvalues = generateValues(fields,lstSLVFields,results);
        try {
            String folderPath = ResourceDetails.INBOUND_FILE_STORE+ File.separator+FileOperationUtils.getCurrentDate();
            if(!FileOperationUtils.doesFolderExists(folderPath))
            {
                FileOperationUtils.createFolder(folderPath);
            }
            String fileName = folderPath + File.separator + "new_devices_" + FileOperationUtils.getTime() + ".csv";
            FileWriter outputfile = new FileWriter(fileName);
            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String []csvheader = DataOperations.convertListToArray(headers);

            writer.writeNext(csvheader);
            for(List<String> cur: lstvalues)
            {
                String []csvvalues = DataOperations.convertListToArray(cur);
                writer.writeNext(csvvalues);
            }
            writer.close();
            if(lstvalues.size() > 0)
            {
                //addDeviceToServer(fileName);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




    }
    public  void deleteDevices()
    {



    }

    public void updateNotes(String pathToCsv){
        BufferedReader csvReader = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row = null;
            boolean headerProcessed = false;
            while ((row = csvReader.readLine()) != null) {
                if(headerProcessed)
                {
                    File f = new File("./stop.txt");
                    if(f.exists())
                    {
                        break;
                    }
                    String []rowData = row.split(",");
                    String idoncontroller = rowData[0];
                    String name = rowData[1];
                    String name_y = rowData[2];
                    String macAddress = rowData[3];
                    String macAddress_y = rowData[4];
                    String macAddressUpdateStatus = rowData[5];
                    String noteGUID = slvDataQueryExecutor.getCurrentNoteGUIDFromIDOnController(idoncontroller, "UtilLocationID", "99f5300b-c1e4-4734-94f0-1ec70a35d6ae");
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
                                if(!name.equals(name_y))
                                {
                                    String title = edgenoteJson.get("title").getAsString();
                                    edgenoteJson.addProperty("title",name);
                                    mustUpdate = true;
                                }
                                if(macAddressUpdateStatus.equals("true"))
                                {
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
                }
                else
                {
                    headerProcessed = true;
                }
            }

            csvReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void updateDevices()
    {
        String folderPath = ResourceDetails.INBOUND_FILE_STORE+ File.separator+FileOperationUtils.getCurrentDate();
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

    @Override
    public boolean applyChanges(JsonObject edgenoteJson, List<FormValues> formComponents, String[] rowData) {
        boolean mustUpdate = false;
        String name = rowData[1];
        String name_y = rowData[2];
        String macAddress = rowData[3];
        String macAddress_y = rowData[4];
        String installStatus = rowData[5];
        String macAddressUpdateStatus = rowData[6];

        mustUpdate = doTitleUpdate(edgenoteJson,name,name_y);
        if(macAddressUpdateStatus.equals("true")) {
            doMacAddressUpdate(macAddressUpdateStatus, macAddress, macAddress_y, formComponents,installStatus);
            if(!mustUpdate)
            {
                mustUpdate = true;
            }
        }
        return  mustUpdate;
    }

    public String getNoteGUID(String idoncontroller, String title,String formfield)
    {
        String noteGUID = slvDataQueryExecutor.getCurrentNoteGUIDFromIDOnController(idoncontroller, formfield, inBoundConfig.getFormtemplateguid());
        return noteGUID;
    }
}

