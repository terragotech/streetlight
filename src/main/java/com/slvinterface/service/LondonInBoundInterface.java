package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.query.In;
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
    private List<String> generateHeaders(String []fields,List<SLVFields> lstSLVFields){
        List<String> headers = new ArrayList<>();
        for(SLVFields cur:lstSLVFields)
        {
            headers.add(cur.getId());
        }
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

                    int idx = DataOperations.getIndex(fields, cur.getSlvfield());
                    System.out.println(cur.getSlvfield());
                    if (idx != -1) {
                        if(rowData[idx] == null)
                        {
                            rowData[idx] = "";
                        }
                        values.add(rowData[idx]);
                    }

            }
            values.add(DataOperations.getValue(fields,"name",rowData));
            values.add(DataOperations.getValue(fields,"lat",rowData));
            values.add(DataOperations.getValue(fields,"lng",rowData));

            int zdx = DataOperations.getIndex(fields, "macaddress");
            if(zdx != -1)
            {
                String macaddress = rowData[zdx];
                macaddress = FormValueUtil.checkNullValues(macaddress);
                if(!macaddress.equals(""))
                {
                    macAddressFound = true;
                }
                /*zdx = DataOperations.getIndex(fields, "installStatus");
                if(zdx != -1)
                {
                    String installStatus = rowData[zdx];
                    installStatus = FormValueUtil.checkNullValues(installStatus);
                    if(installStatus.equals("Installed") || installStatus.equals("Verified"))
                    {
                        values.add(inBoundConfig.getCompletenotelayerguid());
                    }
                    else if(installStatus.equals("Removed"))
                    {
                        values.add(inBoundConfig.getRemovednotelayerguid());
                    }
                    else
                    {
                        values.add(inBoundConfig.getNotcompletenotelayerguid());
                    }
                }*/


            }
            if(macAddressFound)
            {
                values.add(inBoundConfig.getCompletenotelayerguid());
            }
            else
            {
                values.add(inBoundConfig.getNotcompletenotelayerguid());
            }
            values.add(inBoundConfig.getFormtemplateguid());
            String geoZone = DataOperations.getValue(fields,"geozone_namespath",rowData);
            String geoZoneValues[] = geoZone.split("/");
            String noteBookName = "";
            String noteBookGUID = "";
            if(geoZoneValues.length > 2)
            {
                noteBookName = geoZoneValues[2].trim();
                noteBookGUID = slvDataQueryExecutor.getNoteBookGuid(noteBookName);
            }
            else
            {
                noteBookName = "City of London";
                noteBookGUID = slvDataQueryExecutor.getNoteBookGuid(noteBookName);
            }
            values.add(noteBookGUID);
            values.add("");
            values.add("");

            lstValues.add(values);
        }
        return lstValues;


    }
    @Override
    public void addNewDevices() {
        List<String[]> results = slvDataQueryExecutor.getNewDeviceList(inBoundConfig);
        String slvDataFields = inBoundConfig.getSlvquery();
        String []fields = slvDataFields.split(",");

        List<SLVFields> lstSLVFields = inBoundConfig.getSlvfields();
        List<String> headers = generateHeaders(fields,lstSLVFields);
        List<List<String>> lstvalues = generateValues(fields,lstSLVFields,results);
        try {
            InBoundFileUtils.createFolderIfNotExisits(ResourceDetails.INBOUND_FILE_STORE+ File.separator+InBoundFileUtils.generateTodayFolderName());

            String fileName = ResourceDetails.INBOUND_FILE_STORE+ File.separator+InBoundFileUtils.generateTodayFolderName() + File.separator + "new_devices.csv";

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
            headers.add("installStatus");
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

    public boolean applyChanges(JsonObject edgenoteJson,List<FormValues> formComponents,String[] rowData){
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
