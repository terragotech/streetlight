package com.terragoedge.streetlight.service;

import com.terragoedge.streetlight.dao.StreetlightDao;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DuplicateRecordProcessor {

    final Logger logger = Logger.getLogger(DuplicateRecordProcessor.class);

    StreetlightDao streetlightDao = null;
    RestTemplate restTemplate = null;
    public DuplicateRecordProcessor(){
        streetlightDao = new StreetlightDao();
        restTemplate = new RestTemplate();
    }


    public void doProcess(){
       boolean flag = false;
        do{
            Random random = new Random();
            int val = random.nextInt(1000);
            String uuid = UUID.randomUUID().toString()+"_"+val;
            streetlightDao.updateRanValue(uuid);
            List<String> needToProcess =  streetlightDao.getDuplicateRecords(uuid);

            if(needToProcess.size() > 0){
                flag = true;
                doRequest(needToProcess,uuid+".txt");
            }else{
                flag = false;
            }
        }while (flag);
    }


    public void doRequest(List<String> needToProcess,String uuid){
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream("./input/"+uuid);
            for(String ddd : needToProcess){
                ddd = ddd +"\n";
                fos.write(ddd.getBytes());

            }
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("file", new FileSystemResource("./input/"+uuid));
            String response = restTemplate.postForObject("https://amerescousa.terragoedge.com/edgeServer/import/zeroLocation",map,String.class);
            logger.info(response);
        }catch (Exception e){
            logger.error("Error in doRequest",e);
        }finally {
            if(fos != null){
                try{
                    fos.close();
                    File ff = new File("./input/"+uuid);
                    ff.delete();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}
