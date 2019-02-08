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
       // String filePath = "./resources/input.csv";
        String filePath = "D:/Report/input.csv";
        List<SlvData> slvDataList = getSlvDataFromCSV(filePath);
        try {
            for (SlvData slvData : slvDataList) {
                    try {
                        slvToEdgeService.run(slvData);
                    } catch (Exception e) {
                        slvData.setStatus("Failure");
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        beanToCSV(slvDataList);
    }


    private void beanToCSV(List<SlvData> slvData) {
        Writer writer = null;
        try {
            String filePath = "./res.csv";
            writer = new FileWriter(filePath);
            StatefulBeanToCsv<SlvData> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(slvData);
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

    public List<SlvData> getSlvDataFromCSV(String filePath) {
        List<SlvData> slvDataList = new ArrayList<SlvData>();
        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String currentRow;
            while ((currentRow = bufferedReader.readLine()) != null) {
                String values[] = currentRow.split(",");
                SlvData slvData = new SlvData();
                slvData.setNoteTitle(values[0]);
                slvData.setNoteGuid(values[1]);
                slvData.setMacAddress(values[2]);
                slvData.setFixtureQRScan(values[3]);
                slvDataList.add(slvData);
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
