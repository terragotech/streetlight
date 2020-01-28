package com.terrago.streetlights.utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
public class Test {
    public static void main(String[] args) {
        /*CreateRevision createRevision = new CreateRevision();
        createRevision.createRevision("D:\\tmp\\phase3.csv");
        System.out.println("Updated Good...");*/
        UpdateOutageData updateOutageData = new UpdateOutageData();
        updateOutageData.createRevision2();

    }
    public static void main1(String[] args) {
        BufferedWriter bufferedWriter = null;
        try {
            JsonParser jsonParser = new JsonParser();
            JsonArray diagonsticArray = readDiagnosticJson(jsonParser);
            File file = new File("D:\\works\\2019Org\\terrago\\Light_Interface\\fbl\\fbl\\nodes_list_response.txt");
            File outFile = new File("D:\\works\\2019Org\\terrago\\Light_Interface\\fbl\\fbl\\fl_note_import_up2.csv");
            bufferedWriter = new BufferedWriter(new FileWriter(outFile));
            bufferedWriter.append("1,3,4,5,6,8,10,52,12,46,50,51,47,19,20,21,22,23,25,26,27,28,29,30,49,48,title,layerguid,formtemplateguid,notebookguid,description,location,lat,lng\n");
            //bufferedWriter.append("1,3,4,5,6,7,8,9,10,11,12,14,15,16,19,20,21,22,23,25,26,27,28,29,title,layerguid,formtemplateguid,notebookguid,description,location,lat,lng\n");
            FileReader fileReader = new FileReader(file);
            JsonObject jsonObject = jsonParser.parse(fileReader).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
            int count = 0;
            for(JsonElement jsonElement : jsonArray) {
                JsonObject dataObject = jsonElement.getAsJsonObject();
                JsonElement groupElement = dataObject.get("groupId");
                if (isJsonValid(groupElement)) {
                    String groupId = groupElement.getAsString();
                    if (groupId.equals("165") || groupId.equals("182")) {
                        int id = dataObject.get("id").getAsInt();
                        String node = dataObject.get("node").getAsString();
                        String latitude = dataObject.get("latitude").getAsString();
                        String longitude = dataObject.get("longitude").getAsString();

                        String maintenanceCompany = isJsonValid(dataObject.get("maintenanceCompany")) ? dataObject.get("maintenanceCompany").getAsString() : "";

                        String pole_id = isJsonValid(dataObject.get("poleId")) ? dataObject.get("poleId").getAsString() : "";
                        String fixtureId = isJsonValid(dataObject.get("fixtureId")) ? dataObject.get("fixtureId").getAsString() : "";
                        //String poleColor = isJsonValid(dataObject.get("poleColor")) ? dataObject.get("poleColor").getAsString() : "";
                        //String poleHeight = isJsonValid(dataObject.get("poleHeight")) ? dataObject.get("poleHeight").getAsString() : "";
                        String devEui = dataObject.get("dev_eui").getAsString();
                        String poleType = isJsonValid(dataObject.get("poleType")) ? dataObject.get("poleType").getAsString() : "";
                        String fixtureType = isJsonValid(dataObject.get("fixtureType")) ? dataObject.get("fixtureType").getAsString() : "";
                        if(poleType.equals(""))
                        {
                            poleType = "Not selected";
                        }
                        if(fixtureType.equals(""))
                        {
                            fixtureType = "Not selected";
                        }
                        //String fixtureId = isJsonValid(dataObject.get("fixtureId")) ? dataObject.get("fixtureId").getAsString() : "";

                        String poleCurrent = isJsonValid(dataObject.get("CState")) ? dataObject.get("CState").getAsString() : "";
                        String fixtureCurrent = isJsonValid(dataObject.get("C1State")) ? dataObject.get("C1State").getAsString() : "";
                        String poleVoltage = isJsonValid(dataObject.get("VState")) ? dataObject.get("VState").getAsString() : "";
                        String fixtureVoltage = isJsonValid(dataObject.get("V1State")) ? dataObject.get("V1State").getAsString() : "";
                        String dimValue = isJsonValid(dataObject.get("LD1State")) ? dataObject.get("LD1State").getAsString() : "";

                        String alertType = getDiagonsticValue(diagonsticArray,"alertType",id);
                        float minimum = getDiagonsticValueinFloat(diagonsticArray,"minimum",id);
                        float maximum = getDiagonsticValueinFloat(diagonsticArray,"maximum",id);
                        String value = getDiagonsticValue(diagonsticArray,"value",id);
                        String layerGuid = "37f710ba-064d-11e8-ba89-0ed5f89f718b";// not yet complete
                        if(devEui != null && !devEui.trim().equals("")){
                            layerGuid = "ba598462-064c-11e8-ba89-0ed5f89f718b";// complete layer
                        }
                        String notebookguid = "c9ead907-c9b4-4f2f-ba23-c89d04de6172";//165
                        if(groupId.equals("182")){
                            notebookguid = "32cfacd9-e77d-4a97-8ba7-445946db3002"; //182
                        }
                        //bufferedWriter.append("Hide"+","+String.valueOf(id)+","+node+","+latitude+","+longitude+","+fixtureId+","+maintenanceCompany+","+fixtureType+","+pole_id+","+poleType+","+"Install"+","+poleColor+","+poleHeight+","+devEui+","+alertType+","+minimum+","+maximum+","+value+","+"Hide"+","+poleCurrent+","+fixtureCurrent+","+poleVoltage+","+fixtureVoltage+","+dimValue+","+node+","+layerGuid+","+"3a548381-5133-4867-b087-59604ed47362"+","+notebookguid+","+""+","+""+","+latitude+","+longitude+"\n");
                        bufferedWriter.append("Hide"+","+String.valueOf(id)+","+node+","+latitude+","+longitude+","+maintenanceCompany+","+pole_id+","+ fixtureId+ ","+ "Install"+","+devEui+","+poleType+","+fixtureType+","+ "Hide" +"," + alertType+","+minimum+","+maximum+","+value+","+"Hide"+","+poleCurrent+","+fixtureCurrent+","+poleVoltage+","+fixtureVoltage+","+dimValue+","+"Hide"+","+"Complete"+"," + "No" + "," +node+","+layerGuid+","+"3a548381-5133-4867-b087-59604ed47362"+","+notebookguid+","+""+","+""+","+latitude+","+longitude+"\n");
                        count++;
                    }
                }
            }
            System.out.println("count = "+count);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isJsonValid(JsonElement jsonElement){
        if(!jsonElement.isJsonNull() && jsonElement != null){
            return true;
        }
        return false;
    }

    private static JsonArray readDiagnosticJson(JsonParser jsonParser){
        try{
            File file = new File("D:\\works\\2019Org\\terrago\\Light_Interface\\fbl\\fbl\\diag_response.txt");
            FileReader fileReader = new FileReader(file);
            JsonObject jsonObject = jsonParser.parse(fileReader).getAsJsonObject();
            return jsonObject.get("data").getAsJsonArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JsonArray();
    }

    private static String getDiagonsticValue(JsonArray jsonArray,String key,int id){
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.get("id").getAsInt() == id) {
                if (jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null) {
                    return jsonObject.get(key).getAsString();
                }
            }
        }
        return "";
    }

    private static float getDiagonsticValueinFloat(JsonArray jsonArray,String key,int id){
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.get("id").getAsInt() == id) {
                if (jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null) {
                    return jsonObject.get(key).getAsFloat();
                }
            }
        }
        return 0.0f;
    }
}
