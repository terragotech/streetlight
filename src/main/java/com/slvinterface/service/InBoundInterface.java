package com.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.slvinterface.dao.SLVDataQueryExecutor;
import com.slvinterface.json.InBoundConfig;
import com.slvinterface.utils.ResourceDetails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;


abstract public class InBoundInterface {
    protected SLVDataQueryExecutor slvDataQueryExecutor = null;
    protected InBoundConfig inBoundConfig = null;
    Gson gson = null;
    public InBoundInterface() throws Exception{
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
}
