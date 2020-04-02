package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.opencsv.CSVWriter;
import com.terragoedge.slvinterface.dao.OutageDAO;
import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;
import com.terragoedge.slvinterface.dao.tables.OutageEntity;
import com.terragoedge.slvinterface.model.ErrorMessageModel;
import com.terragoedge.slvinterface.model.FailureReportModel;
import com.terragoedge.slvinterface.model.GeozoneModel;
import com.terragoedge.slvinterface.model.OutageData;
import com.terragoedge.slvinterface.utils.DataChecker;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OutageReportService {
    SlvRestService slvRestService;
    Gson gson;
    JsonParser jsonParser;
    final Logger logger = Logger.getLogger(OutageReportService.class);
    ConnectionSource connectionSource;
    private Dao<OutageEntity,String> outageEntityDAO;

    public OutageReportService() throws SQLException {
        slvRestService = new SlvRestService();
        gson = new Gson();
        jsonParser = new JsonParser();
        connectionSource = OutageDAO.INSTANCE.getConnectionSource();
        outageEntityDAO = DaoManager.createDao(connectionSource, OutageEntity .class);

        try{
            TableUtils.createTable(connectionSource, OutageEntity.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void addOutageData(OutageEntity outageEntity) throws SQLException
    {
        outageEntityDAO.create(outageEntity);
    }
    private List<GeozoneModel> getGeozoneModelList(String url, String geoZoneURL) {

        String slv_config = PropertiesReader.getProperties().getProperty("slv_config_use_x_csrf");

        List<GeozoneModel> geozoneModels = new ArrayList<>();
        if(slv_config.equals("false"))
        {
            ResponseEntity<String> responseEntity = slvRestService.getPostRequest(url, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseJson = responseEntity.getBody();
                geozoneModels = gson.fromJson(responseJson, new TypeToken<List<GeozoneModel>>() {
                }.getType());
                return geozoneModels;
            } else {
                logger.error("Unable to get message from EdgeServer. Response Code is :" + responseEntity.getStatusCode());
            }
        }
        else
        {
            try {
                String slvmainurl = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
                ResponseEntity<String> responseEntity = slvRestService.getPostRequest(slvmainurl +geoZoneURL,null);
                if(responseEntity.getStatusCode() == HttpStatus.OK)
                {
                    String responseString = responseEntity.getBody();
                    geozoneModels = gson.fromJson(responseString, new TypeToken<List<GeozoneModel>>() {
                    }.getType());
                    return geozoneModels;

                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }

        }

        return new ArrayList<GeozoneModel>();

    }
    public JsonObject getFailureReport(GeozoneModel geozoneModel) {
        String slv_config = PropertiesReader.getProperties().getProperty("slv_config_use_x_csrf");

        //return (JsonObject) jsonParser.parse(tempResponse);
        String url = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
        String failureUrl = PropertiesReader.getProperties().getProperty("streetlight.slv.failurereport");
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("groupId=" + geozoneModel.getId());
        paramsList.add("reportBuilderClassName=FailuresApplicationReportBuilder");
        paramsList.add("ser=json");
        paramsList.add("reportPropertyName=detail");
        paramsList.add("reportPropertyValue=2");
        paramsList.add("time=" + System.currentTimeMillis());
        String params = StringUtils.join(paramsList, "&");
        String failureReportUrl = url + failureUrl + "?" + params;
        String failureReportUrl1 = url + failureUrl + "?" + params;
        logger.info("Url to get Failure Report:");
        logger.info("Url:" + failureReportUrl);
        if(slv_config.equals("false"))
        {
            ResponseEntity<String> response = slvRestService.getPostRequest(failureReportUrl, null);
            logger.info("Response Code:" + response.getStatusCodeValue());
            if (response.getStatusCodeValue() == 200) {
                String responseString = response.getBody();
                logger.info("--------Response----------");
                logger.info(responseString);
                // return (JsonObject) jsonParser.parse(tempResponse);
                return (JsonObject) jsonParser.parse(responseString);
            }
        }
        else
        {
            try {
                ResponseEntity<String> response = slvRestService.getPostRequest(failureReportUrl1,null);
                if(response.getStatusCodeValue() == 200)
                {
                    String responseString = response.getBody();
                    return (JsonObject) jsonParser.parse(responseString);
                }

            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
        return null;

    }
    public void processFailureReport(GeozoneModel geozoneModel, List<FailureReportModel> failureReportModelList) {


        try {
            // logger.info("Getting Failure Report for "+geozoneModel.getId());

            JsonObject jsonObject = getFailureReport(geozoneModel);

            if (jsonObject != null) {
                JsonObject jsonSubObject = jsonObject.get("properties").getAsJsonObject();
                JsonElement jsonElement = jsonSubObject.get("rows");
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (JsonElement jsonElementObject : jsonArray) {
                    FailureReportModel failureReportModel = new FailureReportModel();
                    failureReportModelList.add(failureReportModel);
                    JsonObject failureObject = jsonElementObject.getAsJsonObject();
                    String title = null;
                    String properties = null;
                    if (failureObject.get("label") != null) {
                        title = failureObject.get("label").getAsString();
                        failureReportModel.setLabel(title);

                        /* - Commented for testing
                        String splitValues[] = title.split("-");
                        if (splitValues.length > 3) {
                            failureReportModel.setFixtureId(splitValues[2]);
                        }*/
                        failureReportModel.setFixtureId(title);
                    }
                    if (failureObject.get("properties") != null) {
                        properties = failureObject.get("properties").toString();
                        failureReportModel.setProperties(properties);
                    }
                    JsonArray jsonValuesArray = failureObject.get("value").getAsJsonArray();
                    setFailureModelObject(jsonValuesArray, failureReportModel);
                }
            }
        } catch (Exception e) {
            logger.error("Error in processFailureReport", e);
        }

    }
    public void setFailureModelObject(JsonArray jsonValuesArray, FailureReportModel failureReportModel) {
        String lifeTime = "";
        String burningHours = "";
        String failureSince = "";
        String lastUpdate = "";
        boolean warning = false;
        boolean outage = false;
        System.out.println(jsonValuesArray.toString());
        if (!jsonValuesArray.get(0).isJsonNull()) {
            warning = jsonValuesArray.get(0).getAsBoolean();
        }
        if (!jsonValuesArray.get(1).isJsonNull()) {
            outage = jsonValuesArray.get(1).getAsBoolean();
        }
        if (!jsonValuesArray.get(2).isJsonNull()) {
            JsonArray errorMessage = jsonValuesArray.get(2).getAsJsonArray();
            if (errorMessage != null && !errorMessage.equals("(null)")) {
                List<String> messageList = new ArrayList<>();
                List<ErrorMessageModel> errorMessageModelList = gson.fromJson(errorMessage, new TypeToken<List<ErrorMessageModel>>() {
                }.getType());
                for (ErrorMessageModel errorMessageModel : errorMessageModelList) {
                    messageList.add(errorMessageModel.getLabel());
                }
                String finalErrorReport = StringUtils.join(messageList, ",");
                failureReportModel.setFailureReason(finalErrorReport);
            }

        }
        if (!jsonValuesArray.get(3).isJsonNull()) {
            lastUpdate = jsonValuesArray.get(3).getAsString();
        }
        if (!jsonValuesArray.get(4).isJsonNull()) {
            failureSince = jsonValuesArray.get(4).getAsString();
        }
        if (!jsonValuesArray.get(5).isJsonNull()) {
            burningHours = jsonValuesArray.get(5).getAsString();
        }
        if (!jsonValuesArray.get(6).isJsonNull()) {
            lifeTime = jsonValuesArray.get(6).toString();
        }
        failureReportModel.setWarning(warning);
        failureReportModel.setOutage(outage);
        failureReportModel.setLastUpdate(lastUpdate);
        failureReportModel.setFailedSince(failureSince);
        failureReportModel.setBurningHours(burningHours);
        failureReportModel.setLifeTime(lifeTime);
    }
    private List<FailureReportModel> getMostRecentEvent(List<FailureReportModel> lstFailureEventModel){
        int tc = lstFailureEventModel.size();
        List<FailureReportModel> result = new ArrayList<>();
        for(int idx=0;idx<tc;idx++)
        {
            FailureReportModel curElement = lstFailureEventModel.get(idx);
            FailureReportModel mostRecent = curElement;
            for(int jdx=0;jdx<tc;jdx++)
            {
                FailureReportModel curElement1 = lstFailureEventModel.get(jdx);
                if(curElement.getFixtureId()==curElement1.getFixtureId())
                {
                    String eventDate1 = curElement.getLastUpdate();
                    String eventDate2 = curElement1.getLastUpdate();
                    if(eventDate1.equals("") || eventDate2.equals(""))
                    {
                        if(eventDate1.equals("") && !eventDate2.equals(""))
                        {
                            mostRecent = curElement1;
                        }
                        else if(eventDate2.equals("") && !eventDate1.equals(""))
                        {
                            mostRecent = curElement;
                        }
                        else
                        {
                            mostRecent = curElement;
                        }
                    }
                    else
                    {
                        if (greater(eventDate1, eventDate2)) {
                            mostRecent = curElement;
                        } else {
                            mostRecent = curElement1;
                        }
                    }

                }
            }
            result.add(mostRecent);
        }
        return result;
    }
    private boolean greater(String strDateParam1,String strDateParam2)
    {
        boolean result = false;
        String []dValue1 = strDateParam1.split(" ");
        String []dValue2 = strDateParam2.split(" ");
        String dateValue1 = dValue1[0];
        String dateValue2 = dValue2[0];
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = format.parse(dateValue1);
            Date date2 = format.parse(dateValue2);
            if(date1.getTime() >= date2.getTime())
            {
                result = true;
            }
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }
        return result;
    }
    public void generateOutageReport(){
        String url = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
        url = url + PropertiesReader.getProperties().getProperty("streetlight.slv.geozoneUrl");
        List<GeozoneModel> geozoneModelList = getGeozoneModelList(url,PropertiesReader.getProperties().getProperty("streetlight.slv.geozoneUrl"));
        File file = new File("./Outage-Report.csv");
        String []headerValues = {
                //"syncid",
                "fixtureId",
                "label",
                "deviceCategory",
                "deviceId",
                "warning",
                "outage",
                "isComplete",
                "failure Reason",
                "lastUpdate",
                "failedSince",
                "burningHours",
                "lifeTime"};
        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(headerValues);

            for (GeozoneModel geozoneModel : geozoneModelList) {
                if (geozoneModel.getChildrenCount() == 0) {
                    List<FailureReportModel> failureReportModelList = new ArrayList<>();
                    processFailureReport(geozoneModel, failureReportModelList);
                    failureReportModelList = getMostRecentEvent(failureReportModelList);
                    for (FailureReportModel failureReportModel : failureReportModelList) {
                        logger.info("ProcessForm Started Title " + failureReportModel.toString());
                        String strWarning = "";
                        String strOutage = "";
                        String strComplete = "";
                        OutageData outageData = new OutageData();
                        outageData.setLabel(failureReportModel.getLabel());
                        if (failureReportModel.isWarning()) {
                            strWarning = "true";
                        } else {
                            strWarning = "false";
                        }
                        outageData.setWarning(strWarning);
                        if (failureReportModel.isOutage()) {
                            strOutage = "true";
                        } else {
                            strOutage = "false";
                        }
                        outageData.setOutage(strOutage);
                        if (failureReportModel.isComplete()) {
                            strComplete = "true";
                        } else {
                            strComplete = "false";
                        }
                        outageData.setIscomplete(strComplete);
                        String strProperties = DataChecker.checkForNull(failureReportModel.getProperties());
                        String v1 = "";
                        String v2 = "";
                        if (!strProperties.equals("")) {
                            JsonObject jsonObject = (JsonObject) jsonParser.parse(strProperties);
                            v1 = jsonObject.get("deviceCategory").getAsString();
                            v2 = jsonObject.get("deviceId").getAsString();
                            outageData.setDevicecategory(v1);
                            outageData.setDeviceid(v2);
                        }
                        String[] dataValues = {
                                //DataChecker.convertToString(failureReportModel.getSyncid()),
                                DataChecker.checkForNull(failureReportModel.getFixtureId()),
                                DataChecker.checkForNull(failureReportModel.getLabel()),
                                v1,
                                v2,
                                strWarning,
                                strOutage, strComplete,
                                DataChecker.checkForNull(failureReportModel.getFailureReason()),
                                DataChecker.checkForNull(failureReportModel.getLastUpdate()),
                                DataChecker.checkForNull(failureReportModel.getFailedSince()),
                                DataChecker.checkForNull(failureReportModel.getBurningHours()),
                                DataChecker.checkForNull(failureReportModel.getLifeTime())
                        };
                        outageData.setFixtureid(DataChecker.checkForNull(failureReportModel.getFixtureId()));
                        outageData.setFailure_reason(DataChecker.checkForNull(failureReportModel.getFailureReason()));
                        outageData.setLastupdate(DataChecker.checkForNull(failureReportModel.getLastUpdate()));
                        outageData.setFailedsince(DataChecker.checkForNull(failureReportModel.getFailedSince()));
                        outageData.setBurninghours(DataChecker.checkForNull(failureReportModel.getBurningHours()));
                        outageData.setLifetime(DataChecker.checkForNull(failureReportModel.getLifeTime()));

                        OutageEntity outageEntity = new OutageEntity();
                        outageEntity.setFixtureid(outageData.getFixtureid());
                        outageEntity.setLabel(outageData.getLabel());
                        outageEntity.setDevicecategory(outageData.getDevicecategory());
                        outageEntity.setDeviceid(outageData.getDeviceid());
                        outageEntity.setWarning(outageData.getWarning());
                        outageEntity.setOutage(outageData.getOutage());
                        outageEntity.setIscomplete(outageData.getIscomplete());
                        outageEntity.setFailurereason(outageData.getFailure_reason());
                        outageEntity.setLastupdate(outageData.getLastupdate());
                        outageEntity.setFailedsince(outageData.getFailedsince());
                        outageEntity.setBurninghours(outageData.getBurninghours());
                        outageEntity.setLifetime(outageData.getLifetime());
                        addOutageData(outageEntity);
                        //}

                        writer.writeNext(dataValues);

                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        ////////////////////////////////////////////

    }
}
