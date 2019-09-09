package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

public class SlvRestService {
    Properties properties = null;
    private static Logger logger = Logger.getLogger(SlvRestService.class);

    public SlvRestService(){
        properties = PropertiesReader.getProperties();
    }

    public <T> ResponseEntity<String> getRequest(Map<String, String> streetLightDataParams, String url, boolean isLog) {
        Set<String> keys = streetLightDataParams.keySet();
        List<String> values = new ArrayList<String>();
        for (String key : keys) {
            try {
                String val = streetLightDataParams.get(key) != null ? streetLightDataParams.get(key).toString() : "";
                String tem = key + "=" + URLEncoder.encode(val.trim(), "UTF-8");
                values.add(tem);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String params = StringUtils.join(values, "&");
        url = url + "?" + params;

        return getRequest(url,isLog);
    }

    public ResponseEntity<String> getRequest(String url,boolean isLog) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        String isTokenNeeded = properties.getProperty("com.use.token.slv.api");
        if(isTokenNeeded.equals("true")){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(true,url);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            // url = "https://slv.poc02.ssn.ssnsgs.net:8443/reports/api/servlet/SLVAssetManagementAPI?methodName=createCategoryDevice&nodeTypeStrId=TB398484989!lightNode01&geoZoneId=738&lng=51.515193&lat=-0.10689554&controllerStrId=TalqBridge@TB398484989&categoryStrId=streetlight&idOnController=38127-demo";
            HttpHeaders headers = getHeaders();
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + response.getStatusCode().toString());
            String responseBody = response.getBody();
            if (isLog) {
                logger.info(responseBody);
            }

            logger.info("------------ Response End ------------------");
            // return responseBody;
            return response;
        }
        return new ResponseEntity<>("Error while call this request: "+url, HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> getRequest(String url,boolean isLog,String accessToken) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        String isTokenNeeded = properties.getProperty("com.use.token.slv.api");
        if(isTokenNeeded.equals("true") && accessToken == null){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(true,url);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            HttpHeaders headers = getHeaders(accessToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + response.getStatusCode().toString());
            String responseBody = response.getBody();
            if (isLog) {
                logger.info(responseBody);
            }

            logger.info("------------ Response End ------------------");
            // return responseBody;
            return response;
        }
        return new ResponseEntity<>("Error while call this request: "+url,HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> getPostRequest(String url,String accessToken) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        String isTokenNeeded = properties.getProperty("com.use.token.slv.api");
        if(isTokenNeeded.equals("true") && accessToken == null){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(true,url);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            HttpHeaders headers = getHeaders(accessToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + response.getStatusCode().toString());
            String responseBody = response.getBody();
            //  logger.info(responseBody);
            logger.info("------------ Response End ------------------");
            return response;
        }
        return new ResponseEntity<>("Error while call this request: "+url,HttpStatus.NOT_FOUND);
    }

    private HttpHeaders getHeaders(String accessToken) {
        String userName = null;
        String password = null;
        HttpHeaders headers = new HttpHeaders();
        if(accessToken != null){
            headers.add("Authorization",  "Bearer "+accessToken);
            return headers;
        }else{
            userName = properties.getProperty("streetlight.slv.username");
            password = properties.getProperty("streetlight.slv.password");
        }
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        headers.add("Authorization", "Basic "+base64Creds);
        return headers;
    }


    public ResponseEntity<String> getRequest(String url){
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url,null,   String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();


        logger.info("------------ Response End ------------------");
        // return responseBody;
        return response;
    }
    private HttpHeaders getHeaders() {
        String userName = properties.getProperty("streetlight.slv.username");
        String password = properties.getProperty("streetlight.slv.password");
        String plainCreds = userName + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic "+base64Creds);
        return headers;
    }

    private ResponseEntity<String> callSlvWithToken(boolean isGetRequest,String url) throws Exception{
        try{
            SlvRestTemplate.INSTANCE.reConnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        HttpResponse response = SlvRestTemplate.INSTANCE.httpClient.execute(isGetRequest ? getSlvGetHeaders(url) : getSlvPostHeaders(url), SlvRestTemplate.INSTANCE.httpContext);
        String responseBody = getResponseBody(response);
        int responseCode = response.getStatusLine().getStatusCode();
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseBody, getHttpStatus(responseCode));
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseCode);
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return responseEntity;
    }

    private String getResponseBody(HttpResponse httpResponse){
        try{
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity().getContent()));
            String response = IOUtils.toString(rd);
            logger.info("-------SLV Response ----------");
            logger.info(response);
            logger.info("-------SLV Response Ends----------");
            return response;
        }catch (Exception e){
            logger.error("Error in getResponseBody",e);
        }
        return null;
    }

    private HttpPost getSlvPostHeaders(String url){
        HttpPost httpPost = new HttpPost();
        httpPost.setHeader("x-csrf-token", SlvRestTemplate.INSTANCE.token);
        httpPost.setHeader("x-requested-with","XMLHttpRequest");
        httpPost.setURI(URI.create(url));
        return httpPost;
    }


    private HttpGet getSlvGetHeaders(String url){
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("x-csrf-token",SlvRestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        logger.info("------SLV Url------------");
        logger.info("Url:"+url);
        logger.info("------SLV Url Ends------------");
        httpGet.setURI(URI.create(url));
        return httpGet;
    }

    private HttpStatus getHttpStatus(int statusCode){
        for(HttpStatus httpStatus : HttpStatus.values()){
            if(httpStatus.value() == statusCode){
                return httpStatus;
            }
        }
        return HttpStatus.NOT_FOUND;
    }

}
