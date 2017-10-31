package com.terragoedge.streetlight.service;

import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.terragoedge.streetlight.PropertiesReader;

public class RestService {
	
	
	private static Logger logger = Logger.getLogger(RestService.class);
	
	Properties properties = null;
	
	public RestService(){
		properties = PropertiesReader.getProperties();
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

	public ResponseEntity<String> getRequest(String url,boolean isLog,String accessToken) {
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
		if(isLog){
			logger.info(responseBody);
		}
		
		logger.info("------------ Response End ------------------");
		// return responseBody;
		return response;
	}

	public ResponseEntity<String> getPostRequest(String url,String accessToken) {
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
	
	
	private HttpHeaders getHeaders(String accessToken) {
		String userName = null;
		String password = null;
		HttpHeaders headers = new HttpHeaders();
		if(accessToken != null){
			headers.add("Authorization",  "Bearer "+accessToken);
			return headers;
		}else{
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
	
	
	public ResponseEntity<String> getRequest(String url){
		logger.info("------------ Request ------------------");
		logger.info(url);
		logger.info("------------ Request End ------------------");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.postForEntity(url,null,   String.class);
		logger.info("------------ Response ------------------");
		logger.info("Response Code:" + response.getStatusCode().toString());
		String responseBody = response.getBody();
		
		
		logger.info("------------ Response End ------------------");
		// return responseBody;
		return response;
	}

}
