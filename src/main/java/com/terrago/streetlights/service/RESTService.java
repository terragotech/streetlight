package com.terrago.streetlights.service;

import com.terrago.streetlights.utils.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RESTService {
    private static Logger logger = Logger.getLogger(RESTService.class);
    public static String getNoteDetails(String noteguid)
    {
        String response ="";
        String baseUrl = PropertiesReader.getProperties().getProperty("baseurl");
        String urlNew = baseUrl + "/rest/notes/" + noteguid;
        System.out.println(urlNew);
        //   logger.info("Url to get Note Details:" + urlNew);
        ResponseEntity<String> responseEntity = serverCall(urlNew, HttpMethod.GET, null);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            response = responseEntity.getBody();
            //     logger.info("----------Response-------");
            //   logger.info(response);
            return response;
        }
        return response;
    }
    public static  HttpHeaders getHeaders() {
        String userName = com.terragoedge.streetlight.PropertiesReader.getProperties().getProperty("edge.username");
        String password = com.terragoedge.streetlight.PropertiesReader.getProperties().getProperty("edge.password");
        HttpHeaders headers = new HttpHeaders();
        String plainCreds = userName + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
    public static ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        //   logger.info("Request Url : " + url);
        //  logger.info("Request Data : " + body);
        logger.info("REST call " + url);
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
        //  logger.info("------------ Response ------------------");

        //  logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        if (responseEntity.getBody() != null) {
               logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }
    public static ResponseEntity<String> updateNoteDetails(String noteDetails, String noteGuid, String notebookGuid) {
        System.out.println("Update called");
        String baseUrl = PropertiesReader.getProperties().getProperty("baseurl");
        System.out.println(noteDetails);
        String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        return serverCall(urlNew, HttpMethod.PUT, noteDetails);
    }
}
