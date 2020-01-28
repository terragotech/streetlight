package com.terragoedge.streetlight.edgeinterface;


import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.terragoedge.edgeserver.LatLong;
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
        String filePath = "./resources/slv_to_edge_location_update_list.csv";
        //String filePath = "D:\\works\\2019\\terragoedge\\slvPhyChange\\dataAnalysis\\edge_to_slv_location_update_list1.csv";
        List<SlvData> slvDataList = null;
        try {
            slvDataList = getSlvDataFromCSV(filePath);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        try {
            int count=0;
            for (SlvData slvData : slvDataList) {

                System.out.println("ProcessTitle :"+slvData.getNoteTitle());
                    try {
                        slvToEdgeService.run(slvData);
                    } catch (Exception e) {
                        slvData.setStatus("Failure");
                    }
                    count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //beanToCSV(slvDataList);
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
    private LatLong parseLatLong(String pointWKTString)
    {
        LatLong latLong = null;
        if(pointWKTString != null)
        {
            pointWKTString = pointWKTString.replace("POINT(","");
            pointWKTString = pointWKTString.replace(")","");
            String cooridinates[] = pointWKTString.split(" ");
            System.out.println(cooridinates[0]);
            System.out.println(cooridinates[1]);
            latLong = new LatLong();
            latLong.setLongitude(cooridinates[0]);
            latLong.setLatitude(cooridinates[1]);
        }
        return latLong;
    }
    public List<SlvData> getSlvDataFromCSV(String filePath) throws Exception{
        List<SlvData> slvDataList = new ArrayList<SlvData>();

        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String currentRow;
            while ((currentRow = bufferedReader.readLine()) != null) {
                String values[] = currentRow.split(",");
                if(values.length < 5)
                {
                    throw new Exception("Bad invalid data ...");
                }
                SlvData slvData = new SlvData();
                slvData.setNoteTitle(values[0]);
                LatLong latLng = parseLatLong(values[4]);
                if(latLng != null)
                {
                    slvData.setSlvLongitude(latLng.getLongitude());
                    slvData.setSlvLatitude(latLng.getLatitude());
                }
                //Correction for NoteBook
                //slvData.setLocation_atlasphysicalpage(values[1]);
              //  slvData.setNoteGuid(values[0]);
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
