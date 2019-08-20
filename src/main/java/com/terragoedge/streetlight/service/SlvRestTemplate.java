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

import java.net.URI;
import java.util.Properties;

public enum SlvRestTemplate {

    INSTANCE;

    HttpClient httpClient;
    HttpContext httpContext;
    String token;
    private CookieStore cookieStore;

    private static final Logger logger = Logger.getLogger(SlvRestTemplate.class);
    Properties properties = PropertiesReader.getProperties();

    SlvRestTemplate(){
        try{
            reConnect();
        }catch (Exception e){
            httpClient = null;
            httpContext = null;
            token = null;
            e.printStackTrace();

        }
    }

    public void reConnect()throws Exception{
        init();
        getCsrfToken();
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
        String baseUrl = properties.getProperty("streetlight.slv.url.main");
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


}
