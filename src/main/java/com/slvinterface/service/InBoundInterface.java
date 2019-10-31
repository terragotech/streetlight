package com.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.slvinterface.dao.SLVDataQueryExecutor;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.InBoundConfig;
import com.slvinterface.model.HistoryModel;
import com.slvinterface.model.SLVDataInfo;
import com.slvinterface.utils.DataOperations;
import com.slvinterface.utils.FormValueUtil;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


abstract public class InBoundInterface {
    protected SLVDataQueryExecutor slvDataQueryExecutor = null;
    protected InBoundConfig inBoundConfig = null;
    protected EdgeRestService edgeRestService;
    Gson gson = null;
    public InBoundInterface() throws Exception{
        edgeRestService = new EdgeRestService();
        slvDataQueryExecutor = new SLVDataQueryExecutor();
        FileReader reader = new FileReader(ResourceDetails.INBOUND_CONFIG_JSON);
        JsonParser jsonParser = new JsonParser();
        String configjson = jsonParser.parse(reader).toString();
        gson = new Gson();
        inBoundConfig = gson.fromJson(configjson, InBoundConfig.class);
        if(inBoundConfig.getSlvquery() == null)
        {
            throw new Exception("Bad Config");
        }
        if(inBoundConfig.getSlvquery() != null)
        {
            if(inBoundConfig.getSlvquery().equals(""))
            {
                throw new Exception("Bad Config");
            }
        }
    }
    public void processUpdate(String []rowData, CSVWriter writer)
    {
        String idoncontroller = rowData[0];
        String name = rowData[1];
        String name_y = rowData[2];
        String macAddress = rowData[3];
        String macAddress_y = rowData[4];
        String installStatus = rowData[5];
        List<String> lstData = new ArrayList<>();
        lstData.add(idoncontroller);
        lstData.add(name);
        lstData.add(name_y);
        if(macAddress == null)
        {
            macAddress = "";
        }
        if(macAddress_y == null)
        {
            macAddress_y = "";
        }
        lstData.add(macAddress);
        lstData.add(macAddress_y);
        lstData.add(installStatus);
        String terragoSLVUserName = PropertiesReader.getProperties().getProperty("streetlight.slv.username");
        if(!macAddress.equals(macAddress_y))
        {
            HistoryService historyService = new HistoryService();
            HistoryModel historyModel = new HistoryModel();
            idoncontroller = idoncontroller.replaceAll(" ","%20");
            historyModel.setIdOnController(idoncontroller);
            historyService.process(historyModel);
            if(historyModel.getUser() != null)
            {
                if(!historyModel.getUser().equals(terragoSLVUserName))
                {
                    lstData.add("true");
                }
                else
                {
                    lstData.add("false");
                }
            }
            else
            {
                lstData.add("true");
            }

        }
        else
        {
            lstData.add("false");
        }
        String []dataValues = DataOperations.convertListToArray(lstData);
        writer.writeNext(dataValues);



    }
    public void startProcessing() throws Exception{
        ImportSLVData importSLVData = new ImportSLVData();
        if(PropertiesReader.getProperties().getProperty("importToDB").equals("true"))
        {
            SLVDataInfo slvDataInfo = importSLVData.startImport();
            importSLVData.loadToDatabase(slvDataInfo);
        }
        addNewDevices();
        deleteDevices();
        updateDevices();
    }

    public String getNoteDetails(String noteguid) {
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
    public String updateNoteDetails(String noteJson,String noteGuid,String notebookGuid)
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
    public String addDeviceToServer(String fileName)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file",new FileSystemResource(new File(fileName)));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        String serverUrl = "http://localhost:8080/" + "import/csv";

        org.springframework.web.client.RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
        return response.getBody();

    }
    abstract public  void addNewDevices();
    abstract public  void deleteDevices();
    abstract public  void updateDevices();
    abstract public boolean applyChanges(JsonObject edgenoteJson,List<FormValues> formComponents,String[] rowData);
    abstract public String getNoteGUID(String idoncontroller, String title,String formfield);
    public void updateNotes(String pathToCsv){
        BufferedReader csvReader = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row = null;
            boolean headerProcessed = false;
            while ((row = csvReader.readLine()) != null) {
                if (headerProcessed) {
                    File f = new File("./stop.txt");
                    if (f.exists()) {
                        break;
                    }
                    String[] rowData = row.split(",");
                    String idoncontroller = rowData[0];
                    String name = rowData[1];

                    String noteGUID = getNoteGUID(idoncontroller,name,inBoundConfig.getFormkeyfield());
                    String noteJson = "";
                        if(!noteGUID.equals("")) {
                            noteJson = getNoteDetails(noteGUID);
                        }
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
                                List<FormValues> formComponents = null;
                                try {
                                    formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                                    }.getType());
                                }
                                catch (Exception e)
                                {
                                    File file = new File("./not_processed_list.txt");
                                    FileWriter fr = new FileWriter(file, true);
                                    Date dt = new Date();
                                    fr.write(dt + " " + name + "\r\n");
                                    fr.close();
                                    continue;
                                }
                                if (formTemplate.equals(inBoundConfig.getFormtemplateguid())) {
                                    mustUpdate = applyChanges(edgenoteJson, formComponents, rowData);
                                }


                                serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                            }
                            edgenoteJson.add("formData", serverForms);
                            edgenoteJson.addProperty("createdBy", "slvinterface");
                            long ntime = System.currentTimeMillis();

                            edgenoteJson.addProperty("createdDateTime", ntime);
                            if (mustUpdate) {
                                //Call Rest CAll to Update
                                updateNoteDetails(edgenoteJson.toString(), noteGUID, restEdgeNote.getEdgeNotebook().getNotebookGuid());
                                File file = new File("./processed_list.txt");
                                FileWriter fr = new FileWriter(file, true);
                                Date dt = new Date();
                                fr.write(dt + " " + name + "\r\n");
                                fr.close();
                            }

                        } else {
                            File file = new File("./not_processed_list.txt");
                            FileWriter fr = new FileWriter(file, true);
                            Date dt = new Date();
                            fr.write(dt + " " + name + "\r\n");
                            fr.close();
                        }

                } else {
                    headerProcessed = true;
                }
            }

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean doTitleUpdate(JsonObject edgenoteJson,String name,String name_y)
    {
        boolean result = false;
        if (!name.equals(name_y)) {
            String title = edgenoteJson.get("title").getAsString();
            edgenoteJson.addProperty("title", name);
            result = true;
        }
        return result;
    }
    public void replaceUpdate(List<FormValues> formComponents,String existingMacAddress,String macAddress){
        int previnstall_id = Integer.parseInt(inBoundConfig.getPrevmacaddress_id());
        int replace_id = Integer.parseInt(inBoundConfig.getReplacemacaddress_id());




        //Update values
        FormValueUtil.updateEdgeForm(formComponents,previnstall_id,existingMacAddress);
        FormValueUtil.updateEdgeForm(formComponents,replace_id,macAddress);

        //Set Form action
        int action_id = Integer.parseInt(inBoundConfig.getActionfield_id());
        String actionValue = inBoundConfig.getReplaceActionValue();
        FormValueUtil.updateEdgeForm(formComponents,action_id,actionValue);

        /*int layer_action_id = Integer.parseInt(inBoundConfig.getFormlayer_id());
        String formlayer_action1 = inBoundConfig.getFormlayer_replace();
        FormValueUtil.updateEdgeForm(formComponents,layer_action_id,formlayer_action1);*/
    }
    public void doMacAddressUpdate(String macAddressUpdateStatus,String macAddress,String macAddress_y,List<FormValues> formComponents,String installStatus) {
        if (macAddressUpdateStatus.equals("true"))
        {
            if (macAddress_y.equals("") && !macAddress.equals("")) {
                //Install
                System.out.println("Install Section");
                int id = Integer.parseInt(inBoundConfig.getInstallmacaddress_id());
                String existingMacAddress = FormValueUtil.getValue(formComponents, id);
                if (existingMacAddress.equals("")) {
                    //Update fields
                    FormValueUtil.updateEdgeForm(formComponents, id, macAddress);

                    //Set Action Values
                    int action_id = Integer.parseInt(inBoundConfig.getActionfield_id());
                    String actionValue1 = inBoundConfig.getInstallActionValue();
                    FormValueUtil.updateEdgeForm(formComponents, action_id, actionValue1);


                    //Set Form Action Values
                    /*int layer_action_id = Integer.parseInt(inBoundConfig.getFormlayer_id());
                    String formlayer_action1 = inBoundConfig.getFormlayer_install();
                    FormValueUtil.updateEdgeForm(formComponents, layer_action_id, formlayer_action1);*/


                } else {
                    //Do Replace work flow
                    replaceUpdate(formComponents, existingMacAddress, macAddress);


                }

            } else if (!macAddress_y.equals("") && macAddress.equals("")) {
                //Remove
                System.out.println("Remove");
                int id = Integer.parseInt(inBoundConfig.getInstallmacaddress_id());
                String existingMacAddress = FormValueUtil.getValue(formComponents, id);
                int replace_id = Integer.parseInt(inBoundConfig.getReplacemacaddress_id());
                int remove_id = Integer.parseInt(inBoundConfig.getRemovemacaddress_id());
                int prev_id = Integer.parseInt(inBoundConfig.getPrevmacaddress_id());
                String replaceMacAddress = FormValueUtil.getValue(formComponents, replace_id);

                FormValueUtil.updateEdgeForm(formComponents, id, "");
                FormValueUtil.updateEdgeForm(formComponents, prev_id, "");
                FormValueUtil.updateEdgeForm(formComponents, replace_id, "");
                FormValueUtil.updateEdgeForm(formComponents, remove_id, macAddress_y);


                //Set Action Values
                int action_id = Integer.parseInt(inBoundConfig.getActionfield_id());
                String actionValue1 = inBoundConfig.getRemoveActionValue();
                FormValueUtil.updateEdgeForm(formComponents, action_id, actionValue1);


                //Set Form Action Values
                /*int layer_action_id = Integer.parseInt(inBoundConfig.getFormlayer_id());
                String formlayer_action1 = inBoundConfig.getFormlayer_remove();
                FormValueUtil.updateEdgeForm(formComponents, layer_action_id, formlayer_action1);*/
            } else {
                //Replace
                System.out.println("Replace");
                int id = Integer.parseInt(inBoundConfig.getInstallmacaddress_id());
                String existingMacAddress = FormValueUtil.getValue(formComponents, id);
                if (existingMacAddress.equals("")) {
                    existingMacAddress = macAddress_y;
                    FormValueUtil.updateEdgeForm(formComponents, id, macAddress_y);
                    replaceUpdate(formComponents, macAddress_y, macAddress);
                } else {
                    replaceUpdate(formComponents, macAddress_y, macAddress);
                }
            }
        }
    }
}
