package com.slvinterface.service;

import com.slvinterface.utils.PropertiesReader;
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

public class SLVRestService {

    private static final Logger logger = Logger.getLogger(SLVRestService.class);


   /* public HttpResponse callGetMethod(String url)throws IOException, ClientProtocolException {
        try{
            SLVRestTemplate.INSTANCE.reConnect();
        }catch (Exception e){
            e.printStackTrace();
        }

        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("x-csrf-token", SLVRestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        logger.info("------SLV Url------------");
        logger.info("Url:"+url);
        logger.info("------SLV Url Ends------------");
        httpGet.setURI(URI.create(url));
        HttpResponse response = SLVRestTemplate.INSTANCE.httpClient.execute(httpGet, SLVRestTemplate.INSTANCE.httpContext);
        logger.info("------SLV Response Status Code------------");
        logger.info("Status Code:"+response.getStatusLine().getStatusCode());
        logger.info("------SLV Response Status Code Ends------------");
        return response;

    }


    private HttpResponse callPostMethod()throws IOException, ClientProtocolException {
        HttpPost httpGet = new HttpPost();
        httpGet.setHeader("x-csrf-token", SLVRestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.slv.base.url");
        httpGet.setURI(URI.create(baseUrl));
        HttpResponse response = SLVRestTemplate.INSTANCE.httpClient.execute(httpGet, SLVRestTemplate.INSTANCE.httpContext);
        return response;

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
*/

    public ResponseEntity<String> callSlvWithToken(boolean isGetRequest,String url,LinkedMultiValueMap<String, String> paramsList) throws Exception{
        RestTemplate restTemplate = getRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-requested-with","XMLHttpRequest");
        headers.add("x-csrf-token", SLVRestTemplate.INSTANCE.getToken());
        headers.add("Cookie",SLVRestTemplate.INSTANCE.getCookie());
        HttpEntity<LinkedMultiValueMap<String, String>> request = null;
        if(paramsList == null){
            request = new HttpEntity<>(headers);
        }else{
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            request = new HttpEntity(paramsList,headers);
        }

        HttpMethod httpMethod = HttpMethod.POST;
        if(isGetRequest){
            httpMethod = HttpMethod.GET;
        }
        ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCodeValue());
        logger.info(response.getBody());
        logger.info("------------ Response End ------------------");
        return response;
    }



    public  RestTemplate getRestTemplate(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(1000 * 60 * 5);
        simpleClientHttpRequestFactory.setReadTimeout(1000 * 60 * 5);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        return restTemplate;
    }
}