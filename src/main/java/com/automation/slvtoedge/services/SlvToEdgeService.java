package com.automation.slvtoedge.services;

import com.automation.slvtoedge.json.GeoZone;

import com.automation.slvtoedge.utils.Utils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SlvToEdgeService extends ServerCommunication{
    private Properties properties = null;
    private JsonParser jsonParser;
    private Gson gson;
    private Logger logger = Logger.getLogger(SlvToEdgeService.class);
    public SlvToEdgeService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
    }

    public void start(String slvDataPath){
        try {

            File slvFile = new File(slvDataPath);
            if (slvFile.exists()) {
                slvFile.delete();
            }
            slvFile.createNewFile();
            getSLVDetails(slvDataPath);
        //String slvFileUploadUrl = properties.getProperty("com.edge.slv.file.upload.url");
        //String updateTableUrl = properties.getProperty("com.edge.update.table.url");
        //File slvDataFile = new File(slvDataPath);
        /*if(slvDataFile.exists()) {
            sendFilesToUpdateTable(updateTableUrl,slvDataFile,"slvdata",";",false);
//            sendFilesToEdgeServer(slvFileUploadUrl, slvDataFile);
        }*/
        //Utils.createZipFile(slvDataPath,slvDataPath.replace(".csv",".zip"));
        //Utils.uploadFileToDropBox(slvFile);
        System.out.println("finished*******");
        }catch (Exception e){
            logger.error("Error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void getSLVDetails(String slvDataPath){
        BufferedWriter bufferedWriter = null;
        try{
            String baseUrl = properties.getProperty("com.slv.server");
            String restMethodName = properties.getProperty("com.slv.getvalues");
            String rootGeoZoneId = properties.getProperty("com.slv.root.geo.zone.id");
            int pageSize = Integer.valueOf(properties.getProperty("com.slv.items.download.per.page"));
            List<GeoZone> geoZones = getAllGeoZones(baseUrl);
            bufferedWriter = new BufferedWriter(new FileWriter(new File("./error/error_"+Utils.getDateTime(new Date().getTime())+".csv")));
            bufferedWriter.append("geozonename,geozoneid,count,type,error\n");
            int totalDeviceCount = 0;
            int processedDeviceCount = 0;
            int pageIndex = 0;
            boolean isHeaderNeeded = true;
            System.out.println("Total geozones count: "+geoZones.size());
            logger.info("Total geozones count: "+geoZones.size());
            int processedCount = 0;
            for(GeoZone geoZone : geoZones){
                logger.info("Geozone name: "+geoZone.getName());
                logger.info("Geozone children count: "+geoZone.getChildrenCount());
                logger.info("Geozone device count: "+geoZone.getDevicesCount());
                if(geoZone.getDevicesCount() > 0) {
                    String extraParameters = "&geozoneId="+geoZone.getId()+"&pageSize="+geoZone.getDevicesCount()+"&pageIndex=0";
                    totalDeviceCount = totalDeviceCount + geoZone.getDevicesCount();
                    callSlv(baseUrl,slvDataPath,restMethodName,extraParameters,pageIndex,isHeaderNeeded,geoZone,bufferedWriter,0);
                    if(isHeaderNeeded){
                        isHeaderNeeded = false;
                    }
                }
                System.out.println("Processed geozones count: "+processedCount++);
                logger.info("Processed geozones count: "+processedCount);
                System.out.println("Total geozones count: "+geoZones.size());
                logger.info("Total geozones count: "+geoZones.size());
            }
            logger.info("Total slv devices count: "+totalDeviceCount);
            /*do{
                logger.info("*************************************");
                logger.info("Going to call createDevicesLastValuesExtAsPaginatedArrayCsvFileUrl");
                logger.info("page size: "+pageSize);
                logger.info("page index: "+pageIndex);
                logger.info("processed device count before processing: "+processedDeviceCount);
                logger.info("*************************************");
                callSlv(baseUrl,slvDataPath,restMethodName,extraParameters,pageIndex);
                processedDeviceCount = processedDeviceCount+pageSize;
                pageIndex++;
            }while (processedDeviceCount < totalDeviceCount);*/
        }catch (Exception e) {
            logger.error("Error in getSLVDetails method: "+e.getMessage());
        }finally {
            if(bufferedWriter != null){
                try{
                   bufferedWriter.close();
                }catch (Exception e){
                }
            }
        }
    }

    private void callSlv(String baseUrl, String slvDataPath,String restMethodName,String extraParameters,int pageIndex,boolean isHeaderNeeded,GeoZone geoZone,BufferedWriter bufferedWriter,int retryCount){
        ResponseEntity<String> responseEntity = call(baseUrl+restMethodName+(isHeaderNeeded ? "true" : "false")+extraParameters+pageIndex,null,HttpMethod.POST,true);
        if(responseEntity.getStatusCode() == HttpStatus.OK){
            String body = responseEntity.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(body);
            System.out.println("response= "+jsonObject);
            if(jsonObject.has("value")){
                String downloadPath = jsonObject.get("value").getAsString();
                downloadFile(downloadPath,slvDataPath,geoZone,bufferedWriter,0);
            }
        }else{
            if(retryCount >=5){
                // error log
                try {
                    bufferedWriter.append(geoZone.getName() + "," + geoZone.getId()+","+geoZone.getDevicesCount() + "," + "slv," + responseEntity.getBody() + "\n");
                }catch (Exception e){
                    logger.error("Error while writting error file: ",e);
                }
                logger.error("Error while fetching data from this geozone max retrycount reached: "+gson.toJson(geoZone)+" Error code: "+responseEntity.getStatusCode()+" error: "+responseEntity.getBody());
            }else{
                retryCount = retryCount+1;
                callSlv(baseUrl, slvDataPath, restMethodName, extraParameters, pageIndex, isHeaderNeeded, geoZone, bufferedWriter, retryCount);
            }
        }
        System.out.println(responseEntity.getStatusCode());
    }
    private List<GeoZone> getAllGeoZones(String baseUrl){
        List<GeoZone> geoZones = new ArrayList<>();
        try {
            String allGeoZoneUrl = properties.getProperty("com.slv.get.all.geo.zones.url");
            ResponseEntity<String> responseEntity = call(baseUrl + allGeoZoneUrl, null, HttpMethod.POST, true);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    geoZones = gson.fromJson(body, new TypeToken<List<GeoZone>>() {
                    }.getType());
                }
            }
        }catch (Exception e){
            logger.error("Error in getAllGeoZones method: "+e.getMessage());
        }
        return geoZones;
    }
    private void downloadFile(String downloadPath,String slvDataPath,GeoZone geoZone,BufferedWriter bufferedWriter,int retrycount){
        ResponseEntity<byte[]> responseEntity1 = download(properties.getProperty("com.slv.server")+downloadPath,null,HttpMethod.GET,true);
        if(responseEntity1.getStatusCode() == HttpStatus.OK){
            byte[] bytes = responseEntity1.getBody();
            try {
                Files.write(Paths.get(slvDataPath), bytes, StandardOpenOption.APPEND);
            }catch (Exception e){
                logger.error("Error while downloadFile: ",e);
            }
        }else{
            if(retrycount >= 5){
                //error log
                try {
                    bufferedWriter.append(geoZone.getName() + "," + geoZone.getId() + ","+geoZone.getDevicesCount() + "," + "download," + responseEntity1.getBody() + "\n");
                }catch (Exception e){
                    logger.error("Error while writting error file in download: ",e);
                }
                logger.error("Error while downloading data from this geozone max retrycount reached: "+gson.toJson(geoZone)+" Error code: "+responseEntity1.getStatusCodeValue());
            }else{
                retrycount = retrycount+1;
                downloadFile(downloadPath,slvDataPath,geoZone,bufferedWriter,retrycount);
            }
        }
    }
}
