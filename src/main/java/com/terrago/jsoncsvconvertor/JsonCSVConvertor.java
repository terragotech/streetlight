package com.terrago.jsoncsvconvertor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonCSVConvertor {

    public static void main(String[] r) throws Exception {
       List<String> dataInList = IOUtils.readLines(new FileInputStream("/Users/Nithish/Documents/office/data-fix/Aug21/edge_current_data_aug_21.csv"));
        List<EdgeData> edgeDataList = new ArrayList<>();
       for(String data : dataInList){
           Gson gson = new Gson();
           List<Data> dataList = gson.fromJson(data, new TypeToken<List<Data>>() {
           }.getType());

           EdgeData edgeData = new EdgeData();
           edgeDataList.add(edgeData);
           for (Data data1 : dataList) {
               switch (data1.getId()) {
                   case "19":
                       edgeData.setMacAddress(data1.getValue());
                       break;
                   case "26":
                       edgeData.setMacAddressRNF(data1.getValue());
                       break;
                   case "36":
                       edgeData.setExMacAddressRNF(data1.getValue());
                       break;
                   case "29":
                       edgeData.setExMacAddressRN(data1.getValue());
                       break;
                   case "30":
                       edgeData.setMacAddressRN(data1.getValue());
                       break;
                   case "createdDateTime":
                       edgeData.setCreateDateTime(Long.valueOf(data1.getValue()));
                       break;
                   case "24":
                       edgeData.setAction(data1.getValue());
                       break;

                   case "title":
                       edgeData.setTitle(data1.getValue());
                       break;

               }
           }
       }
        generateInstallCSVFile(edgeDataList,"/Users/Nithish/Documents/office/data-fix/Aug21/res_1.csv");

    }


    public static void generateInstallCSVFile(List<EdgeData> csvReportModelList, String filePath) throws IOException {
        StringWriter writer = new StringWriter();
        com.opencsv.CSVWriter csvWriter = new CSVWriter(writer, ',');
        List<String[]> data = toStringInstallReportArray(csvReportModelList);
        csvWriter.writeAll(data);
        csvWriter.close();
        System.out.println(writer);
        writeData(writer.toString(), filePath);
    }




    public static List<String[]> toStringInstallReportArray(List<EdgeData> installtionReportJsons) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"Title", "macAddress","exMacAddressRNF","macAddressRNF","exMacAddressRN","macAddressRN","CreateDate","Action"});
        for (EdgeData edgeData : installtionReportJsons) {
            records.add(new String[]{
                    nullCheck(edgeData.getTitle()),
                    nullCheck(edgeData.getMacAddress()),
                    nullCheck(edgeData.getExMacAddressRNF()),
                    nullCheck(edgeData.getMacAddressRNF()),
                    nullCheck(edgeData.getExMacAddressRN()),
                    nullCheck(edgeData.getMacAddressRN()),
                    edgeData.getCreateDateTime()+"",
                    nullCheck(edgeData.getAction())
            });
        }
        return records;
    }


    public static String nullCheck(String str) {
        if (str == null || str.equals("null") || str.equals("(null)") || str.equals("Null")) {
            return "";
        }
        return str;
    }


    public static void writeData(String data, String filePath) {
        System.out.println("OutputFile path is :" + filePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
            System.out.println("Successfully generated CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("file writting problem" + e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
