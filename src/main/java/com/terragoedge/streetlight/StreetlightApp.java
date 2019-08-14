package com.terragoedge.streetlight;

import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.Properties;


public class StreetlightApp {
    private static final Logger logger = Logger.getLogger(StreetlightApp.class);

    public static void main(String[] args) {
        int sleepingTime = 1000 * 60;
        while (true){
            try {
                try{
                   boolean vpnStatus = checkVPNStatus();
                   logger.info("VPN Status:"+vpnStatus);
                   if(vpnStatus){
                       boolean serverAccess = false;
                       try {
                           serverAccess =   checkServerAccess();
                       }catch (Exception e){
                           logger.error("Error in checkServerAccess",e);
                       }
                       logger.info("SLV Server Status:"+vpnStatus);
                     if(!serverAccess){
                         Properties properties = PropertiesReader.getProperties();
                         String serverUrl =  properties.getProperty("com.edge.url.slvserverdown");
                         serverCall(serverUrl,HttpMethod.GET,null);
                         sleepingTime = 1000 * 60 * 60;
                     }else{
                         sleepingTime = 1000 * 60;
                     }
                   }
                }catch (Exception e){
                    logger.error("Error in checkVPNStatus",e);
                }

            }catch (Exception e){
                logger.error("Error in checkVPNStatus",e);
            }
            try {
                logger.info("Thread Waiting Time:"+sleepingTime);
                Thread.sleep(sleepingTime);
            }catch (Exception e){
                logger.error("Error in Thread Sleep",e);
            }

        }
	}


	public static boolean checkVPNStatus()throws Exception{
        Properties properties = PropertiesReader.getProperties();
        String ipAddress =  properties.getProperty("com.chicago.vpn.ipaddress");
        InetAddress inet = InetAddress.getByName(ipAddress);
        System.out.println("Sending Ping Request to " + ipAddress);
        return inet.isReachable(5000);
    }


    public static boolean checkServerAccess(){
        RestTemplate restTemplate = new RestTemplate();
        Properties properties = PropertiesReader.getProperties();
        String serverUrl =  properties.getProperty("com.slv.url");
        ResponseEntity<String> response =  restTemplate.exchange(serverUrl, HttpMethod.GET,null,String.class);
        return response.getStatusCode().is2xxSuccessful();
    }


    public static ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers =new HttpHeaders();

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

}
