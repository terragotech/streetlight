package com.slvinterface.service;

import com.slvinterface.utils.PropertiesReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EdgeRestInterface {
    private static EdgeRestService edgeRestService = new EdgeRestService();

    public static String getNoteDetails(String noteguid) {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notes/" + noteguid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String> requestEntity = edgeRestService.getRequest(urlNew,true,tokenString);
        if(requestEntity.getStatusCode() == HttpStatus.OK)
        {
            response = requestEntity.getBody();
        }
        return response;
    }
}
