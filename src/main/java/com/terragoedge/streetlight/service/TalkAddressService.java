package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TalkAddressService extends AbstractService {
    private Logger logger = Logger.getLogger(TalkAddressService.class);
    ExecutorService executor = null;
    private TalqAddressTask talqAddressTask = null;

    public TalkAddressService() {
    }

    public void getTalqAddress() {
        logger.info("start terragotalqudate");
        int threadCount = Integer.parseInt(PropertiesReader.getProperties().getProperty("edge.threadcount"));
        System.out.println("ThreadCount: " + threadCount);
        logger.info("ThreadCount :" + threadCount);
        executor = Executors.newFixedThreadPool(threadCount);
        List<LoggingModel> unSyncedTalqAddress = getUnsyncedTalqAddress();
        logger.info("------------ Total records ------------------");
        logger.info(unSyncedTalqAddress.size());
        logger.info("------------ end ------------------");
        System.out.println("unSyncedTalqAddress: " + unSyncedTalqAddress);
        for (LoggingModel loggingModel : unSyncedTalqAddress) {
            System.out.println("Process NoteName" + loggingModel.getNoteName());
            logger.info("Process NoteName" + loggingModel.getNoteName());
            Runnable processTask = new TalqAddressTask(loggingModel);
            executor.execute(processTask);
        }
        executor.shutdown();
        System.out.println("All thread Finished");
    }

    public List<LoggingModel> getUnsyncedTalqAddress() {
        List<LoggingModel> loggingModelList = new ArrayList<>();
        BufferedReader fis = null;
        try {
            String data = null;
            System.out.println("Started");

            fis = new BufferedReader(new FileReader("./data/emptytalq.txt"));
            while ((data = fis.readLine()) != null) {
                LoggingModel loggingModel = new LoggingModel();
                try {
                    String[] res = data.split(",");
                    loggingModel.setNoteName(res[0]);
                    loggingModel.setMacAddress(res[1]);
                    loggingModel.setLayerType(res[2]);
                    logger.info("------------------------------process Notes------------------");
                    logger.info(res[0] + " - " + res[1]);
                    logger.info("--------------------------------end----------------------------");
                    System.out.println("IdonController-MAC : " + res[0] + " - " + res[1]);
                    loggingModelList.add(loggingModel);
                    // logger.info("Response Code"+errorCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return loggingModelList;
    }
}
