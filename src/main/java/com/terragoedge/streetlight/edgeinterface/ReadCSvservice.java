package com.terragoedge.streetlight.edgeinterface;


import com.terragoedge.streetlight.PropertiesReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReadCSvservice {
    BufferedReader bufferedReader = null;
    FileReader fileReader = null;
    private SlvToEdgeService slvToEdgeService;

    public ReadCSvservice() {
        slvToEdgeService = new SlvToEdgeService();
    }

    public void start() {
        String filePath = PropertiesReader.getProperties().getProperty("edge.csv.filepath");
        List<SlvData> slvDataList = getSlvDataFromCSV(filePath);
        try {
            for (SlvData slvData : slvDataList) {
                slvToEdgeService.run(slvData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SlvData> getSlvDataFromCSV(String filePath) {
        List<SlvData> slvDataList = new ArrayList<SlvData>();
        try {
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String currentRow;
            boolean isFirst = false;
            while ((currentRow = bufferedReader.readLine()) != null) {
                String values[] = currentRow.split(",");
                SlvData slvData = new SlvData();
                //slvData.setGuid(values[0]);
                slvData.setNoteTitle(values[0]);
                slvData.setNoteGuid(values[1]);
                slvData.setComponentId(values[2]);
                slvData.setComponentValue(values[3]);
                slvDataList.add(slvData);
            }
            System.out.println("Successfully Removed");
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
