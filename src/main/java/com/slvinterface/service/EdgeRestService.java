package com.slvinterface.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slvinterface.utils.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class EdgeRestService {

    private static final Logger logger = Logger.getLogger(EdgeRestService.class);
    JsonParser jsonParser;

    EdgeRestService(){
        jsonParser = new JsonParser();
    }


    protected String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String userName = PropertiesReader.getProperties().getProperty("streetlight.edge.username");
        String password = PropertiesReader.getProperties().getProperty("streetlight.edge.password");
        url = url + "oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;
    }


    public ResponseEntity<String> getRequest(String url){
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url,null,   String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        logger.info("------------ Response End ------------------");
        // return responseBody;
        return response;
    }


    public ResponseEntity<String> getRequest(String url,boolean isLog,String accessToken) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        HttpHeaders headers = getHeaders(accessToken);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        if(isLog){
            logger.info(responseBody);
        }

        logger.info("------------ Response End ------------------");
        // return responseBody;
        return response;
    }


    private HttpHeaders getHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",  "Bearer "+accessToken);
        return headers;
    }


    public ResponseEntity<String> slv2Edge(String httpUrl,  HttpMethod httpMethod, MultiValueMap<String, String> params){
        HttpHeaders headers = getEdgeHeaders();
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);

        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.slvServerUrl");
        url = url + httpUrl;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        uriBuilder.queryParams(params);

        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
    }


    private HttpHeaders getEdgeHeaders() {
        String userName = PropertiesReader.getProperties().getProperty("streetlight.edge.username");
        String password = PropertiesReader.getProperties().getProperty("streetlight.edge.password");
        HttpHeaders headers = new HttpHeaders();
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
    public ResponseEntity<String> putRequest(String urlNew,String body,boolean mustLog,String accessToken){
        HttpHeaders headers = getHeaders(accessToken);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = null;
        if (body != null) {
            request = new HttpEntity<String>(body, headers);
        }
        else
        {
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> response = restTemplate.exchange(urlNew, HttpMethod.PUT, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        if(mustLog){
            logger.info(responseBody);
        }

        logger.info("------------ Response End ------------------");
        // return responseBody;
        return response;

    }
}
