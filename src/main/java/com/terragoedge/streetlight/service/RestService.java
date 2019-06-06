package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.terragoedge.streetlight.PropertiesReader;

public class RestService {


    private static Logger logger = Logger.getLogger(RestService.class);

    Properties properties = null;

    public RestService() {
        properties = PropertiesReader.getProperties();
    }

    public ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

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


    private HttpHeaders getHeaders() {
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        logger.info("Requet username :" + userName);
        logger.info("Requet password :" + password);
        HttpHeaders headers = new HttpHeaders();

        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
	/*public <T> ResponseEntity<String> getRequest(Map<String, String> streetLightDataParams, String url,boolean isLog) {
		Set<String> keys = streetLightDataParams.keySet();
		List<String> values = new ArrayList<>();
		for (String key : keys) {
			String val = streetLightDataParams.get(key) != null ? streetLightDataParams.get(key).toString() : "";
			String tem = key + "=" + val;
			values.add(tem);
		}
		String params = StringUtils.join(values, "&");
		url = url + "?" + params;

		return getRequest(url,isLog);
	}*/

    public ResponseEntity<String> getRequest(String url, boolean isLog, String accessToken) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
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
    private HttpStatus getHttpStatus(int statusCode){
        for(HttpStatus httpStatus : HttpStatus.values()){
            if(httpStatus.value() == statusCode){
                return httpStatus;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
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
    private HttpPost getSlvPostHeaders(String url){
        HttpPost httpPost = new HttpPost();
        httpPost.setHeader("x-csrf-token", SlvRestTemplate.INSTANCE.token);
        httpPost.setHeader("x-requested-with","XMLHttpRequest");
        httpPost.setURI(URI.create(url));
        return httpPost;
    }

    public ResponseEntity<String> callSlvWithToken(boolean isGetRequest,String url) throws Exception{
        try{
            logger.info("ReConnect");
            SlvRestTemplate.INSTANCE.reConnect();
        }catch (Exception e){

            e.printStackTrace();
        }
        logger.info("failureport :"+url);
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

    public ResponseEntity<String> getPostRequest(String url, String accessToken) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        HttpHeaders headers = getHeaders(accessToken);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
    }


    public ResponseEntity<String> getPostRequest(String url, String accessToken, String contentType) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        HttpHeaders headers = getHeaders(accessToken);
        headers.add("Content-Type", contentType);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
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


    public ResponseEntity<String> getRequest(String url) {
        logger.info("------------ Request ------------------");
        logger.info(url);
        logger.info("------------ Request End ------------------");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();


        logger.info("------------ Response End ------------------");
        // return responseBody;
        return response;
    }

}
