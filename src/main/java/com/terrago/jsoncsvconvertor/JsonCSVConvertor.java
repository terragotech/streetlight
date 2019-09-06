package com.terrago.jsoncsvconvertor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.terrago.jsoncsvconvertor.promoted.PromotedConfig;
import com.terrago.jsoncsvconvertor.promoted.PromotedDatum;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonCSVConvertor {

    public static void main(String[] r)throws Exception{
        Map<String,EdgeData> edgeDataMap = new HashMap<>();
        List<String> dataInList = IOUtils.readLines(new FileInputStream("/Users/Nithish/Documents/office/data-fix/Aug23/comed_data.csv"));
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        ;
        processSLVData(edgeDataMap);
        List<EdgeData> edgeDataList = new ArrayList<>();
        int  i = 0;
        for(String dataRaw : dataInList){

        }



        generateInstallCSVFile(edgeDataList,"/Users/Nithish/Documents/office/data-fix/Aug23/res_1.csv");
    }


    public static void  processEdgeData(Map<String,EdgeData> edgeDataMap)throws  Exception{
        List<String> dataInList = IOUtils.readLines(new FileInputStream("/Users/Nithish/Documents/office/data-fix/Aug23/comed_data.csv"));
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        for(String data : dataInList){
            List<PromotedConfig> promotedConfigList =  gson.fromJson(data,new TypeToken<List<PromotedConfig>>() {
            }.getType());
            for(PromotedConfig promotedConfig : promotedConfigList){
                List<PromotedDatum> promotedDatumList =  promotedConfig.getPromotedData();
                String title = null;
                String cslpNode = null;
                String cslpLum = null;
                String nodeInstall = null;
                String lumInstall = null;
                for(PromotedDatum promotedDatum : promotedDatumList){
                    switch (promotedDatum.getComponentId()){
                        case "3":
                            title = promotedDatum.getValue();
                            break;
                        case "169":
                            cslpNode = promotedDatum.getValue();
                            break;
                        case "170":
                            cslpLum  = promotedDatum.getValue();
                            break;
                        case "171":
                            nodeInstall = promotedDatum.getValue();
                            break;
                        case "162":
                            lumInstall = promotedDatum.getValue();
                            break;
                    }
                }
                if(!edgeDataMap.containsKey(title)){
                    EdgeData edgeData = new EdgeData();
                    edgeDataMap.put(title,edgeData);
                }
                EdgeData edgeData = edgeDataMap.get(title);
                edgeData.setEdgeCslpNodeInstallDate(Long.valueOf(getStringDate(cslpNode)));
                edgeData.setEdgeCslpLumInstallDate(Long.valueOf(getStringDate(cslpLum)));
                edgeData.setEdgeInstallDate(Long.valueOf(getStringDate(nodeInstall)));
                edgeData.setEdgeLumInstallDate(Long.valueOf(getStringDate(lumInstall)));


            }
        }
    }

    public  static String getStringDate(String val){
        try{
            Long.valueOf(val);
            return val;
        }catch (Exception e){

        }
        return "0";
    }


    public static  void  processSLVData(Map<String,EdgeData> edgeDataMap)throws  Exception{
       CSVReader csvReader = new CSVReader(new FileReader(""));
        String[] dataInList = null;
        while((dataInList = csvReader.readNext()) != null){
            EdgeData edgeData = new EdgeData();
            edgeDataMap.put(dataInList[0],edgeData);
            edgeData.setTitle(dataInList[0]);
            edgeData.setSlvCslpNodeInstallDate(dataInList[1].trim().isEmpty() ? 0 : Long.valueOf(dataInList[1].trim()));
            edgeData.setSlvCslpLumInstallDate(dataInList[2].trim().isEmpty() ? 0 : Long.valueOf(dataInList[2].trim()));
            edgeData.setEdgeInstallDate(dataInList[3].trim().isEmpty() ? 0 : Long.valueOf(dataInList[3].trim()));
            edgeData.setSlvLumInstallDate(dataInList[4].trim().isEmpty() ? 0 : Long.valueOf(dataInList[4].trim()));
        }

    }

   /* public static void main_1(String[] r) throws Exception {
       List<String> dataInList = IOUtils.readLines(new FileInputStream("/Users/Nithish/Documents/office/data-fix/Aug21/edge_current_data_aug_21.csv"));
        List<EdgeData> edgeDataList = new ArrayList<>();
       for(String data : dataInList){
           Gson gson = new Gson();
           List<Data> dataList = gson.fromJson(data, new TypeToken<List<Data>>() {
           }.getType());

           EdgeData edgeData = new EdgeData();
           edgeDataList.add(edgeData);
           for (Data data1 : dataList) {
               switch (data1.getId()+"") {
                   case "19":
                       edgeData.setMacAddress(data1.getValue());
                       break;
                   case "26":
                       if(edgeData.getMacAddressRNF() != null){
                           edgeData.setMacAddressRNF(edgeData.getMacAddressRNF() +","+data1.getValue());
                       }else{
                           edgeData.setMacAddressRNF(data1.getValue());
                       }

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

    }*/


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
        records.add(new String[]{"Title", "macAddress","macAddressRNF","macAddressRN","Action"});
        for (EdgeData edgeData : installtionReportJsons) {
            records.add(new String[]{
                    nullCheck(edgeData.getTitle()),
                    nullCheck(edgeData.getMacAddress()),
                    nullCheck(edgeData.getMacAddressRNF()),
                    nullCheck(edgeData.getMacAddressRN()),
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
