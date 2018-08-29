package com.terragoedge.automation.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.EdgeReportDAO;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.entity.InventoryReport;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExceptionReportAutomationService {
    private InventoryDAO inventoryDAO;
    private ConnectionDAO connectionDAO;
    private EdgeReportDAO edgeReportDAO;
    public ExceptionReportAutomationService() {
        inventoryDAO = InventoryDAO.INSTANCE;
        connectionDAO = ConnectionDAO.INSTANCE;
        edgeReportDAO = EdgeReportDAO.INSTANCE;
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
                    String macAddress = "";
                    String destinationLocation = "";
                    List<String> currentLocations = new ArrayList<>();
                    EdgeNoteView edgeNoteView = connectionDAO.getEdgeNoteViewFromTitle(macAddress);
                    if (edgeNoteView != null) {
                        destinationLocation = getNotebookName(edgeNoteView);
                    }
                    List<InventoryReport> inventoryReports = edgeReportDAO.getReportDetails(macAddress);
                    for (InventoryReport inventoryReport : inventoryReports) {
                        String processingGuid = inventoryReport.getProcessingnoteguid();
                        EdgeNoteView edgeNoteView1 = connectionDAO.getEdgeNoteView(processingGuid);
                        if (edgeNoteView1 != null) {
                            currentLocations.add(getNotebookName(edgeNoteView1));
                        }
                    }
                    boolean isSuccess = true;
                    if (!destinationLocation.equals(jsonObject.get("destination").getAsString())) {
                        isSuccess = false;
                    }
                    if (!currentLocations.contains(jsonObject.get("current").getAsString())) {
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

    private String getNotebookName(EdgeNoteView edgeNoteView){
        String notebookId = edgeNoteView.getNotebookid();
        EdgeNotebookEntity edgeNotebookEntity = connectionDAO.getEdgeNotebookEntity(notebookId);
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
                        jsonObject.addProperty("destination", arr[2]);
                        jsonObject.addProperty("selected", arr[3]);
                        jsonObject.addProperty("line", str);
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
}
