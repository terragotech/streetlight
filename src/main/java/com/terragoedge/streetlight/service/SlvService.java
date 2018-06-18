package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.edgeserver.SlvData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
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
    String FILEPATH = "./src/main/resources/context_changes.csv";
    //String FILEPATH = "./resources/context_changes.csv";
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;


    public SlvService() {
        super();
        streetlightDao = new StreetlightDao();
        gson = new Gson();
        jsonParser = new JsonParser();
        baseUrl = PropertiesReader.getProperties().getProperty("http://localhost:8182/edgeServer/");
    }

    public List<SlvData> getSlvDataFromCSV(List<String> noteTitles) {
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
                    slvData.setGuid(values[0]);
                    noteTitles.add(values[1]);
                    slvData.setTitle(values[1]);
                    slvData.setLocation(values[2]);
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


    public void start() {
        List<String> noteTitles = new ArrayList<>();
        List<SlvData> slvDataList = getSlvDataFromCSV(noteTitles);
        List<SlvData> edgeDatas = streetlightDao.getNoteDetails(noteTitles);
        edgeDatas.removeAll(slvDataList);
        for (SlvData slvData : edgeDatas) {
            ResponseEntity<String> response = getNoteDetails(baseUrl, slvData.getGuid());
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
                edgeNote.addProperty("locationdescription", slvData.getLocation());
                edgeNote.add("formData", edgeFormJsonArray);
                edgeNote.addProperty("createdDateTime", System.currentTimeMillis());
                edgeNote.addProperty("createdBy", "admin");
                String currentNoteguid = edgeNote.get("noteGuid").getAsString();
                JsonObject jsonObject = streetlightDao.getNotebookGuid(currentNoteguid);
                String notebookGuid = jsonObject.get("notebookguid").getAsString();
                if (slvData.getLocation().contains("Residential")) {
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if (!notebookName.contains("Residential")||!notebookName.contains("alley")) {
                        notebookName = notebookName + " Residential";// create notebook
                        createNotebook(notebookName, notebookGuid, currentNoteguid);
                    }
                } else {
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if (notebookName.contains("Residential")) {
                        notebookName = notebookName.replace("Residential", "");// create notebook
                        createNotebook(notebookName, notebookGuid, currentNoteguid);
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
                edgeFormDatas.get(pos).setValue(expected);
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

    private void createNotebook(String notebookName, String notebookGuid, String currentNoteguid) {
        FullEdgeNotebook edgeNotebook = streetlightDao.getNotebook(notebookGuid);
        if (edgeNotebook != null) {
            edgeNotebook.setLastupdatedtime(System.currentTimeMillis());
            edgeNotebook.setNotebookname(notebookName);
            CreateNotebook(baseUrl, edgeNotebook);
        } else {
            logger.info(currentNoteguid + " -> notebook not present");
        }
    }
}
