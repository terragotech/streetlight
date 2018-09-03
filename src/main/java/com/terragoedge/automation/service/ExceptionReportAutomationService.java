package com.terragoedge.automation.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.EdgeReportDAO;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.entity.InventoryReport;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.service.AbstractService;
import com.terragoedge.slvinterface.service.EdgeService;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExceptionReportAutomationService extends EdgeService {
    private InventoryDAO inventoryDAO;
    private ConnectionDAO connectionDAO;
    private EdgeReportDAO edgeReportDAO;
    private Gson gson;
    private Map<String, String> users = new HashMap<>();
    public ExceptionReportAutomationService() {
        inventoryDAO = InventoryDAO.INSTANCE;
        connectionDAO = ConnectionDAO.INSTANCE;
        edgeReportDAO = EdgeReportDAO.INSTANCE;
        gson = new Gson();
        users = getAllUsers();
    }

    public void start(){
        JsonArray jsonArray = readInputFile();
        int index = 0;
        List<String> results = new ArrayList<>();
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = (JsonObject) jsonElement;
            if(jsonObject != null){
                String line = jsonObject.get("line").getAsString();
                if(index == 0){
                    results.add(line+",Status");
                }else {
                    String macAddress = jsonObject.get("macaddress").getAsString();
                    String workflow = jsonObject.get("workflow").getAsString();
                    List<String> destinationLocations = new ArrayList<>();
                    List<String> currentLocations = new ArrayList<>();
                    List<String> selectedLocations = new ArrayList<>();
                    List<InventoryReport> inventoryReports = edgeReportDAO.getReportDetails(macAddress);
                    for (InventoryReport inventoryReport : inventoryReports) {
                        String processingGuid = inventoryReport.getProcessingnoteguid();
                        String sourceGuid = inventoryReport.getSourcenoteguid();
                        currentLocations.add(getCurrentLocation(processingGuid));
                        ExceptionLocation exceptionLocation = getSelectedLocation(workflow,sourceGuid, jsonObject.get("username").getAsString());
                        selectedLocations.addAll(exceptionLocation.getSelectedLoc());
                        destinationLocations.addAll(exceptionLocation.getDestinationLoc());

                    }
                    boolean isSuccess = true;
                    if (!destinationLocations.contains(jsonObject.get("destination").getAsString())) {
                        isSuccess = false;
                    }
                    if (!currentLocations.contains(jsonObject.get("current").getAsString())) {
                        isSuccess = false;
                    }
                    if (!selectedLocations.contains(jsonObject.get("selected").getAsString())) {
                        isSuccess = false;
                    }
                    results.add(line+","+isSuccess);
                    System.out.println(inventoryReports);
                }
            }
            index++;
        }
        if(results.size() > 0){
            writeStatus(results);
        }
    }

    private String getInstallNotebookName(EdgeNoteView edgeNoteView){
        String notebookId = edgeNoteView.getNotebookid();
        EdgeNotebookEntity edgeNotebookEntity = connectionDAO.getEdgeNotebookEntity(notebookId);
        if(edgeNotebookEntity != null){
            return edgeNotebookEntity.getNotebookName();
        }
        return "";
    }

    private String getInventoryNotebookName(EdgeNoteView edgeNoteView){
        String notebookId = edgeNoteView.getNotebookid();
        EdgeNotebookEntity edgeNotebookEntity = inventoryDAO.getEdgeNotebookEntity(notebookId);
        if(edgeNotebookEntity != null){
            return edgeNotebookEntity.getNotebookName();
        }
        return "";
    }

    private JsonArray readInputFile(){
        File file = new File("/Users/ram/Desktop/report/exception.csv");
        JsonArray jsonArray = new JsonArray();
        if(file.exists()){
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String str;
                while((str = bufferedReader.readLine()) != null){
                        String[] arr = str.split(",");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("current", arr[1]);
                        jsonObject.addProperty("destination", arr[3]);
                        jsonObject.addProperty("selected", arr[2]);
                        jsonObject.addProperty("macaddress", arr[0]);
                        jsonObject.addProperty("workflow", arr[6]);
                        jsonObject.addProperty("line", str);
                        jsonObject.addProperty("username", arr[5]);
                        jsonArray.add(jsonObject);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    private void writeStatus(List<String> lines){
        File file = new File("/Users/ram/Desktop/report/exception_result.csv");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
            for(String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getDestinationLocation(String macAddress){
        EdgeNoteView edgeNoteView = inventoryDAO.getEdgeNoteViewFromTitle(macAddress);
        if (edgeNoteView != null) {
            return getInventoryNotebookName(edgeNoteView);
        }
        return "";
    }

    private String getCurrentLocation(String processingGuid){
        EdgeNoteView edgeNoteView1 = inventoryDAO.getEdgeNoteView(processingGuid);
        if (edgeNoteView1 != null) {
            return getInventoryNotebookName(edgeNoteView1);
        }
        return "";
    }

    private ExceptionLocation getSelectedLocation(String workflow, String sourceGuid,String username){
        List<String> selectedLocations = new ArrayList<>();
        List<String> destinations = new ArrayList<>();
        if(workflow.equals("Load for Assignment") || workflow.equals("Return to Stock") || workflow.equals("Disposition")) {
            List<EdgeFormEntity> edgeFormEntities = connectionDAO.getFormDef(sourceGuid);
                for (EdgeFormEntity formData : edgeFormEntities) {
                    if (formData.getName().equals(workflow)) {
                        List<EdgeFormData> edgeFormDataList = gson.fromJson(formData.getFormDef(), new TypeToken<List<EdgeFormData>>(){}.getType());
                        String destination = "";
                        String label = "";
                        switch (formData.getName()) {
                            case "Load for Assignment":
                                label = "Loading from Reporting Center";
                                destination = "To Installer";
                                selectedLocations.add(AbstractService.getValueByTitle(edgeFormDataList, label));
                                break;
                            case "Return to Stock":
                                selectedLocations.add(users.get(username));
                                destination = "Returning to Reporting Center";
                                break;
                            case "Disposition":
                                selectedLocations.add(users.get(username));
                                destination = "Disposing at Reporting Center";
                                break;
                        }

                        destinations.add(AbstractService.getValueByTitle(edgeFormDataList, destination));
                    }
                }
        }else if(workflow.equals("InventoryHandlingWorkFlow")){
            List<EdgeFormEntity> edgeFormEntities = inventoryDAO.getFormDef(sourceGuid);
                    for (EdgeFormEntity formData : edgeFormEntities) {
                        List<EdgeFormData> edgeFormDataList = gson.fromJson(formData.getFormDef(),new TypeToken<List<EdgeFormData>>(){}.getType());
                        selectedLocations.add(AbstractService.getValueById(edgeFormDataList, 8));
                        selectedLocations.add(AbstractService.getValueById(edgeFormDataList, 32));
                        selectedLocations.add(AbstractService.getValueById(edgeFormDataList, 27));
                        selectedLocations.add(AbstractService.getValueById(edgeFormDataList, 33));

                        destinations.add(AbstractService.getValueById(edgeFormDataList,9));
                        destinations.add(AbstractService.getValueById(edgeFormDataList,13));
                        destinations.add(AbstractService.getValueById(edgeFormDataList,28));
                        destinations.add(AbstractService.getValueById(edgeFormDataList,30));
                    }
        }
        ExceptionLocation exceptionLocation = new ExceptionLocation();
        exceptionLocation.setSelectedLoc(selectedLocations);
        exceptionLocation.setDestinationLoc(destinations);
        return exceptionLocation;
    }

    protected Map<String,String> getAllUsers() {
        Map<String,String> usersMap = new HashMap<>();
        String urlNew =  "https://streetlights.comedinstall.com/" + "/rest/users";
        List<User> users = new ArrayList<>();
        ResponseEntity<String> responseEntity =  serverCall(urlNew,HttpMethod.GET,null);
        if(responseEntity.getStatusCode().is2xxSuccessful()){
            String response =   responseEntity.getBody();
            users = gson.fromJson(response,new TypeToken<List<User>>(){}.getType());
        }
        for(User user : users){
            usersMap.put(user.getUserName(),"User-"+(user.getFirstName() == null ? "" : user.getFirstName()) +" "+(user.getLastName() == null ? "" : user.getLastName()));
        }
        return usersMap;
    }
}
