package com.slvinterface.service;

import com.google.gson.Gson;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;

public class SLVRestService {

private static final Logger logger = Logger.getLogger(SLVRestService.class);
    private Properties properties;
    private Gson gson = null;

    public SLVRestService() {
        properties = PropertiesReader.getProperties();
        gson = new Gson();
    }

    public ResponseEntity<String> callGetMethod(String url)throws IOException, ClientProtocolException {
        String isTokenNeeded = properties.getProperty("com.use.token.slv.api");
        if(isTokenNeeded.equals("true")){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(true,url,null);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            HttpHeaders headers = getHeaders(null);
            RestTemplate restTemplate = Utils.getRestTemplate();
            HttpEntity request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + response.getStatusCode().toString());
            String responseBody = response.getBody();
            logger.info(responseBody);
            logger.info("------------ Response End ------------------");
            return response;
        }
        return new ResponseEntity<>("Error while call this request: "+url,HttpStatus.NOT_FOUND);

    }


    public String getResponseBody(HttpResponse httpResponse){
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

    private HttpHeaders getHeaders(String accessToken) {
        String userName = null;
        String password = null;
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.add("Authorization", "Bearer " + accessToken);
            return headers;
        } else {
            userName = properties.getProperty("streetlight.slv.username");
            password = properties.getProperty("streetlight.slv.password");
        }
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;

    }
    public  RestTemplate getRestTemplate(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(1000 * 60 * 5);
        simpleClientHttpRequestFactory.setReadTimeout(1000 * 60 * 5);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        return restTemplate;
    }

    private ResponseEntity<String> callSlvWithToken(boolean isGetRequest, String url, LinkedMultiValueMap<String, String> paramsList) throws Exception{
        RestTemplate restTemplate = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-requested-with","XMLHttpRequest");
        headers.add("x-csrf-token", SlvRestTemplate.INSTANCE.getToken());
        headers.add("Cookie",SlvRestTemplate.INSTANCE.getCookie());
        HttpEntity<LinkedMultiValueMap<String, String>> request = null;
        if (paramsList == null){
            request = new HttpEntity<>(headers);
        }else {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            request = new HttpEntity(paramsList,headers);
        }
        HttpMethod httpMethod = HttpMethod.POST;
        if (isGetRequest){
            httpMethod = HttpMethod.GET;
        }
        ResponseEntity<String> response = restTemplate.exchange(url,httpMethod,request,String.class);
        String responseBody = response.getBody();
        int responseCode = response.getStatusCode().value();
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseCode);
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
    }

    public ResponseEntity<String> getPostRequest(String url, String accessToken, LinkedMultiValueMap<String,String> params) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        String isTokenNeeded = properties.getProperty("com.use.token.slv.api");
        logger.info("request params: "+gson.toJson(params));
        if(accessToken == null && isTokenNeeded.equals("true")){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(false,url,params);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            HttpHeaders headers = getHeaders(accessToken);
            RestTemplate restTemplate = Utils.getRestTemplate();
            HttpEntity<LinkedMultiValueMap<String,String>> request = null;
            if (params == null) {
                request = new HttpEntity<>(headers);
            }else {
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                request = new HttpEntity<>(params,headers);
            }
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + response.getStatusCode().toString());
            logger.info("------------ Response End ------------------");
            return response;
        }
        return new ResponseEntity<>("Error while call this request: "+url, HttpStatus.NOT_FOUND);
    }
}
