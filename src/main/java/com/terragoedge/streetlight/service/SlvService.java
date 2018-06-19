package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.edgeserver.SlvData;
import com.terragoedge.edgeserver.SlvDataDub;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SlvService extends AbstractService {
    private String baseUrl;
    private JsonParser jsonParser;
    private Logger logger = Logger.getLogger(SlvService.class);
    private Gson gson;
    private StreetlightDao streetlightDao;
    //String FILEPATH = "./context_changes.csv.csv";
    //String FILEPATH = "./context_changes.csv";
    String FILEPATH = "/Users/Nithish/Documents/ameresco_usa_context_changes_19_june.csv";
    //String FILEPATH = "./resources/context_changes.csv";
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;


    public SlvService() {
        super();
        streetlightDao = new StreetlightDao();
        gson = new Gson();
        jsonParser = new JsonParser();
        baseUrl = "http://localhost:8080/";
        //baseUrl = PropertiesReader.getProperties().getProperty("http://localhost:8182/edgeServer/");
    }

    public List<SlvData> getSlvDataFromCSV() {
        List<SlvData> slvDataList = new ArrayList<SlvData>();
        try {
            fileReader = new FileReader(FILEPATH);
            bufferedReader = new BufferedReader(fileReader);
            String currentRow;
            boolean isFirst=false;
            while ((currentRow = bufferedReader.readLine()) != null) {
                if(isFirst) {
                    String values[] = currentRow.split(",");
                    SlvData slvData = new SlvData();
                    //slvData.setGuid(values[0]);
                    slvData.setTitle(values[0]);
                    slvData.setLocation(values[1]);
                    slvDataList.add(slvData);
                }
                isFirst=true;
            }
            System.out.println("Successfully Removed");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in start method : " + e.toString());
        } finally {
            closeBufferedReader(bufferedReader);
            closeFileReader(fileReader);
        }
        return slvDataList;
    }

    public void closeBufferedReader(BufferedReader bufferedReader) {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeFileReader(FileReader fileReader) {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*public void getUpdated(){
        List<String> noteTitles = new ArrayList<>();
        List<SlvData> slvDataList = streetlightDao.getSLVData();
        List<SlvDataDub> slvDataDubList = new ArrayList<>();
        List<SlvData> edgeDatas = streetlightDao.getNoteDetails(slvDataDubList);
        slvDataList.removeAll(edgeDatas);
        StringBuffer stringBuffer = new StringBuffer();
        for(SlvData slvData : slvDataList){
            SlvDataDub slvDataDub = new SlvDataDub();
            slvDataDub.setTitle(slvData.getTitle());
            stringBuffer.append(slvData.getTitle());
            stringBuffer.append(",");
            stringBuffer.append(slvData.getLocation());
            stringBuffer.append(",");
            int pos = slvDataDubList.indexOf(slvDataDub);
            if(pos != -1){
                slvDataDub =  slvDataDubList.get(pos);
                stringBuffer.append(slvDataDub.getLocation());
            }

            stringBuffer.append("\n");
        }
        stringBuffer.toString();
        FileOutputStream fos = null;
        try{
             fos = new FileOutputStream("./res.csv");
             fos.write(stringBuffer.toString().getBytes());
             fos.flush();
             fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }*/


    public void start() {
        List<SlvDataDub> slvDataDubList = new ArrayList<>();
        List<SlvData> slvDataList = getSlvDataFromCSV();



        List<SlvData> edgeDatas = streetlightDao.getNoteDetails();

        for(SlvData slvData : edgeDatas){
            SlvDataDub slvDataDub = new SlvDataDub(slvData.getTitle(),slvData.getGuid(),slvData.getLayerName());
            slvDataDubList.add(slvDataDub);
        }

        slvDataList.removeAll(edgeDatas);
        for (SlvData slvData : slvDataList) {
            SlvDataDub slvDataDubTemp = new SlvDataDub(slvData.getTitle(),null,null);
            int pos =  slvDataDubList.indexOf(slvDataDubTemp);
            if(pos != -1){
                slvDataDubTemp = slvDataDubList.get(pos);
            }

            ResponseEntity<String> response = getNoteDetails(baseUrl, slvDataDubTemp.getGuid());
            if (response.getStatusCode().is2xxSuccessful()) {
                String noteJson = response.getBody();
                // Gson to Edge Note
                JsonObject edgeNote = (JsonObject) jsonParser.parse(noteJson);
                JsonArray edgeFormJsonArray = edgeNote.get("formData").getAsJsonArray();



                // Process Form Data
                int size = edgeFormJsonArray.size();
                for (int i = 0; i < size; i++) {
                    JsonObject edgeForm = edgeFormJsonArray.get(i).getAsJsonObject();
                    String formTemplateGuid = edgeForm.get("formTemplateGuid").getAsString();
                    String formDefJson = edgeForm.get("formDef").getAsString();
                    formDefJson = formDefJson.replace("\\\\", "");
                    List<EdgeFormData> edgeFormDatas = getEdgeFormData(formDefJson);
                    if (formTemplateGuid.equals("9d6a41fb-49ca-4e53-952c-18715a74faf6") || formTemplateGuid.equals("c8acc150-6228-4a27-bc7e-0fabea0e2b93")) {//Existing fixture information and install and maintenance form
                        updateFormValues(edgeFormDatas, "Proposed context", slvData.getLocation());
                    }
                    edgeForm.add("formDef", gson.toJsonTree(edgeFormDatas));
                    edgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                }
                edgeNote.addProperty("locationDescription", slvData.getLocation() +" | "+slvDataDubTemp.getLayerName());
                edgeNote.add("formData", edgeFormJsonArray);
                edgeNote.addProperty("createdDateTime", System.currentTimeMillis());
                edgeNote.addProperty("createdBy", "admin");
                String currentNoteguid = edgeNote.get("noteGuid").getAsString();
                JsonObject jsonObject = streetlightDao.getNotebookGuid(currentNoteguid);
                String notebookGuid = jsonObject.get("notebookguid").getAsString();
                if (slvData.getLocation().contains("Residential") || slvData.getLocation().contains("Alley")) {
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if (!notebookName.contains("Residential")) {
                        notebookName = notebookName + " Residential";// create notebook
                        notebookGuid =  createNotebook(notebookName, notebookGuid, currentNoteguid);
                    }
                } else {
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if (notebookName.contains("Residential")) {
                        notebookName = notebookName.replace("Residential", "");// create notebook
                        notebookName =  notebookName.trim();
                        notebookGuid =  createNotebook(notebookName, notebookGuid, currentNoteguid);
                    }
                }
                updateServer(edgeNote.toString(), notebookGuid, currentNoteguid, baseUrl);
            }
        }

    }

    private void updateFormValues(List<EdgeFormData> edgeFormDatas, String label, String expected) {
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setLabel(label);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        if (pos != -1) {
            String value = edgeFormDatas.get(pos).getValue();
            if (value != null && !value.equals("") && !value.equals(expected)) {
                edgeFormDatas.get(pos).setValue(label+"#"+expected);
            }
        }
    }

    private List<SlvData> getSlvDatas(String[] titles, String[] locations) {
        List<SlvData> slvDataList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            SlvData slvData = new SlvData();
            slvData.setLocation(locations[i]);
            slvData.setTitle(titles[i]);
            slvDataList.add(slvData);
        }
        return slvDataList;
    }

    private String createNotebook(String notebookName, String notebookGuid, String currentNoteguid) {
        String edgeNotebookGuid =  streetlightDao.getNotebookByName(notebookName);
        if(edgeNotebookGuid == null){
           FullEdgeNotebook edgeNotebook = streetlightDao.getNotebook(notebookGuid);
            if (edgeNotebook != null) {
                edgeNotebook.setLastUpdatedTime(System.currentTimeMillis());
                edgeNotebook.setNotebookName(notebookName);
                edgeNotebook.getForms().add("c8acc150-6228-4a27-bc7e-0fabea0e2b93");
                edgeNotebook.getForms().add("4c645aab-a78d-411a-a383-eb111fb65b20");
                return createNotebook(baseUrl, edgeNotebook);
            } else {
                logger.info(currentNoteguid + " -> notebook not present");
            }
            return null;
        }else{
            return edgeNotebookGuid;
        }

    }
}
