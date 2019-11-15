package com.slvinterface.service;

import com.google.gson.*;
import com.slvinterface.dao.NetSenseQueryExecutor;
import com.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class NetSenseInterface {
    private static final Logger logger = Logger.getLogger(NetSenseInterface.class);
    public static String createFixture(String fixtureQRScan, String luminareType,String nemaSocket)
    {
        String responseJSON = "";
        String baseURL = PropertiesReader.getProperties().getProperty("netsense.base.url");
        String customerID = PropertiesReader.getProperties().getProperty("netsense.customer.id");
        String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
        String api_key = PropertiesReader.getProperties().getProperty("netsense.appkey");
        String requestURL = baseURL + "v3.0/customers/" + customerID + "/sites/" + siteID + "/fixtures";
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key",api_key);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJsonString = "";
        JsonObject jsonObject1 = new JsonObject();

        String []fixtureQRScanValues = fixtureQRScan.split(",");

        String fixtureName = "";
        String manufacturerName = "";
        if(fixtureQRScanValues.length > 0)
        {
            fixtureName = fixtureQRScanValues[0];
            manufacturerName = fixtureQRScanValues[0];
        }

        jsonObject1.addProperty("name",fixtureName);
        jsonObject1.addProperty("manufacturer",manufacturerName);
        jsonObject1.addProperty("nemasocket",nemaSocket);
        jsonObject1.addProperty("fixtureType",luminareType);

        Gson gson = new Gson();
        requestJsonString = gson.toJson(jsonObject1);

        HttpEntity request = new HttpEntity<>(requestJsonString,headers);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            responseJSON = response.getBody();
            logger.info("Create fixture called");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.info("Create fixture failed");
        }
        return responseJSON;
    }
    public static void assignNode(String fixtureID,String nodeID)
    {
        String baseURL = PropertiesReader.getProperties().getProperty("netsense.base.url");
        String customerID = PropertiesReader.getProperties().getProperty("netsense.customer.id");
        String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
        String api_key = PropertiesReader.getProperties().getProperty("netsense.appkey");
        String requestURL = baseURL + "v3.0/customers/" + customerID + "/sites/" + siteID + "/fixtures/" + fixtureID + "/assign/node/" + nodeID;
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key",api_key);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJsonString = "";
        JsonObject jsonObject1 = new JsonObject();

        jsonObject1.addProperty("fixtureid",fixtureID);
        jsonObject1.addProperty("nodeid",nodeID);


        Gson gson = new Gson();
        requestJsonString = gson.toJson(jsonObject1);
        HttpEntity request = new HttpEntity<>(requestJsonString,headers);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            String responseJSON = response.getBody();
            logger.info("Create fixture called");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.info("Create fixture failed");
        }


    }

    public static void setLight(String nodeID,String lightLevel, String turnOnDuration, boolean status)
    {
        String baseURL = PropertiesReader.getProperties().getProperty("netsense.base.url");
        String customerID = PropertiesReader.getProperties().getProperty("netsense.customer.id");
        String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
        String api_key = PropertiesReader.getProperties().getProperty("netsense.appkey");
        String requestURL = baseURL + "v3.0/customers/" + customerID + "/sites/" + siteID + "/lightcontrol/node/" + nodeID;
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key",api_key);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJsonString = "";
        if(status)
        {
            requestJsonString = "{\"level\":"+ lightLevel+",\"timeout\":" + turnOnDuration + "}";
        }
        else
        {
            requestJsonString = "{\"level\":1,\"timeout\":" + turnOnDuration+"}";
        }
        logger.info("DC request String = " + requestJsonString);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(requestJsonString,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.POST, request, String.class);
            String responseJSON = response.getBody();
            logger.info("Device Controlled called");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.info("Device Controlled call failed");
        }

    }
    public static JsonObject getFixtureDetails(String fixtureID)
    {
        JsonObject jsonObject = null;
        String baseURL = PropertiesReader.getProperties().getProperty("netsense.base.url");
        String customerID = PropertiesReader.getProperties().getProperty("netsense.customer.id");
        String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
        String api_key = PropertiesReader.getProperties().getProperty("netsense.appkey");
        String requestURL = baseURL + "v3.0/customers/" + customerID + "/sites/" + siteID + "/fixtures";
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key",api_key);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseJSON = response.getBody();
                System.out.println(responseJSON);
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseJSON);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while(iterator.hasNext())
                {
                    JsonElement jsonElement1 = iterator.next();
                    JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
                    String curNodeID = jsonObject1.get("fixtureid").getAsString();
                    if(curNodeID.equals(fixtureID))
                    {
                        jsonObject = jsonObject1;
                        System.out.println(jsonObject1.toString());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public static JsonObject getNodeDetails(String nodeID){
        JsonObject jsonObject = null;
        String baseURL = PropertiesReader.getProperties().getProperty("netsense.base.url");
        String customerID = PropertiesReader.getProperties().getProperty("netsense.customer.id");
        String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
        String api_key = PropertiesReader.getProperties().getProperty("netsense.appkey");
        String requestURL = baseURL + "v3.0/customers/" + customerID + "/sites/" + siteID + "/nodes";
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key",api_key);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseJSON = response.getBody();
                System.out.println(responseJSON);
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseJSON);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while(iterator.hasNext())
                {
                    JsonElement jsonElement1 = iterator.next();
                    JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
                    String curNodeID = jsonObject1.get("nodeid").getAsString();
                    if(curNodeID.equals(nodeID))
                    {
                        jsonObject = jsonObject1;
                        System.out.println(jsonObject1.toString());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public void start(){


       try {
           NetSenseQueryExecutor queryExecutor = new NetSenseQueryExecutor();
           while(true) {

               List<String[]> lstTime = queryExecutor.getLastModifiedTime();
               if(lstTime != null)
               {
                   if(lstTime.size() > 0)
                   {
                       String []lastModifiedTimeArray = lstTime.get(0);
                       String lastModifiedTime = lastModifiedTimeArray[0];
                       long maxLngTime = Long.parseLong(lastModifiedTime);
                       logger.info("Looking for new changes ...");
                       List<String[]> lstResult = queryExecutor.getLatestNoteGUIDS(Long.parseLong(lastModifiedTime));
                       if(lstResult != null)
                       {
                           if(lstResult.size() > 0)
                           {
                               int tc = lstResult.size();
                               for(int idx=0;idx<tc;idx++)
                               {
                                  String []resu = lstResult.get(idx);
                                  String noteGUID = resu[0];
                                  long modifiedTime = Long.parseLong(resu[1]);
                                  CreateRevision createRevision = new CreateRevision();
                                  createRevision.createRevision(noteGUID,null);
                                  if(modifiedTime > maxLngTime)
                                  {
                                      maxLngTime = modifiedTime;
                                  }
                                   long ntime = System.currentTimeMillis();
                                   maxLngTime = Math.max(maxLngTime, ntime);
                                  queryExecutor.setLastModifiedTime(maxLngTime);
                               }
                           }
                       }

                   }
               }




               Thread.sleep(5000);
           }
        }catch (Exception e)
        {
            e.printStackTrace();
            logger.info(e);
        }
        /*JsonObject jsonObject = getNodeDetails("015322000006412");
        if(jsonObject != null)
        {
            String fixtureID = jsonObject.get("fixtureid").getAsString();
            JsonObject jsonObject1 = getFixtureDetails(fixtureID);

        }*/

    }

}
