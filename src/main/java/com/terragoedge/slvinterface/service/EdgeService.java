package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class EdgeService {
    private Logger logger = Logger.getLogger(EdgeService.class);

    protected ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("------------ Request ------------------");
        logger.info("Request Url:" + url);
        logger.info("------------ input data ------------------");
        logger.info("Request Data:" + body);
        RestTemplate restTemplate = getRestTemplate();
        HttpHeaders headers = getHeaders(null);

        HttpEntity request = null;
        if(body != null){
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        }else{
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        logger.info("------------ Response data ------------------");
        logger.info("Response Data:" + responseEntity.getBody());
        return responseEntity;
    }


    protected HttpHeaders getHeaders(String accessToken) {
        String userName = null;
        String password = null;
        HttpHeaders headers = new HttpHeaders();
        if(accessToken != null){
            headers.add("Authorization",  "Bearer "+accessToken);
            return headers;
        }else{
            userName = "admin";
            password = "T455wy04ry41!";
        }
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }


    public RestTemplate getRestTemplate(){
        try{
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();


            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpclient);
            RestTemplate restTemplate = new RestTemplate(requestFactory);
            return restTemplate;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
