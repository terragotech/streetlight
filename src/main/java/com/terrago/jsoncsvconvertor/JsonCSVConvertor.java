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
import com.terrago.jsoncsvconvertor.utils.DataConnection;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonCSVConvertor {

private static final Logger logger = Logger.getLogger(JsonCSVConvertor.class);

    public static void main(String[] r)throws Exception{
        Map<String,EdgeData> edgeDataMap = new HashMap<>();
        processSLVData(edgeDataMap);
        processEdgeData(edgeDataMap);
        Collection<EdgeData> edgeDataCollection =  edgeDataMap.values();
        List<EdgeData> results = new ArrayList<>();
        List<EdgeData> dayMatchRes = new ArrayList<>();
       for(EdgeData edgeData : edgeDataCollection){
          boolean isEqual = edgeData.isEqual();
          if(!isEqual){
              boolean dayMatch =  edgeData.compareStartOfDay();
              if(dayMatch){
                  dayMatchRes.add(edgeData);
              }else{
                  results.add(edgeData);
              }

          }
       }
        generateInstallCSVFile(results,getFolderName()+"/res.csv");
        generateInstallCSVFile(dayMatchRes,getFolderName()+"/daymatch.csv");
    }


    public static  String getFolderName(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String folderName = simpleDateFormat.format(new Date());
        File file = new File("./"+folderName+"/");
        file.mkdirs();
        return file.getPath();
    }


    public static void  processEdgeData(Map<String,EdgeData> edgeDataMap)throws  Exception{
        Connection connection = DataConnection.getConnetion();
        Statement statement = null;
        try {
            logger.info("Going to load data from Edge Data.");
            Gson gson = new Gson();
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select promotedvalue from promotedformdata_31");
            while (rs.next()) {
                String promotedValue = rs.getString("promotedvalue");

                List<PromotedConfig> promotedConfigList =  gson.fromJson(promotedValue,new TypeToken<List<PromotedConfig>>() {
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
                            case "172":
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
            logger.info("Edge Data has been loaded.");
        }catch (Exception e){
           logger.error("Error in processEdgeData",e);
        }finally {
            if(statement != null){
                statement.close();
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

//idoncontroller;cslp_node_install_date;cslp_lum_install_date;luminaire_installdate;install_date
    public static  void  processSLVData(Map<String,EdgeData> edgeDataMap)throws  Exception{
        Connection connection = DataConnection.getConnetion();
        Statement statement = null;
        try {
            logger.info("Getting Data from SLV.");
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select idoncontroller,cslp_node_install_date,cslp_lum_install_date,install_date,luminaire_installdate from slvdata");
            while (rs.next()) {

                String idOnController = rs.getString("idoncontroller");
                String cslpNodeInstallDate = rs.getString("cslp_node_install_date");
                String cslpLumInstallDate = rs.getString("cslp_lum_install_date");
                String installDate = rs.getString("install_date");
                String lumInstallDate = rs.getString("luminaire_installdate");


                EdgeData edgeData = new EdgeData();
                edgeDataMap.put(idOnController,edgeData);
                edgeData.setTitle(idOnController);
                edgeData.setSlvCslpNodeInstallDate(toMilli(cslpNodeInstallDate));
                edgeData.setSlvCslpLumInstallDate(toMilli(cslpLumInstallDate));
                edgeData.setSlvLumInstallDate(toMilli(lumInstallDate));
                edgeData.setSlvInstallDate(toMilli(installDate));
            }
            logger.info("SLV Data has been loaded.");
            logger.info("Total Records:"+edgeDataMap.keySet().size());
        }catch (Exception e){
            logger.error("Error in processSLVData",e);
        }finally {
            if(statement != null){
                statement.close();
            }
        }


    }


    private static  long toMilli(String dateVal){
        if(dateVal != null && !dateVal.trim().isEmpty()){
            try{
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                DateTime dt =  fmt.parseDateTime(dateVal);
                return dt.withTimeAtStartOfDay().getMillis();
            }catch (Exception e){


                try{
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                    DateTime dt =  fmt.parseDateTime(dateVal);
                    return dt.withTimeAtStartOfDay().getMillis();
                }catch (Exception e1){


                    try{
                        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                        DateTime dt =  fmt.parseDateTime(dateVal);
                        return dt.withTimeAtStartOfDay().getMillis();
                    }catch (Exception e2){
                        try{
                            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                            DateTime dt =  fmt.parseDateTime(dateVal);
                            return dt.withTimeAtStartOfDay().getMillis();
                        }catch (Exception e3){
                            e3.printStackTrace();
                        }
                    }
                }
            }

        }
        return 0;
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
        records.add(new String[]{"Title", "SLV_CSLP_Node_Date","Edge_CSLP_Node_Date","SLV_CSLP_Lum_Date","Edge_CSLP_Lum_Date","SLV_Lum_Date","Edge_Lum_Date","SLV_Install_Date","Edge_Install_Date"});
        for (EdgeData edgeData : installtionReportJsons) {
            records.add(new String[]{
                    nullCheck(edgeData.getTitle()),
                    nullCheck(String.valueOf(edgeData.getSlvCslpNodeInstallDate())),
                    nullCheck(String.valueOf(edgeData.getEdgeCslpNodeInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvCslpLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getEdgeCslpLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getEdgeLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvInstallDate())),
                    nullCheck(String.valueOf(edgeData.getEdgeInstallDate())),
            });
        }
        return records;
    }


    public static String nullCheck(String str) {
        if (str == null || str.equals("null") || str.equals("(null)") || str.equals("Null") || str.equals("0")) {
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
