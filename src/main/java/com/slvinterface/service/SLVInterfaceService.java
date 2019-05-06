package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.exception.DatabaseException;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract  class SLVInterfaceService {

    private static final Logger logger = Logger.getLogger(SLVInterfaceService.class);

    EdgeRestService edgeRestService = null;
    JsonParser jsonParser = null;
    QueryExecutor queryExecutor = null;
    Gson gson = null;
    ConditionsJson conditionsJson = null;

    SLVInterfaceService() throws Exception{
        edgeRestService = new EdgeRestService();
        jsonParser = new JsonParser();
        queryExecutor = new QueryExecutor();
        gson = new Gson();
    }

    public void run(){
        String accessToken = edgeRestService.getEdgeToken();
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }


        try {
            conditionsJson = getConditionsJson();
        } catch (Exception e) {
            logger.error("Unable to load Configuration file.", e);
            return;
        }

        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = edgeRestService.getRequest(edgeSlvUrl, false, accessToken);

        // Process only response code as success
        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();

            JsonArray noteGuidsJsonArray = (JsonArray)jsonParser.parse(notesGuids);
            if(noteGuidsJsonArray != null &&  !noteGuidsJsonArray.isJsonNull()){
                for(JsonElement noteGuidJson : noteGuidsJsonArray){
                    String noteGuid = noteGuidJson.getAsString();
                }
            }
        }
    }


    private void run(String noteGuid,String accessToken)throws DatabaseException{
        try{
            SLVSyncTable slvSyncTable = queryExecutor.getSLSyncTable(noteGuid);
            if(slvSyncTable != null){
                logger.info("Current NoteGuid ["+noteGuid+"] is already Processed.");
                return;
            }
        }catch (Exception e){
            throw new DatabaseException(e);
        }

        try{
            SLVSyncTable slvSyncTable = new SLVSyncTable();
            slvSyncTable.setNoteGuid(noteGuid);
            slvSyncTable.setProcessedDateTime(System.currentTimeMillis());

            String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

            url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

            url = url + "/" +noteGuid;
            logger.info("Given url is :" + url);


            // Get NoteList from edgeserver
            ResponseEntity<String> responseEntity = edgeRestService.getRequest(url, false, accessToken);

            // Process only response code as success
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String notesData = responseEntity.getBody();
            }else{
                slvSyncTable.setErrorDetails("Unable to Get Note Details from Edge Server.");
            }

        }catch (Exception e){
            logger.error("Error in run",e);
        }

    }

    private void processNoteData(String notesData,SLVSyncTable slvSyncTable){
        try {
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            populateSLVSyncTable(edgeNote,slvSyncTable);
        }catch (Exception e){
            logger.error("Error in processNoteData",e);
        }
    }

    /**
     * Populate Note Details to SLVSyncTable.
     * @param edgeNote
     * @param slvSyncTable
     */
    private void populateSLVSyncTable(EdgeNote edgeNote,SLVSyncTable slvSyncTable){
        slvSyncTable.setNoteName(edgeNote.getTitle());
        slvSyncTable.setNoteCreatedBy(edgeNote.getCreatedBy());
        slvSyncTable.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());
        slvSyncTable.setSyncTime(edgeNote.getSyncTime());
    }


    /**
     * Get List of processable FormData(FormTemplateGuid configured in properties file)
     * @param edgeNote
     * @return
     */
    private List<FormData> getFormDataList(EdgeNote edgeNote){
        List<FormData> formDataList =  edgeNote.getFormData();
        List<FormData> formDataRes = new ArrayList<>();
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplate.guid");
        for(FormData formData : formDataList){
            if(formData.getFormTemplateGuid().equals(formTemplateGuid)){
                formDataRes.add(formData);
            }
        }
        return formDataRes;
    }


    /**
     * Get Configuration Details
     * @return
     * @throws Exception
     */
    public ConditionsJson getConditionsJson() throws Exception {
        FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH);
        String configjson = jsonParser.parse(reader).toString();

        ConditionsJson configs = gson.fromJson(configjson, ConditionsJson.class);
        return configs;


    }


    protected String valueById(List<FormValues>  formValuesList, int id) throws NoValueException {
        FormValues edgeFormTemp = new FormValues();
        edgeFormTemp.setId(id);

        int pos = formValuesList.indexOf(edgeFormTemp);
        if (pos != -1) {
            FormValues formValues = formValuesList.get(pos);
            String value = formValues.getValue();
            logger.info("edgeFormData value:" + value);
            if (value == null || value.trim().isEmpty() || value.contains("null") || value.equals("null")) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

    public boolean checkMacAddressExists(String macAddress, String idOnController)
            throws QRCodeAlreadyUsedException,Exception {

        boolean isExistMacAddress = queryExecutor.isExistMacAddress(idOnController, macAddress);
        if(isExistMacAddress){
            throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already processed.", macAddress);
        }
        logger.info("Getting Mac Address from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("attribute=MacAddress");
        paramsList.add("value=" + macAddress);
        paramsList.add("operator=eq-i");
        paramsList.add("recurse=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        System.out.println("Url :"+url);
        ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("-------MAC Address----------");
            logger.info(responseString);
            logger.info("-------MAC Address End----------");
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        // return false;
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
            }
            throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already Used in following devices [" + stringBuilder.toString() + "]", macAddress);
        } else {
            throw new QRCodeAlreadyUsedException("Error while getting data from SLV.", macAddress);
        }

    }
}
