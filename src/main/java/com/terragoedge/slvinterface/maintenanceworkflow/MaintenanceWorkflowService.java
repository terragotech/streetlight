package com.terragoedge.slvinterface.maintenanceworkflow;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.exception.NoDataChangeException;
import com.terragoedge.slvinterface.exception.SkipNoteException;
import com.terragoedge.slvinterface.maintenanceworkflow.model.DataDiffResponse;
import com.terragoedge.slvinterface.maintenanceworkflow.model.DataDiffValueHolder;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.service.EdgeService;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/*

streetlight.edge.Maintenance.formtemplate.guid // TODO

 */

public class MaintenanceWorkflowService {

    private Gson gson;
    private JsonParser jsonParser;
    private EdgeService edgeService;

    public MaintenanceWorkflowService() {
        gson = new Gson();
        jsonParser = new JsonParser();
    }

    final Logger logger = Logger.getLogger(MaintenanceWorkflowService.class);

    public void processMaintenanceWorkflow(EdgeNote edgeNote) throws NoDataChangeException, SkipNoteException {
        try {
            List<FormData> formDataList = edgeNote.getFormData();
            boolean isMaintenanceFormPresent = false;

            Map<String, List<Integer>> listMap = null; // TODO

            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.Maintenance.formtemplate.guid");
            logger.info("Swap Form Template Guid:" + formTemplateGuid);
            for (FormData formData : formDataList) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    isMaintenanceFormPresent = true;
                }

            }
            logger.info("Maintenance Form Template:" + isMaintenanceFormPresent);
            if (isMaintenanceFormPresent) {
                DataDiffResponse dataDiffResponse = compareRevisionData(edgeNote.getNoteGuid());
                if (dataDiffResponse != null) {
                    String actionType = getActionType(dataDiffResponse,listMap);
                    switch (actionType){
                        case "install":
                            break;
                        case "replacesmartcontroller":
                            break;
                        case "replaceledlight":
                            break;
                        case "replaceledlightsmartcontroller":
                            break;
                        case "remove":
                            break;
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Error in processMaintenanceWorkflow", e);
        }
    }


    /**
     * Call Edge REST Api to analyze data change. Checking data with Previous Revision. If no data change, then it
     * throws NoDataChangeException (Continue to Ameresco Workflow).SkipNoteException (Something Error in REST, so dont process this note.)
     *
     * @param noteGuid
     * @return
     * @throws NoDataChangeException
     * @throws SkipNoteException
     */
    private DataDiffResponse compareRevisionData(String noteGuid) throws NoDataChangeException, SkipNoteException {
        logger.info("Comparing data from the Previous Revision.");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.url.checkrevisiondata");
        String config = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.url.checkrevisiondata.config");
        JsonObject configJson = (JsonObject) jsonParser.parse(config);
        configJson.addProperty("noteGuid", noteGuid);
        logger.info("Given url is :" + url);
        // Compare Revision data to identify any changes or not.
        ResponseEntity<String> responseEntity = edgeService.serverCall(url, HttpMethod.POST, configJson.toString());
        // Success Response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            logger.info(responseBody);
            // Json to Java Object
            DataDiffResponse dataDiffResponse = gson.fromJson(responseBody, DataDiffResponse.class);
            // If status code, 404 either formtemplate not present or edgenote not present. So continue Ameresco worklfow
            if (dataDiffResponse.getStatusCode() == 404) {
                logger.info("Response Code: 404.");
                throw new NoDataChangeException(responseBody);
            } else if (dataDiffResponse.getStatusCode() == 500) {
                logger.info("Skip this note due to error");
                //  Something went wrong, Skip this note
                throw new SkipNoteException(responseBody);
            } else if (dataDiffResponse.getStatusCode() == 200) {
                // If there is not data change, then throws NoDataChangeException
                if (!dataDiffResponse.isChanged()) {
                    logger.info("No Data Changes with the Previous revision.");
                    throw new NoDataChangeException(responseBody);
                }
            }
            return dataDiffResponse;
        }
        return null;

    }

    private String getActionType(DataDiffResponse dataDiffResponse, Map<String, List<Integer>> listMap) {
        Set<String> keySets = listMap.keySet();
        List<DataDiffValueHolder> dataDiff = dataDiffResponse.getDataDiff();
        for (String actionType : keySets) {
            List<Integer> idList = listMap.get(actionType);
            for (Integer ids : idList) {
                DataDiffValueHolder temp = new DataDiffValueHolder();
                temp.setId(ids);
                int pos = dataDiff.indexOf(temp);
                if (pos != -1) {
                    return actionType;
                }
            }
        }
        return null;

    }


}
