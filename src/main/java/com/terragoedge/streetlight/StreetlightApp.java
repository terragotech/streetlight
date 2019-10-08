package com.terragoedge.streetlight;

import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Properties;


public class StreetlightApp {
    private static final Logger logger = Logger.getLogger(StreetlightApp.class);

    public static void main(String[] args) {
        int sleepingTime = 1000 * 30;
        long lastMailTime = 0;
        long vpnLastMailTime = 0;
        while (true) {
            try {
                try {
                    Properties properties = PropertiesReader.getProperties();
                    boolean vpnStatus = PingIP.isVPNConnected();
                    logger.info("VPN Status:" + vpnStatus);
                    if (vpnStatus) {
                        vpnLastMailTime = 0;
                        boolean serverAccess = false;
                        try {
                            serverAccess = checkServerAccess();
                        } catch (Exception e) {
                            logger.error("Error in checkServerAccess", e);
                        }
                        logger.info("SLV Server Status:" + vpnStatus);
                        if (!serverAccess) {
                            if((System.currentTimeMillis() - lastMailTime) >= (1000 * 60 * 60) || lastMailTime == 0){
                                String serverUrl = properties.getProperty("com.edge.url.slvdown.url");
                                String subject = properties.getProperty("com.edge.url.slvdown.subject");
                                String body = properties.getProperty("com.edge.url.slvdown.body");
                                String to = properties.getProperty("com.edge.url.slvdown.to");
                                serverCall(serverUrl, subject,body,to);

                                //serverCall(serverUrl, HttpMethod.GET, null);
                                lastMailTime = System.currentTimeMillis();
                            }
                        } else {
                            lastMailTime = 0;
                            //sleepingTime = 1000 * 10;
                        }
                    }else{
                        if(vpnLastMailTime == 0){
                            vpnLastMailTime = System.currentTimeMillis();
                        }else{
                            if((System.currentTimeMillis() - vpnLastMailTime) >= (1000 * 60 * 10)){
                                vpnLastMailTime = System.currentTimeMillis();
                                logger.info("VPN is Not Connected......");
                                String serverUrl = properties.getProperty("com.edge.url.vpndown.url");
                                String subject = properties.getProperty("com.edge.url.vpndown.subject");
                                String body = properties.getProperty("com.edge.url.vpndown.body");
                                String to = properties.getProperty("com.edge.url.vpndown.to");
                                serverCall(serverUrl, subject,body,to);
                                // Need to send Mail
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in checkVPNStatus", e);
                }

            } catch (Exception e) {
                logger.error("Error in checkVPNStatus", e);
            }
            try {
                logger.info("Thread Waiting Time:" + sleepingTime);
                Thread.sleep(sleepingTime);
            } catch (Exception e) {
                logger.error("Error in Thread Sleep", e);
            }

        }
    }


    public static boolean checkServerAccess() {
        RestTemplate restTemplate = new RestTemplate();
        Properties properties = PropertiesReader.getProperties();
        String serverUrl = properties.getProperty("com.slv.url");
        logger.info("Server Url:" + serverUrl);
        ResponseEntity<String> response = restTemplate.exchange(serverUrl, HttpMethod.GET, null, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }


    public static ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpEntity request = null;
        if (body != null) {
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        } else {
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");

        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        if (responseEntity.getBody() != null) {
            logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }


    public static ResponseEntity<String> serverCall(String url, String subject,String body,String rec) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("subject",subject)
                .queryParam("body",body)
                .queryParam("recipientList",rec).build();

        HttpEntity request = new HttpEntity<>(headers);


        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, String.class);
        logger.info("------------ Response ------------------");

        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        if (responseEntity.getBody() != null) {
            logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }



}
