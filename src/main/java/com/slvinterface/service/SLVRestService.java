package com.slvinterface.service;

import com.slvinterface.utils.PropertiesReader;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import com.slvinterface.service.RestTemplate;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class SLVRestService {

private static final Logger logger = Logger.getLogger(SLVRestService.class);


    public HttpResponse callGetMethod(String url)throws IOException, ClientProtocolException {
        try{
            RestTemplate.INSTANCE.reConnect();
        }catch (Exception e){
            e.printStackTrace();
        }

        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("x-csrf-token",RestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        logger.info("------SLV Url------------");
        logger.info("Url:"+url);
        logger.info("------SLV Url Ends------------");
        httpGet.setURI(URI.create(url));
        HttpResponse response = RestTemplate.INSTANCE.httpClient.execute(httpGet, RestTemplate.INSTANCE.httpContext);
        logger.info("------SLV Response Status Code------------");
        logger.info("Status Code:"+response.getStatusLine().getStatusCode());
        logger.info("------SLV Response Status Code Ends------------");
        return response;

    }



    private HttpResponse callPostMethod()throws IOException, ClientProtocolException {
        HttpPost httpGet = new HttpPost();
        httpGet.setHeader("x-csrf-token",RestTemplate.INSTANCE.token);
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.slv.base.url");
        httpGet.setURI(URI.create(baseUrl));
        HttpResponse response = RestTemplate.INSTANCE.httpClient.execute(httpGet, RestTemplate.INSTANCE.httpContext);
        return response;

    }
    public HttpResponse callPostMethod(String url)throws Exception {
        HttpPost httpPost = new HttpPost();
        RestTemplate.INSTANCE.reConnect();
        httpPost.setHeader("x-csrf-token",RestTemplate.INSTANCE.token);
        httpPost.setHeader("x-requested-with","XMLHttpRequest");
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.slv.base.url");
        httpPost.setURI(URI.create(url));
        System.out.println(url);
        logger.info(url);
        HttpResponse response = RestTemplate.INSTANCE.httpClient.execute(httpPost, RestTemplate.INSTANCE.httpContext);
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
}
