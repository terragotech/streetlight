package com.terrago.jsoncsvconvertor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class Test {

    public static void main(String[] rr)throws  Exception{
        Gson gson = new Gson();
        String formDef = "[{\"id\":160,\"label\":\"Fixture ID\",\"value\":\"Fixture ID#(null)\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":161,\"label\":\"Proposed context\",\"value\":\"Proposed context#(null)\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":1,\"label\":\"Existing Fixture Information\",\"value\":\"Existing Fixture Information#Hide\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":3,\"label\":\"Fixture ID\",\"value\":\"Fixture ID#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":7,\"label\":\"Address\",\"value\":\"Address#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":8,\"label\":\"Atlas Physical Page\",\"value\":\"Atlas Physical Page#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":9,\"label\":\"Proposed Fixture Color\",\"value\":\"Proposed Fixture Color#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":16,\"label\":\"Proposed context\",\"value\":\"Proposed context#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":150,\"label\":\"ComEd Project name\",\"value\":\"ComEd Project name#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":12,\"label\":\"Fixture Code\",\"value\":\"Fixture Code#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":13,\"label\":\"Mast arm angle\",\"value\":\"Mast arm angle#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":14,\"label\":\"Mast arm length\",\"value\":\"Mast arm length#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":15,\"label\":\"No of mast arms\",\"value\":\"No of mast arms#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":112,\"label\":\"Talq Address\",\"value\":\"Talq Address#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":159,\"label\":\"Luminaire Type\",\"value\":\"Luminaire Type#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":163,\"label\":\"Mast Arm Color\",\"value\":\"Mast Arm Color#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":164,\"label\":\"Mast Arm Type\",\"value\":\"Mast Arm Type#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":165,\"label\":\"Pole Type\",\"value\":\"Pole Type#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":166,\"label\":\"Pole Height\",\"value\":\"Pole Height#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":162,\"label\":\"Pole Material\",\"value\":\"Pole Material#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":151,\"label\":\"Warranty Start date\",\"value\":\"Warranty Start date#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":152,\"label\":\"Warranty Status\",\"value\":\"Warranty Status#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":173,\"label\":\"SLV MAC address\",\"value\":\"SLV MAC address#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":169,\"label\":\"Proj. Node Install Date\",\"value\":\"Proj. Node Install Date#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":170,\"label\":\"Proj. Fixture Install Date\",\"value\":\"Proj. Fixture Install Date#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":171,\"label\":\"Cur. Node Install Date\",\"value\":\"Cur. Node Install Date#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":172,\"label\":\"Cur. Fixture Install Date\",\"value\":\"Cur. Fixture Install Date#(null)\",\"count\":0,\"groupId\":2,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":17,\"label\":\"Action\",\"value\":\"Action#New\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":19,\"label\":\"Node MAC address\",\"value\":\"Node MAC address#(null)\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":20,\"label\":\"Fixture QR Scan\",\"value\":\"Fixture QR Scan#(null)\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":22,\"label\":\"Install status\",\"value\":\"Install status#Select From Below\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":23,\"label\":\"Skipped Fixture Reason\",\"value\":\"Skipped Fixture Reason#(null)\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":42,\"label\":\"Skipped Reason\",\"value\":\"Skipped Reason#(null)\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":174,\"label\":\"Requires Head-To-Head Wiring Go-Back?\",\"value\":\"Requires Head-To-Head Wiring Go-Back?#(null)\",\"count\":0,\"groupId\":18,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":24,\"label\":\"Repairs \\u0026 Outages\",\"value\":\"Repairs \\u0026 Outages#Replace Node and Fixture\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":36,\"label\":\"Existing Node MAC Address\",\"value\":\"Existing Node MAC Address#(null)\",\"count\":0,\"groupId\":25,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":26,\"label\":\"New Node MAC Address\",\"value\":\"New Node MAC Address#(null)\",\"count\":0,\"groupId\":25,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":38,\"label\":\"Fixture QR Scan\",\"value\":\"Fixture QR Scan#(null)\",\"count\":0,\"groupId\":25,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":29,\"label\":\"Existing Node MAC Address\",\"value\":\"Existing Node MAC Address#(null)\",\"count\":0,\"groupId\":28,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":30,\"label\":\"New Node MAC Address\",\"value\":\"New Node MAC Address#(null)\",\"count\":0,\"groupId\":28,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":39,\"label\":\"New Fixture QR Scan\",\"value\":\"New Fixture QR Scan#(null)\",\"count\":0,\"groupId\":37,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":40,\"label\":\"Reason for Replacement\",\"value\":\"Reason for Replacement#(null)\",\"count\":0,\"groupId\":37,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":120,\"label\":\"Replace Fixture Reason - Other\",\"value\":\"Replace Fixture Reason - Other#(null)\",\"count\":0,\"groupId\":37,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":44,\"label\":\"Issue\",\"value\":\"Issue#(null)\",\"count\":0,\"groupId\":43,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":47,\"label\":\"Dayburner?\",\"value\":\"Dayburner?#(null)\",\"count\":0,\"groupId\":43,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":45,\"label\":\"Add Comment\",\"value\":\"Add Comment#(null)\",\"count\":0,\"groupId\":43,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":46,\"label\":\"Scan Existing MAC if wrong\",\"value\":\"Scan Existing MAC if wrong#(null)\",\"count\":0,\"groupId\":43,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":35,\"label\":\"Reason for removal\",\"value\":\"Reason for removal#(null)\",\"count\":0,\"groupId\":34,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":117,\"label\":\"Scan Existing Node (If Able)\",\"value\":\"Scan Existing Node (If Able)#(null)\",\"count\":0,\"groupId\":34,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":119,\"label\":\"Remove - Comments\",\"value\":\"Remove - Comments#(null)\",\"count\":0,\"groupId\":34,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":102,\"label\":\"Issue\",\"value\":\"Issue#(null)\",\"count\":0,\"groupId\":101,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":103,\"label\":\"Add Comment\",\"value\":\"Add Comment#(null)\",\"count\":0,\"groupId\":101,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":104,\"label\":\"Scan Existing MAC if wrong\",\"value\":\"Scan Existing MAC if wrong#(null)\",\"count\":0,\"groupId\":101,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":124,\"label\":\"Description of repair:\",\"value\":\"Description of repair:#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":125,\"label\":\"Have all deficient items been remedied?\",\"value\":\"Have all deficient items been remedied?#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":126,\"label\":\"Does the light properly function at the end of the repair?\",\"value\":\"Does the light properly function at the end of the repair?#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":144,\"label\":\"# Splices\",\"value\":\"# Splices#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":145,\"label\":\"# Fuses\",\"value\":\"# Fuses#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":146,\"label\":\"# Fuse Kits\",\"value\":\"# Fuse Kits#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":147,\"label\":\"# Harnesses\",\"value\":\"# Harnesses#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":142,\"label\":\"Repair\",\"value\":\"Repair#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":129,\"label\":\"Aerial Fed Outage\",\"value\":\"Aerial Fed Outage#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":130,\"label\":\"Voltage at fixture:\",\"value\":\"Voltage at fixture:#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":131,\"label\":\"Voltage reading on aerial feed:\",\"value\":\"Voltage reading on aerial feed:#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":148,\"label\":\"Aerial Fed Outage - Comments\",\"value\":\"Aerial Fed Outage - Comments#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":132,\"label\":\"Underground Fed Outage\",\"value\":\"Underground Fed Outage#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":133,\"label\":\"Voltage at fixture:\",\"value\":\"Voltage at fixture:#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":134,\"label\":\"Voltage reading at fixture splice:\",\"value\":\"Voltage reading at fixture splice:#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":149,\"label\":\"Underground Fed Outage - Comments\",\"value\":\"Underground Fed Outage - Comments#(null)\",\"count\":0,\"groupId\":122,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":106,\"label\":\"Issue\",\"value\":\"Issue#(null)\",\"count\":0,\"groupId\":105,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":121,\"label\":\"Unable to Repair - Comment\",\"value\":\"Unable to Repair - Comment#(null)\",\"count\":0,\"groupId\":105,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":100,\"value\":\"undefined#(null)\",\"count\":0,\"groupId\":-1,\"groupRepeatableCount\":-1,\"isGroup\":false},{\"id\":168,\"label\":\"Metering Data\",\"value\":\"Metering Data#(null)\",\"count\":0,\"groupId\":167,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":153,\"label\":\"Metered Power\",\"value\":\"Metered Power#(null)\",\"count\":0,\"groupId\":167,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":154,\"label\":\"Luminaire Wattage\",\"value\":\"Luminaire Wattage#(null)\",\"count\":0,\"groupId\":167,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":157,\"label\":\"Metered Current\",\"value\":\"Metered Current#(null)\",\"count\":0,\"groupId\":167,\"groupRepeatableCount\":1,\"isGroup\":true},{\"id\":158,\"label\":\"Metered Voltage\",\"value\":\"Metered Voltage#(null)\",\"count\":0,\"groupId\":167,\"groupRepeatableCount\":1,\"isGroup\":true}]";


        HeaderColumnNameMappingStrategy<SLVData> strategy
                = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(SLVData.class);

        CsvToBean csvToBean = new CsvToBeanBuilder(new FileReader("/Users/Nithish/Documents/office/data-fix/Oct16/change_management_data.csv"))
                .withType(SLVData.class)
                .withMappingStrategy(strategy)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        Iterator<SLVData> slvDataList = csvToBean.iterator();

        List<Output> outputList = new ArrayList<>();

        int i = 0;
        Writer writer = new BufferedWriter(new FileWriter("/Users/Nithish/Documents/office/data-fix/Oct16/data/change_res_1.csv"));

        CSVWriter csvWriter = new CSVWriter(writer,
                ';');

        String[] headerRecord = {"IdOnController", "FormDef"};
        csvWriter.writeNext(headerRecord);

        List<EdgeFormData> edgeFormDataListTemp = gson.fromJson(formDef, new TypeToken<List<EdgeFormData>>() {
        }.getType());

        int count = 1;
        while (slvDataList.hasNext()){
            SLVData slvData = slvDataList.next();
            i += 1;


            List<EdgeFormData> edgeFormDataList = new ArrayList<>(edgeFormDataListTemp);
            updateFormComponent(160,slvData.getIdOnController(),edgeFormDataList);
            updateFormComponent(161,slvData.getLocation_proposedcontext(),edgeFormDataList);
            updateFormComponent(3,slvData.getIdOnController(),edgeFormDataList);
            updateFormComponent(7,slvData.getAddress(),edgeFormDataList);
            updateFormComponent(8,slvData.getLocation_atlasphysicalpage(),edgeFormDataList);
            updateFormComponent(9,slvData.getLuminaire_proposedfixture(),edgeFormDataList);

            updateFormComponent(16,slvData.getLocation_proposedcontext(),edgeFormDataList);
            updateFormComponent(150,slvData.getComed_projectname(),edgeFormDataList);

            updateFormComponent(12,slvData.getLuminaire_fixturecode(),edgeFormDataList);
            updateFormComponent(13,slvData.getFixing_mastarmangle(),edgeFormDataList);
            updateFormComponent(14,slvData.getFixing_mastarmlength(),edgeFormDataList);
            updateFormComponent(15,slvData.getFixing_numberofmastarms(),edgeFormDataList);
            updateFormComponent(112,slvData.getTalqAddress(),edgeFormDataList);
            updateFormComponent(159,slvData.getLuminaire_type(),edgeFormDataList);
            updateFormComponent(165,slvData.getPole_type(),edgeFormDataList);
            updateFormComponent(166,slvData.getPole_height(),edgeFormDataList);
            updateFormComponent(162,slvData.getPole_material(),edgeFormDataList);

            updateFormComponent(151,slvData.getLuminaire_warranty_start_date(),edgeFormDataList);
            updateFormComponent(152,slvData.getLuminaire_warranty_status(),edgeFormDataList);
            updateFormComponent(173,slvData.getMacAddress(),edgeFormDataList);
            long cslpNode = toMilli(slvData.getCslp_node_install_date());
            updateFormComponent(169,cslpNode > 0 ? cslpNode+"" : "(null)",edgeFormDataList);

            long cslpLum = toMilli(slvData.getCslp_lum_install_date());
            updateFormComponent(170,cslpLum > 0 ? cslpLum+"" : "(null)",edgeFormDataList);

            long installDate = toMilli(slvData.getInstall_date());
            updateFormComponent(171,installDate > 0 ? installDate+"" : "(null)",edgeFormDataList);


            long lumInstallDate = toMilli(slvData.getLuminaire_installdate());
            updateFormComponent(172,lumInstallDate > 0 ? lumInstallDate+"" : "(null)",edgeFormDataList);


            updateFormComponent(19,slvData.getMacAddress(),edgeFormDataList);
            updateFormComponent(20,slvData.getFixtureqrscan(),edgeFormDataList);

            updateFormComponent(17,"New",edgeFormDataList);

            String installStatus = "Select From Below";
            if(slvData.getMacAddress() != null){
                installStatus = "Complete";
            }else if(slvData.getFixtureqrscan() != null){
                installStatus = "Button Photocell Installation";
            }
            updateFormComponent(22,installStatus,edgeFormDataList);

            Output output = new Output();
            output.setIdOnController(slvData.getIdOnController());
            output.setFormDef(gson.toJson(edgeFormDataList));

            String[] res = new String[]{
                    nullCheck(slvData.getIdOnController()),
                    nullCheck(gson.toJson(edgeFormDataList))
            };


            csvWriter.writeNext(res);

            if(i % 10000 == 0){
                System.out.println(i);
                count += 1;
                csvWriter.flush();
                csvWriter.close();
                writer = new BufferedWriter(new FileWriter("/Users/Nithish/Documents/office/data-fix/Oct16/data/change_res_"+count+".csv"));
                csvWriter = new CSVWriter(writer,
                        ';');
                csvWriter.writeNext(headerRecord);


            }
           // outputList.add(output);
        }

        csvWriter.flush();
        csvWriter.close();
       // generateInstallCSVFile(outputList,"/Users/Nithish/Documents/office/data-fix/Oct16/change_res.csv");
        //System.out.println(outputList);
    }


    public static void updateFormComponent(int id,String value,List<EdgeFormData> edgeFormDataList ){
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(id);

        int pos = edgeFormDataList.indexOf(edgeFormData);
        if(pos != -1){
            EdgeFormData tempEdgeFormData =  edgeFormDataList.get(pos);
            tempEdgeFormData.setValue(tempEdgeFormData.getLabel()+"#"+value);
        }

    }



    public static void main_date(String[] r)throws  Exception{
        CSVReader csvReader = new CSVReader(new FileReader("/Users/Nithish/Documents/office/data-fix/Oct16/slv_dates.csv"));
        String[] values = csvReader.readNext();
        List<EdgeData> edgeDataList = new ArrayList<>();
        while ((values = csvReader.readNext()) != null){
            EdgeData edgeData = new EdgeData();
            String idOnController = values[0];
            edgeData.setTitle(idOnController);
            String cslpNode = values[1];
            String cslpLum = values[2];
            String installDate = values[3];
            String lumInstallDate = values[4];
            long cslpNodeL =  toMilli(cslpNode);
            edgeData.setSlvCslpNodeInstallDate(cslpNodeL);
            long cslpLumL =   toMilli(cslpLum);
            edgeData.setSlvCslpLumInstallDate(cslpLumL);
            long installDateL =   toMilli(installDate);
            edgeData.setSlvInstallDate(installDateL);
            long lumInstallDateL =  toMilli(lumInstallDate);
            edgeData.setSlvLumInstallDate(lumInstallDateL);
            edgeDataList.add(edgeData);

        }
        //generateInstallCSVFile(edgeDataList,"/Users/Nithish/Documents/office/data-fix/Oct16/slv_dates_res.csv");
    }

    private static long toMilli(String dateVal){
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


    public static void generateInstallCSVFile(List<Output> csvReportModelList, String filePath) throws IOException {
        StringWriter writer = new StringWriter();
        com.opencsv.CSVWriter csvWriter = new CSVWriter(writer, ',');
        List<String[]> data = toStringInstallReportArray(csvReportModelList);
        csvWriter.writeAll(data);
        csvWriter.close();
        System.out.println(writer);
        writeData(writer.toString(), filePath);
    }




    public static List<String[]> toStringInstallReportArray_1(List<EdgeData> installtionReportJsons) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"Title", "Edge_CSLP_Node_Date","Edge_CSLP_Lum_Date","Edge_Lum_Date","Edge_Install_Date"});
        for (EdgeData edgeData : installtionReportJsons) {
            records.add(new String[]{
                    nullCheck(edgeData.getTitle()),
                    nullCheck(String.valueOf(edgeData.getSlvCslpNodeInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvCslpLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvLumInstallDate())),
                    nullCheck(String.valueOf(edgeData.getSlvInstallDate()))
            });
        }
        return records;
    }


    public static List<String[]> toStringInstallReportArray(List<Output> installtionReportJsons) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"Title", "FormDef"});
        for (Output edgeData : installtionReportJsons) {
            records.add(new String[]{
                    nullCheck(edgeData.getIdOnController()),
                    nullCheck(edgeData.getFormDef())
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
