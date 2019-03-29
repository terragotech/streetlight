package com.terragoedge.streetlight.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.terragoedge.streetlight.json.model.HistoryModel;
import com.terragoedge.streetlight.json.model.HistoryResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryService extends AbstractProcessor {
    final Logger logger = Logger.getLogger(AbstractProcessor.class);

    public void start() {
        List<HistoryModel> historyModelList = new ArrayList<>();
        BufferedReader bufferedReader = null;
        String sourcepath = properties.getProperty("streetlight.location.inputpath");

        try {
            bufferedReader = new BufferedReader(new FileReader(sourcepath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                logger.info("Current Note Title:" + line);
                HistoryModel historyModel = new HistoryModel();
                historyModel.setIdOnController(line);
                process(historyModel);
                historyModelList.add(historyModel);
            }
        } catch (Exception e) {
            logger.error("Error in  history.", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // write csv historyModelList
        generateCSV(historyModelList);

    }

    public void process(HistoryModel historyModel) {
        loadDeviceId(historyModel);
        String historyJson = getDeviceHistory(historyModel);
        if (historyJson != null) {
            Type listType = new TypeToken<ArrayList<HistoryResponse>>() {
            }.getType();
            List<HistoryResponse> historyResponses = gson.fromJson(historyJson, listType);
            if (historyResponses.size() > 0) {
                HistoryResponse historyResponse = historyResponses.get(0);
                logger.info("historyresponse" + gson.toJson(historyResponse));
                String comment = (historyResponse.getInfo() != null) ? historyResponse.getInfo().getComment() : "";
                String user = (historyResponse.getInfo() != null) ? historyResponse.getInfo().getUser() : "";
                historyModel.setComment(comment);
                historyModel.setUser(user);
                historyModel.setEventTime(historyResponse.getEventTime());
                historyModel.setValue(historyResponse.getValue());
                historyModel.setUpdateTime(historyResponse.getUpdateTime());
            }
        }
        historyModel.setHistoryJson(historyJson);
    }

    private void generateCSV(List<HistoryModel> historyModelList) {
        Writer writer = null;
        try {
            String filePath = properties.getProperty("streetlight.device.historypath");
            // Creating date format
            DateFormat simple = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss");
            Date result = new Date(System.currentTimeMillis());
            System.out.println(simple.format(result));
            //String filePath = "D:/data/idoncontroller_history.csv";
            writer = new FileWriter(filePath+simple.format(result)+".csv");
            StatefulBeanToCsv<HistoryModel> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(historyModelList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadDeviceId(HistoryModel historyModel) {
        try {
            logger.info("Load default device method called");
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String deviceUrl = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + deviceUrl;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue=" + historyModel.getIdOnController());
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeOperator=eq-i");
            paramsList.add("maxResults=1");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("Load Device url :" + url);
            ResponseEntity<String> response = restService.getRequest(url, true, null);
            if (response.getStatusCodeValue() == 200) {
                logger.info("LoadDevice Respose :" + response.getBody());
                String responseString = response.getBody();
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :" + id);
                historyModel.setDeviceId(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDeviceHistory(HistoryModel historyModel) {
        try {
            if (historyModel.getDeviceId() > 0) {
                logger.info("GetDeviceHistory called");
                String mainUrl = properties.getProperty("streetlight.slv.url.main");
                String commentUrl = properties.getProperty("streetlight.slv.url.comment.get");
                String url = mainUrl + commentUrl;
                List<String> paramsList = new ArrayList<>();
                paramsList.add("returnTimeAges=false");
                paramsList.add("param0=" + historyModel.getDeviceId());
                paramsList.add("slvSystemServiceRequestTime=" + System.currentTimeMillis());
                paramsList.add("valueName=MacAddress");
                paramsList.add("ser=json");
                String params = StringUtils.join(paramsList, "&");
                url = url + "?" + params;
                logger.info("Get MAC Address url :" + url);
                ResponseEntity<String> response = restService.getRequest(url, true, null);
                if (response.getStatusCodeValue() == 200) {
                    logger.info("Get MAC Address Respose :" + response.getBody());
                    String responseString = response.getBody();
                    return responseString;
                }
            }
        } catch (Exception e) {
            logger.error("Error in getComment", e);
        }
        return null;
    }

}