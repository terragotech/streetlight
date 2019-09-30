package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.SLVFields;
import com.slvinterface.utils.FormValueUtil;
import com.slvinterface.utils.InBoundFileUtils;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrentInBoundInterface extends InBoundInterface {
    private EdgeRestService edgeRestService;

    public BrentInBoundInterface() throws Exception{
        edgeRestService = new EdgeRestService();
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
    private int getIndex(String []fields,String fieldName)
    {
        int result = -1;
        int fc = fields.length;
        for(int idx=0;idx<fc;idx++)
        {
            if(fields[idx].equals(fieldName))
            {
                result = idx;
                break;
            }
        }
        return result;
    }
    private String getValue(String []fields,String fieldName,String []rowData)
    {
        String result = "";
        int idx = getIndex(fields,fieldName);
        if(idx != -1)
        {
            result = rowData[idx];
            if(result == null)
            {
                result = "";
            }
        }
        return result;
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
                    int idx = getIndex(fields, macAddressField);
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
                    int idx = getIndex(fields, "idoncontroller");
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
                    int idx = getIndex(fields, cur.getSlvfield());
                    System.out.println(cur.getSlvfield());
                    if (idx != -1) {
                        if(rowData[idx] == null)
                        {
                            rowData[idx] = "";
                        }
                        values.add(rowData[idx]);
                    }
                }
            }
            values.add(getValue(fields,"name",rowData));
            values.add(getValue(fields,"lat",rowData));
            values.add(getValue(fields,"lng",rowData));
            if(macAddressFound)
            {
                values.add(inBoundConfig.getCompletenotelayerguid());
            }
            else
            {
                values.add(inBoundConfig.getNotcompletenotelayerguid());
            }
            values.add(inBoundConfig.getFormtemplateguid());
            String geoZone = getValue(fields,"geozonepath",rowData);
            String geoZoneValues[] = geoZone.split("/");
            String noteBookName = "";
            String noteBookGUID = "";
            if(geoZoneValues.length > 2)
            {
                 noteBookName = geoZoneValues[2];
                noteBookGUID = slvDataQueryExecutor.getNoteBookGuid(noteBookName);
            }
            values.add(noteBookGUID);
            values.add(getValue(fields,"luminaire_brand",rowData) + " | " +  getValue(fields,"power",rowData));
            values.add(getValue(fields,"address",rowData));

            lstValues.add(values);
        }
        return lstValues;


    }
    private String [] convertListToArray(List<String> lstData)
    {
        int tc = lstData.size();
        String []result = new String[tc];
        int idx = 0;
        for(String cur:lstData)
        {
            result[idx] = cur;
            idx = idx + 1;
        }
        return result;
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
            InBoundFileUtils.createFolderIfNotExisits(ResourceDetails.INBOUND_FILE_STORE+ File.separator+InBoundFileUtils.generateTodayFolderName());

            String fileName = ResourceDetails.INBOUND_FILE_STORE+ File.separator+InBoundFileUtils.generateTodayFolderName() + File.separator + "new_devices.csv";

            FileWriter outputfile = new FileWriter(fileName);
            CSVWriter writer = new CSVWriter(outputfile, ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String []csvheader = convertListToArray(headers);

            writer.writeNext(csvheader);
            for(List<String> cur: lstvalues)
            {
                String []csvvalues = convertListToArray(cur);
                writer.writeNext(csvvalues);
            }
            writer.close();
            if(lstvalues.size() > 0)
            {
                addDeviceToServer(fileName);
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
    private void processUpdate(String []rowData)
    {
        String title = rowData[0];
        String idoncontroller = rowData[1];
        String macAddress = rowData[2];
        System.out.println(title);
        System.out.println(title);
        String noteGUID = slvDataQueryExecutor.getCurrentNoteGUID(title);
        String noteJson = getNoteDetails(noteGUID);
        boolean mustUpdate = false;
        if(!noteJson.equals(""))
        {
            EdgeNote restEdgeNote = gson.fromJson(noteJson, EdgeNote.class);
            JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
            JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
            int size = serverForms.size();
            for (int i = 0; i < size; i++) {
                JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                //formDefJson = formDefJson.replaceAll("\\\\", "");
                //formDefJson = formDefJson.replace("u0026","\\u0026");
                List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                }.getType());
                if(formTemplate.equals(inBoundConfig.getFormtemplateguid()))
                {
                    mustUpdate = true;
                    /* Update here */
                    List<SLVFields> lstSLVChange = inBoundConfig.getSlvchangefields();
                    for(SLVFields cur: lstSLVChange)
                    {

                        if(cur.getSlvfield().equals("macaddress"))
                        {
                            int id = Integer.parseInt(cur.getId());
                            FormValueUtil.updateEdgeForm(formComponents,id,macAddress);
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
                if(mustUpdate) {
                    //Call Rest CAll to Update
                    updateNoteDetails(edgenoteJson.toString(),noteGUID,restEdgeNote.getEdgeNotebook().getNotebookGuid());
                }

        }
        else
        {

        }
    }
    private String getNoteDetails(String noteguid) {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notes/" + noteguid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String> requestEntity = edgeRestService.getRequest(urlNew,true,tokenString);
        if(requestEntity.getStatusCode() == HttpStatus.OK)
        {
            response = requestEntity.getBody();
        }
        return response;
    }
    private String updateNoteDetails(String noteJson,String noteGuid,String notebookGuid)
    {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String>  responseEntity = edgeRestService.putRequest(urlNew,noteJson,true,tokenString);
        if(responseEntity.getStatusCode() == HttpStatus.OK)
        {
            response = responseEntity.getBody();
        }
        return response;
    }
    public  void updateDevices()
    {
        List<String[]> results = slvDataQueryExecutor.getUpdatedDeviceList(inBoundConfig);
        int tc = results.size();
        for(int jdx=0;jdx<tc;jdx++)
        {
            String []rowData = results.get(jdx);
            processUpdate(rowData);
        }

    }
}
