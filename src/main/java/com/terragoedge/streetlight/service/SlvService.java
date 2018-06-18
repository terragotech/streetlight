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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SlvService extends AbstractService{
    private String baseUrl;
    private JsonParser jsonParser;
    private Logger logger = Logger.getLogger(SlvService.class);
    private Gson gson;
    private StreetlightDao streetlightDao;
    public SlvService() {
        super();
        streetlightDao = new StreetlightDao();
        gson = new Gson();
        jsonParser = new JsonParser();
        baseUrl = PropertiesReader.getProperties().getProperty("http://localhost:8182/edgeServer/");
    }
    public void start(){
        String[] noteTitles = {"67933","93251","221535","107162","222814","109749","156353","237899","288248","33268","174337","219485",
        "321553","212045","163832","224108","247860","156631","26824","247043","283238","313583","230843","9094","326552","826606","166270",
        "1063825","1063827","1063035","259450","1064219","195117","269380","115026","75503","163002","308986","268031","245391","209129","285002",
        "1063832","108962","1064221","1061432","58310","28170","230844","1061434","205253","72967","242921","1063040","81024","1122623","31561","61517",
        "263679","186305","35705",
                "326113",
                "292490",
                "117538",
                "302253",
                "112285",
                "1123423",
                "1122624",
                "283239",
                "135022",
                "263683",
                "273253"};
        String[] locations = {
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "6 - Arterial (Feeder) Legacy (66 Ft ROW",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "Node Only",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "X - Out of Scope",
                "X - Out of Scope",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROW Staggered)",
                "4 - Residential Coach (Retrofit)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "4 - Residential Coach (Retrofit)",
                "1 - Residential Legacy (66 Ft ROW One-Sided)",
                "Node Only",
                "3 - Residential Modern (66ft ROWStaggered)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "14 - Viaduct",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "1 - Residential Legacy (66 Ft ROW One-Sided)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "1 - Residential Legacy (66 Ft ROW One-Sided)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "1 - Residential Legacy (66 Ft ROW One-Sided)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "5 - Residential Intersection (66 Ft ROW 45 Deg Angle)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
                "4 - Residential Coach (Retrofit)",
                "3 - Residential Modern (66ft ROWStaggered)",
        };
        List<SlvData> edgeDatas = streetlightDao.getNoteDetails(noteTitles);
        List<SlvData> slvDatas = getSlvDatas(noteTitles,locations);
        edgeDatas.removeAll(slvDatas);
        for(SlvData slvData : edgeDatas){
            ResponseEntity<String> response = getNoteDetails(baseUrl,slvData.getGuid());
            if(response.getStatusCode().is2xxSuccessful()){
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
                if(slvData.getLocation().contains("Residential")){
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if(!notebookName.contains("Residential")){
                        notebookName = notebookName+" Residential";// create notebook
                        createNotebook(notebookName,notebookGuid,currentNoteguid);
                    }
                }else{
                    String notebookName = jsonObject.get("notebookname").getAsString();
                    if(notebookName.contains("Residential")){
                        notebookName = notebookName.replace("Residential","");// create notebook
                        createNotebook(notebookName,notebookGuid,currentNoteguid);
                    }
                }
                updateServer(edgeNote.toString(), notebookGuid, currentNoteguid, baseUrl);
            }
        }

    }

    private void updateFormValues(List<EdgeFormData> edgeFormDatas, String label,String expected){
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setLabel(label);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        if(pos != -1){
            String value = edgeFormDatas.get(pos).getValue();
            if(value != null && !value.equals("") && !value.equals(expected)){
                edgeFormDatas.get(pos).setValue(expected);
            }
        }
    }

    private List<SlvData> getSlvDatas(String[] titles,String[] locations){
        List<SlvData> slvDataList = new ArrayList<>();
        for(int i=0;i<titles.length;i++){
            SlvData slvData = new SlvData();
            slvData.setLocation(locations[i]);
            slvData.setTitle(titles[i]);
            slvDataList.add(slvData);
        }
        return slvDataList;
    }
    private void createNotebook(String notebookName,String notebookGuid,String currentNoteguid){
        FullEdgeNotebook edgeNotebook = streetlightDao.getNotebook(notebookGuid);
        if(edgeNotebook!= null) {
            edgeNotebook.setLastupdatedtime(System.currentTimeMillis());
            edgeNotebook.setNotebookname(notebookName);
            CreateNotebook(baseUrl, edgeNotebook);
        }else{
            logger.info(currentNoteguid + " -> notebook not present");
        }
    }
}
