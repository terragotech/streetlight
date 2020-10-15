package com.automation.slvtoedge.services;


import com.slvinterface.utils.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;


public class ServerCommunication {
    private Logger logger = Logger.getLogger(ServerCommunication.class);
    protected ResponseEntity<String> call(String url, String body, HttpMethod httpMethod,boolean isSlvCall){
        logger.info("Request Url : "+url);
        logger.info("Request Data : "+body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders(isSlvCall);
        String isCallWithCsrfToken = PropertiesReader.getProperties().getProperty("com.slv.rest.with.csrf.token");
        if(isCallWithCsrfToken.equals("true")){
            try {
                ResponseEntity<String> responseEntity = callSlvWithToken(httpMethod == HttpMethod.GET ? true : false,url);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            HttpEntity request = null;
            if (body != null) {
                headers.add("Content-Type", "application/json");
                request = new HttpEntity<String>(body, headers);
            } else {
                request = new HttpEntity<String>(headers);
            }

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
            logger.info("------------ Response ------------------");

            logger.info("Response Code:" + responseEntity.getStatusCode().toString());
            return responseEntity;
        }
        return new ResponseEntity<String>("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    protected ResponseEntity<byte[]> download(String url, String body, HttpMethod httpMethod,boolean isSlvCall){
        logger.info("Request Url : "+url);
        logger.info("Request Data : "+body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders(isSlvCall);
        String isCallWithCsrfToken = PropertiesReader.getProperties().getProperty("com.slv.rest.with.csrf.token");
        if(isCallWithCsrfToken.equals("true")){
            try {
                ResponseEntity<byte[]> responseEntity = callSlvDownloadWithToken(httpMethod == HttpMethod.GET ? true : false,url);
                return responseEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            HttpEntity request = null;
            if (body != null) {
                headers.add("Content-Type", "application/json");
                request = new HttpEntity<String>(body, headers);
            } else {
                request = new HttpEntity<String>(headers);
            }

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, httpMethod, request, byte[].class);
                logger.info("------------ Response ------------------");
                logger.info("Response Code:" + responseEntity.getStatusCode().toString());
            return responseEntity;
        }
        return new ResponseEntity<byte[]>(new byte[]{}, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private HttpHeaders getHeaders(boolean isSlvCall) {
        String userName;
        String password;
        if(isSlvCall){
            userName = PropertiesReader.getProperties().getProperty("com.slv.username");
            password = PropertiesReader.getProperties().getProperty("com.slv.password");
        }else{
            userName = PropertiesReader.getProperties().getProperty("comed.edge.username");
            password = PropertiesReader.getProperties().getProperty("comed.edge.password");
        }
        HttpHeaders headers = new HttpHeaders();

        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        System.out.println(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }

    protected String sendFilesToEdgeServer(String url, File file){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("file",new FileSystemResource(file));
            HttpEntity<MultiValueMap<String,Object>> httpEntity = new HttpEntity<>(multiValueMap,httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url,multiValueMap,String.class);
            return response;
        }catch (Exception e){
            logger.error("Error while sendFilesToEdgeServer: ",e);
        }
        return null;
    }

    protected ResponseEntity<String> sendFilesToUpdateTable(String url, File file,String tableName,String delimiter,boolean isUpdate){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("file",new FileSystemResource(file));
            multiValueMap.add("tableName",tableName);
            multiValueMap.add("delimiter",delimiter);
            multiValueMap.add("isUpdate",isUpdate);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity responseEntity = restTemplate.postForObject(url,multiValueMap,ResponseEntity.class);
            return responseEntity;
        }catch (Exception e){
            logger.error("Error while sendFilesToEdgeServer: ",e);
        }
        return null;
    }

    private HttpPost getSlvPostHeaders(String url){
        HttpPost httpPost = new HttpPost();
        httpPost.setHeader("x-csrf-token", SlvRestTemplate.INSTANCE.token);
        httpPost.setHeader("x-requested-with","XMLHttpRequest");
        int connectionTimeOut = Integer.valueOf(PropertiesReader.getProperties().getProperty("request.timeout"));
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeOut).setConnectTimeout(connectionTimeOut).setSocketTimeout(connectionTimeOut).build();
        httpPost.setConfig(requestConfig);
        httpPost.setURI(URI.create(url));
        return httpPost;
    }


    private HttpGet getSlvGetHeaders(String url){
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("x-csrf-token",SlvRestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        int connectionTimeOut = Integer.valueOf(PropertiesReader.getProperties().getProperty("request.timeout"));
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeOut).setConnectTimeout(connectionTimeOut).setSocketTimeout(connectionTimeOut).build();
        httpGet.setConfig(requestConfig);
        logger.info("------SLV Url------------");
        logger.info("Url:"+url);
        logger.info("------SLV Url Ends------------");
        httpGet.setURI(URI.create(url));
        return httpGet;
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
        return "internal server error";
    }

    private HttpStatus getHttpStatus(int statusCode){
        for(HttpStatus httpStatus : HttpStatus.values()){
            if(httpStatus.value() == statusCode){
                return httpStatus;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ResponseEntity<String> callSlvWithToken(boolean isGetRequest,String url) throws Exception{
        try{
            SlvRestTemplate.INSTANCE.reConnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("x-csrf-token: "+SlvRestTemplate.INSTANCE.token);
        HttpResponse response = SlvRestTemplate.INSTANCE.httpClient.execute(isGetRequest ? getSlvGetHeaders(url) : getSlvPostHeaders(url), SlvRestTemplate.INSTANCE.httpContext);
        String responseBody = getResponseBody(response);
        int responseCode = response.getStatusLine().getStatusCode();
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseBody, getHttpStatus(responseCode));
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseCode);
        if(responseEntity.getStatusCode() != HttpStatus.OK) {
            logger.info(responseBody);
        }
        logger.info("------------ Response End ------------------");
        return responseEntity;
    }

    private ResponseEntity<byte[]> callSlvDownloadWithToken(boolean isGetRequest,String url) throws Exception{
        try{
            SlvRestTemplate.INSTANCE.reConnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("x-csrf-token: "+SlvRestTemplate.INSTANCE.token);
        HttpResponse response = SlvRestTemplate.INSTANCE.httpClient.execute(isGetRequest ? getSlvGetHeaders(url) : getSlvPostHeaders(url), SlvRestTemplate.INSTANCE.httpContext);
        byte[] bytes = EntityUtils.toByteArray(response.getEntity());
        int responseCode = response.getStatusLine().getStatusCode();
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(bytes, getHttpStatus(responseCode));
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseCode);
        logger.info("------------ Response End ------------------");
        return responseEntity;
    }
}
