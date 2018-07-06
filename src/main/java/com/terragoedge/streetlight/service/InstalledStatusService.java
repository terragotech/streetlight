package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.streetlight.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class InstalledStatusService {
    private String baseUrl = null;
    private RestService restService = null;
    private Gson gson = null;
    private static final Logger logger = Logger.getLogger(InstalledStatusService.class);
    InstalledStatusService() {
        baseUrl = PropertiesReader.getProperties().getProperty("server.edge.url.main");
        restService = new RestService();
        gson = new Gson();
    }

    public void run() {
        try {
            //  String notebookNamePath = "./src/main/resources/notebooknames.csv";
            String notebookNamePath = "./resources/notebooknames.csv";
            String notebookName = null;
            String url=null;
            BufferedReader fis = new BufferedReader(new FileReader(notebookNamePath));
            while ((notebookName = fis.readLine()) != null) {
                try {
                    List<Object> paramsList = new ArrayList<>();
                    paramsList.add("noteBookName=" + notebookName);
                    paramsList.add("isSingle=" + true);
                    paramsList.add("ser=json");
                    String params = StringUtils.join(paramsList, "&");
                    url = baseUrl + "&" + params;
                    ResponseEntity<String> responseEntity = serverCall(url, HttpMethod.GET, null);
                    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                        String response = responseEntity.getBody();
                        if (response.contains("Success")) {
                            logger.info("Success Notebook : " + notebookName);
                        } else {
                            logger.info("Failure Notebook : " + notebookName);
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

        HttpEntity request = null;
        if (body != null) {
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        } else {
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");

        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        if (responseEntity.getBody() != null) {
            logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }


    private HttpHeaders getHeaders() {
        String userName = PropertiesReader.getProperties().getProperty("comed.edge.username");
        String password = PropertiesReader.getProperties().getProperty("comed.edge.password");
        HttpHeaders headers = new HttpHeaders();

        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
}