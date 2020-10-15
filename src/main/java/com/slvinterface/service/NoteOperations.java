package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slvinterface.utils.PropertiesReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class NoteOperations {
    private EdgeRestService edgeRestService;
    public NoteOperations(){
        edgeRestService = new EdgeRestService();
    }
    public String getNoteDetails(String noteguid) {
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
    public String updateNoteDetails(String noteJson,String noteGuid,String notebookGuid)
    {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String>  responseEntity = edgeRestService.putRequest(urlNew,noteJson,true,tokenString);
        /*if(responseEntity.getStatusCode() == HttpStatus.OK)
        {
        }
         */
            response = responseEntity.getBody();

        return response;
    }
    public String moveNoteToNoteBook(String noteGuid,String notebookGuid)
    {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "/moveNotesToNotebook.html";
        System.out.println(urlNew);
        //String tokenString = edgeRestService.getEdgeToken();

        String bodyText = "{\"notebookGuid\":" + notebookGuid + "," +
                "\"noteGuid\":[" + noteGuid + "]}";
        ResponseEntity<String>  responseEntity = edgeRestService.postRequest(urlNew,bodyText);
        if(responseEntity.getStatusCode() == HttpStatus.OK)
        {
            response = responseEntity.getBody();
        }
        return response;
    }
}
