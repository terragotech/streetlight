package com.terrago.streetlights.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.UbiInterfaceLog;
import com.terrago.streetlights.utils.JsonDataParser;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class UbicquiaLightsInterface {
    private static Logger logger = Logger.getLogger(UbicquiaLightsInterface.class);
    private static String dynamicToken = null;
    private static long timeStamp = 0;
    public static long getTimeStamp(){
        return timeStamp;
    }
    public static List<String> getGroupNodes(String groupID)
    {
        logger.info("get all nodes in a group");
        String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
        List<String> result = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes/group/" + groupID;
            //headers.add("x-api-key", "321b0b2e5a815068913c659e93dc56608bd8c4dafcc586f5e1732cf41b443f54");
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());

                JsonArray jsonArray = jsonObject.getAsJsonArray ("data");
                logger.info("Got Response : " + response.getBody() );

                for (JsonElement pa : jsonArray) {
                    JsonObject jsonObject1 = pa.getAsJsonObject();
                    String res1 = JsonDataParser.checkDataNull(jsonObject1, "id");
                    result.add(res1);

                }

            }
            else
            {
                logger.info("Bad Repsponse "  + response.getStatusCode());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public static String updateCustomFields(LastUpdated lastUpdated, String nodeData, String dev_eui)
    {
        logger.info("Update Custom Fields");
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;

            String requestURL = baseURL + "/nodecustomdata";
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(nodeData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(nodeData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                result = datajsonObject.toString();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;

    }
    public static JsonObject getNodes(LastUpdated lastUpdated, String dev_eui)
    {
        logger.info("get all nodes");
        JsonObject result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes?node_level_type_id=1";
            //headers.add("x-api-key", "321b0b2e5a815068913c659e93dc56608bd8c4dafcc586f5e1732cf41b443f54");
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody("");
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());

                JsonArray jsonArray = jsonObject.getAsJsonArray ("data");
                logger.info("Got Response : " + response.getBody() );

                for (JsonElement pa : jsonArray) {
                    JsonObject jsonObject1 = pa.getAsJsonObject();
                    String res1 = JsonDataParser.checkDataNull(jsonObject1, "dev_eui");
                    if(res1.trim().toLowerCase().equals(dev_eui.trim().toLowerCase()))
                    {
                        result = jsonObject1;
                        break;
                    }

                }

            }
            else
            {
                logger.info("Bad Repsponse "  + response.getStatusCode());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public static void requestDynamicToken()
    {

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String strUserName = PropertiesReader.getProperties().getProperty("ubicquia_user");
            String strPassword = PropertiesReader.getProperties().getProperty("ubicquia_pwd");

            MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
            map.add("email", strUserName);
            map.add("password", strPassword);

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/loginToUbiVu";
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity( requestURL, request , String.class );
            if(response.getStatusCode() == HttpStatus.OK)
            {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                dynamicToken =  datajsonObject.get("access_token").getAsString();
                timeStamp = System.currentTimeMillis();
                logger.info("Dynamic token found " + dynamicToken);
                System.out.println(dynamicToken);

            }
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static String getNodeData(String nodeId)
    {
        logger.info("Entering getNode Data");
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes/" + nodeId;
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());

                JsonArray jsonArray = jsonObject.getAsJsonArray ("data");
                logger.info("Got Response : " + response.getBody() );

                for (JsonElement pa : jsonArray) {
                    JsonObject jsonObject1 = pa.getAsJsonObject();
                    result = jsonObject1.toString();
                }

            }
            else
            {
                logger.info("Bad Repsponse "  + response.getStatusCode());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;

    }
    public static String setNodeData(LastUpdated lastUpdated,String nodeId,String nodeData)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;

            String requestURL = baseURL + "/nodes/update-node/" + nodeId;
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(nodeData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.PUT, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(nodeData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                result = datajsonObject.toString();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;

    }

    public static String SetDimmingValue(LastUpdated lastUpdated,String id,String dimmingValue)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes/setLightDim";
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            String idData = "";
            if(dimmingValue == null)
            {
                idData = "{\"id_list\":[{\"id\":" + id + "}],\"value\":" + "80" + "}";
            }
            else
            {
                idData = "{\"id_list\":[{\"id\":" + id + "}],\"value\":" + dimmingValue + "}";
            }
            System.out.println(idData);
            HttpEntity<String> request = new HttpEntity<String>(idData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(idData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                result = datajsonObject.toString();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public static String SetDevice(LastUpdated lastUpdated,String id,boolean status, String dimmingVal)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes/setLightState";
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            String idData = "";
            if(status) {
                idData = "{\"id_list\":[{\"id\":" + id + "}],\"value\":" + "1" + ",\"node_level_type_id\": 1}";
            }
            else
            {
               if(dimmingVal != null){
                   idData = "{\"id_list\":[{\"id\":" + id + "}],\"value\":" + ""+dimmingVal+"" + ",\"node_level_type_id\": 1}";
               }else{
                   idData = "{\"id_list\":[{\"id\":" + id + "}],\"value\":" + "0" + ",\"node_level_type_id\": 1}";
               }

            }
            logger.info("Url:"+requestURL);
            logger.info("Request Body:"+idData);
            System.out.println(idData);
            System.out.println(requestURL);
            System.out.println(idData);
            HttpEntity<String> request = new HttpEntity<String>(idData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(idData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                logger.info("Response Body:"+response.getBody());
                /*JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                result = datajsonObject.toString();*/
            }

        }
        catch (Exception e)
        {
            logger.error("Error",e);
        }
        return result;
    }
    public static String SetMultipleDevice(LastUpdated lastUpdated,List<String> lstID,boolean status)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;

            String requestURL = baseURL + "/nodes/setLightState";
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            String idData = "";
            if(status) {
                idData = "{\"id_list\":[";
                String tmp = "";
                int tc = lstID.size();
                for(int idx=0;idx<tc;idx++)
                {
                    if(idx != tc-1)
                    {
                        tmp = tmp + "{\"id\":" + lstID.get(idx) + "},";
                    }
                    else
                    {
                        tmp = tmp + "{\"id\":" + lstID.get(idx) + "}";
                    }
                }
                idData = idData + tmp + "],\"value\":" + "1" + "}";
            }
            else
            {
                idData = "{\"id_list\":[";
                String tmp = "";
                int tc = lstID.size();
                for(int idx=0;idx<tc;idx++)
                {
                    if(idx != tc-1)
                    {
                        tmp = tmp + "{\"id\":" + lstID.get(idx) + "},";
                    }
                    else
                    {
                        tmp = tmp + "{\"id\":" + lstID.get(idx) + "}";
                    }
                }
                idData = idData + tmp + "],\"value\":" + "0" + "}";
            }
            System.out.println(idData);
            HttpEntity<String> request = new HttpEntity<String>(idData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(idData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonObject datajsonObject = jsonObject.getAsJsonObject("data");
                result = datajsonObject.toString();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public static String getQueryData(String queryString)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();

            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;

            String requestURL = baseURL + "/nodes?q=" + queryString;
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<String>(headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
                JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                JsonArray jsonArray = jsonObject.getAsJsonArray ("data");

                for (JsonElement pa : jsonArray) {
                    JsonObject jsonObject1 = pa.getAsJsonObject();
                    result = jsonObject1.toString();
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
    public static String CreateNewNode(LastUpdated lastUpdated,String dev_eui,String strLatitude,String strLongitude,String poleID)
    {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String strBaseUrl = PropertiesReader.getProperties().getProperty("ubicquia_baseurl");
            String baseURL = strBaseUrl;
            String requestURL = baseURL + "/nodes";
            headers.add("Authorization", "Bearer " + dynamicToken);
            RestTemplate restTemplate = new RestTemplate();
            String idData = "";
            JsonObject jsonObjectNewNode = new JsonObject();
            jsonObjectNewNode.addProperty("node",poleID);
            jsonObjectNewNode.addProperty("latitude",strLatitude);
            jsonObjectNewNode.addProperty("longitude",strLongitude);
            jsonObjectNewNode.addProperty("dev_eui",dev_eui);
            jsonObjectNewNode.addProperty("active","true");
            jsonObjectNewNode.addProperty("twinPole","0");
            idData = jsonObjectNewNode.toString();
            System.out.println(idData);
            HttpEntity<String> request = new HttpEntity<String>(idData,headers);
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            if(lastUpdated != null)
            {
                UbiInterfaceLog ubiInterfaceLog = new UbiInterfaceLog();
                ubiInterfaceLog.setNotegui(lastUpdated.getNoteguid());
                ubiInterfaceLog.setTitle(lastUpdated.getTitle());
                ubiInterfaceLog.setUrlrequest(requestURL);
                ubiInterfaceLog.setRequestBody(idData);
                ubiInterfaceLog.setRequestResponse(response.getBody());
                ubiInterfaceLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiInterfaceLog(ubiInterfaceLog);
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonObject jsonObject = JsonDataParser.getJsonObject(response.getBody());
                    System.out.println(jsonObject.toString());
                    JsonObject datajsonObject = jsonObject.getAsJsonObject("data");

                    String strStatus = jsonObject.get("status").getAsString();
                    String strCode = jsonObject.get("code").getAsString();
                    if(strStatus.equals("success") && strCode.equals("200"))
                    {
                        result = "success";
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
