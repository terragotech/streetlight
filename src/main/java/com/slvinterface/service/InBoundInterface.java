package com.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import com.slvinterface.dao.SLVDataQueryExecutor;
import com.slvinterface.json.InBoundConfig;
import com.slvinterface.model.HistoryModel;
import com.slvinterface.model.SLVDataInfo;
import com.slvinterface.utils.DataOperations;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


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
    public void updateNotes(String pathToCsv){

    }
}
