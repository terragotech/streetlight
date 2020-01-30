package com.terragoedge.streetlight.service;

import com.terragoedge.streetlight.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Properties;

public enum SlvRestTemplate {

    INSTANCE;

    HttpClient httpClient;
    HttpContext httpContext;
    private String token;
    private String cookie;
    private CookieStore cookieStore;

    private static final Logger logger = Logger.getLogger(SlvRestTemplate.class);
    Properties properties = PropertiesReader.getProperties();



     public void refreshToken()throws Exception{
         getSLVCsrfToken();
     }



     public String getToken(){
         return token;
     }

     public String getCookie(){
         return cookie;
     }




    private void init(){
        httpClient = HttpClientBuilder.create().build();
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

    }

    private void getCsrfToken()throws AuthenticationException,Exception{
        String userName = properties.getProperty("streetlight.slv.username");
        String password = properties.getProperty("streetlight.slv.password");
        String plainCreds = userName + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpGet httpGet = new HttpGet();
        httpGet.setHeader("authorization", "Basic "+base64Creds);
        httpGet.setHeader("x-csrf-token","Fetch");
        httpGet.setHeader("x-requested-with","XMLHttpRequest");
        String baseUrl = properties.getProperty("streetlight.url.main");
        System.out.println(baseUrl+"/reports/api/userprofile/getCurrentUser");
       // logger.info("Url:"+baseUrl+"/reports/api/userprofile/getCurrentUser");
        httpGet.setURI(URI.create(baseUrl+"/reports/api/userprofile/getCurrentUser"));

        HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
        int code = httpResponse.getStatusLine().getStatusCode();

        if(code == HttpStatus.SC_OK){
            token = httpResponse.getFirstHeader("X-CSRF-Token").getValue();
        }else{
            throw  new AuthenticationException("Unable to Authentication. Status code"+code);
        }
    }



    private void getSLVCsrfToken()throws AuthenticationException,Exception{
        String userName = properties.getProperty("streetlight.slv.username");
        String password = properties.getProperty("streetlight.slv.password");
        String plainCreds = userName + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        String baseUrl = properties.getProperty("streetlight.url.main");
        baseUrl =   baseUrl+"/reports/api/userprofile/getCurrentUser";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Basic "+base64Creds);
        headers.add("x-csrf-token","Fetch");
        headers.add("x-requested-with","XMLHttpRequest");
        HttpEntity request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, request, String.class);
        if(response.getStatusCode().is2xxSuccessful()){
            HttpHeaders responseHeaders = response.getHeaders();
            cookie = responseHeaders.getFirst(HttpHeaders.SET_COOKIE);
            token = responseHeaders.getFirst("X-CSRF-Token");
        }else{
            logger.error("Error in getSLVCsrfToken");
            if(response != null){
                logger.error(response.getBody());
            }
            throw  new AuthenticationException("Unable to Authentication. Status code"+response.getStatusCodeValue());
        }

    }


}