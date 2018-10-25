package com.terragoedge.streetlight.edgeinterface;


import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ReadCSvservice {
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;
    private SlvToEdgeService slvToEdgeService;
    private StreetlightDao streetlightDao;

    public ReadCSvservice() {
        slvToEdgeService = new SlvToEdgeService();
        streetlightDao = new StreetlightDao();
    }

    public void start() {
        String filePath ="./resource/input.csv";
        List<SlvData> slvDataList = getSlvDataFromCSV(filePath);
        try {
            for (SlvData slvData : slvDataList) {
                if(slvData.getStatus() == null){
                    try{
                        slvToEdgeService.run(slvData);
                    }catch (Exception e){
                        slvData.setStatus("Failure");
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        beanToCSV(slvDataList);
    }


    private void beanToCSV(List<SlvData> slvData){
        Writer writer = null;
        try{
            String filePath =  "./res.csv";
            writer = new FileWriter(filePath);
            StatefulBeanToCsv<SlvData> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(slvData);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(writer != null){
                try{
                    writer.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }

    }

    public List<SlvData> getSlvDataFromCSV(String filePath) {
        List<SlvData> slvDataList = new ArrayList<SlvData>();
        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String currentRow;
            while ((currentRow = bufferedReader.readLine()) != null) {
                String values[] = currentRow.split(",");
                SlvData slvData = new SlvData();
                String tt = values[1]+values[2];
                slvData.setNoteTitle(tt);
                slvData.setComponentValue(values[0]);
               List<com.terragoedge.edgeserver.SlvData> dbSlvDataList = streetlightDao.getNoteDetails(slvData.getNoteTitle());
               if(dbSlvDataList.size() == 0){
                   slvData.setStatus("Failure");
                   slvData.setErrorDetails("Given Title not matched.");
               }else if(dbSlvDataList.size() > 1){
                   slvData.setStatus("Failure");
                   String tem = "";
                   for(com.terragoedge.edgeserver.SlvData slvData1 : dbSlvDataList){
                       tem = tem + slvData1.getTitle()+" | ";
                   }
                   slvData.setErrorDetails("Given Title has more than one record."+tem);
               }else{
                   List<com.terragoedge.edgeserver.SlvData> newSlvDataList =  streetlightDao.getNoteDetails(values[0]);
                  /* com.terragoedge.edgeserver.SlvData  slvData1 = dbSlvDataList.get(0);
                   slvData.setNoteGuid(slvData1.getGuid());
                   slvData.setComponentId("74");*/
                    if(newSlvDataList.size() > 0){
                        slvData.setStatus("Failure");
                        slvData.setErrorDetails("New Title already present.");
                    }else{
                        com.terragoedge.edgeserver.SlvData  slvData1 = dbSlvDataList.get(0);
                        slvData.setNoteGuid(slvData1.getGuid());
                        slvData.setComponentId("74");
                    }


                   slvDataList.add(slvData);
               }

            }
            System.out.println("Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in start method : " + e.toString());
        } finally {
            closeBufferedReader(bufferedReader);
            closeFileReader(fileReader);
        }
        return slvDataList;
    }

    public void closeBufferedReader(BufferedReader bufferedReader) {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeFileReader(FileReader fileReader) {
        if (fileReader != null) {
            try {
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
