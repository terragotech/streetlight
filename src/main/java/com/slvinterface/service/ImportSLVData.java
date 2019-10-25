package com.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.dao.SLVDataQueryExecutor;
import com.slvinterface.json.GeoZone;
import com.slvinterface.model.SLVDataInfo;
import com.slvinterface.utils.FileOperationUtils;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImportSLVData {
    SLVRestService slvRestService;
    String slv_server_url;
    String slv_server_user_name;
    String slv_server_password;
    String slv_root_geozone_id;
    String slv_server_geozoneurl;
    String slv_request_parm_url;
    int fixturesPerPage;
    private Gson gson;
    private static final Logger logger = Logger.getLogger(ImportSLVData.class);
    SLVDataQueryExecutor slvDataQueryExecutor;

    public ImportSLVData() throws Exception{
        slvRestService = new SLVRestService();
        slv_server_url = PropertiesReader.getProperties().getProperty("streetlight.slv.base.url");
        slv_server_user_name  = PropertiesReader.getProperties().getProperty("streetlight.slv.username");
        slv_server_password  = PropertiesReader.getProperties().getProperty("streetlight.slv.password");
        slv_root_geozone_id  = PropertiesReader.getProperties().getProperty("streetlight.root.geozone");
        slv_server_geozoneurl  = PropertiesReader.getProperties().getProperty("streetlight.slv.url.geozone");
        slv_request_parm_url = PropertiesReader.getProperties().getProperty("com.slv.getvalues");
        String strFixturePerPage = PropertiesReader.getProperties().getProperty("com.slv.items.download.per.page");
        fixturesPerPage = Integer.parseInt(strFixturePerPage);
        gson = new Gson();
        slvDataQueryExecutor = new SLVDataQueryExecutor();
    }
    private List<GeoZone> getAllGeoZones(String url){
        List<GeoZone> geoZones = new ArrayList<>();
        try {
            String allGeoZoneUrl = url;
            HttpResponse response = slvRestService.callPostMethod(allGeoZoneUrl);
            if(response.getStatusLine().getStatusCode() == 200)
            {
                String responseString = slvRestService.getResponseBody(response);
                if (responseString != null) {
                    geoZones = gson.fromJson(responseString, new TypeToken<List<GeoZone>>() {
                    }.getType());
                }
            }
        }catch (Exception e){
            logger.error("Error in getAllGeoZones method: "+e.getMessage());
            e.printStackTrace();
        }
        return geoZones;
    }
    private void requestSLVValues(String slvDataPath,String methodName,String extraParameters,int pageIndex){
        try {
            JsonParser jsonParser = new JsonParser();
            HttpResponse response = slvRestService.callPostMethod( methodName + (pageIndex == 0 ? "true" : "false") + extraParameters + pageIndex);
            if(response.getStatusLine().getStatusCode() == 200)
            {
                String body = slvRestService.getResponseBody(response);//responseEntity.getBody();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(body);
                System.out.println("response= "+jsonObject);
                if(jsonObject.has("value")){
                    String downloadPath = jsonObject.get("value").getAsString();
                    downloadFile(downloadPath,slvDataPath);
                }
                else if(response.getStatusLine().getStatusCode() == 403){
                    requestSLVValues(slvDataPath,methodName,extraParameters,pageIndex);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private HttpResponse download(String downloadPath,String downloadFileName)
    {
        HttpResponse response = null;
        try {
            response = slvRestService.callGetMethod(slv_server_url + "reports" + downloadPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return response;
    }
    private void downloadFile(String downloadPath,String downloadFileName)
    {
        HttpResponse response = download(downloadPath,downloadFileName);
        if(response != null)
        {
            if(response.getStatusLine().getStatusCode() == 200)
            {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    response.getEntity().writeTo(baos);
                    byte[] bytes = baos.toByteArray();
                    Files.write(Paths.get(downloadFileName), bytes, StandardOpenOption.APPEND);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if(response.getStatusLine().getStatusCode() == 403)
            {
                downloadFile(downloadPath,downloadFileName);
            }
        }

    }
    public SLVDataInfo startImport(){

        String extraParameters = "&geozoneId="+slv_root_geozone_id+"&pageSize="+Integer.toString(fixturesPerPage)+"&pageIndex=";
        List<GeoZone> lstGeoZones = getAllGeoZones(slv_server_geozoneurl);
        int totalDeviceCount = 0;
        int processedDeviceCount = 0;
        int pageIndex = 0;
        for(GeoZone geoZone : lstGeoZones){
            if(geoZone.getChildrenCount() == 0 && geoZone.getDevicesCount() > 0) {
                totalDeviceCount = totalDeviceCount + geoZone.getDevicesCount();
            }
        }
        String folderLocation = ResourceDetails.INBOUND_FILE_STORE+ File.separator +FileOperationUtils.getCurrentDate();
        if(!FileOperationUtils.doesFolderExists(folderLocation))
        {
            FileOperationUtils.createFolder(folderLocation);
        }
        String fileName = "slvdata_" + FileOperationUtils.getCurrentDate() + "_" + FileOperationUtils.getTime() + ".csv";
        String slvFileLocation = folderLocation;
        String slvDataPath = slvFileLocation + File.separator + fileName;

        SLVDataInfo slvDataInfo = new SLVDataInfo();
        slvDataInfo.setFileLocation(slvFileLocation);
        slvDataInfo.setFileName(fileName);
        FileOperationUtils.createFile(slvDataPath);


        do{

            requestSLVValues(slvDataPath,slv_request_parm_url,extraParameters,pageIndex);
            processedDeviceCount = processedDeviceCount+fixturesPerPage;
            pageIndex++;
        }while (processedDeviceCount < totalDeviceCount);
        return slvDataInfo;
    }
    public void loadToDatabase(SLVDataInfo slvDataInfo)
    {
        try {
            FileUtils.copyFile(new File(slvDataInfo.getFileLocation() + File.separator + slvDataInfo.getFileName()), new File(PropertiesReader.getProperties().getProperty("tmppath") + File.separator +  slvDataInfo.getFileName()));
            slvDataQueryExecutor.importSLVData(PropertiesReader.getProperties().getProperty("tmppath") + slvDataInfo.getFileName());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
