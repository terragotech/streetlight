package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.model.EdgeFormData;

import java.util.ArrayList;
import java.util.List;

public class LoadForAssignmentService {
    public static final String PARSE_REPEATEABLE_DELIMITER = "\\*\\|\\#\\*";
    private Gson gson;
    private ConnectionDAO connectionDAO;
    private InventoryDAO inventoryDAO;
    public LoadForAssignmentService() {
        connectionDAO = ConnectionDAO.INSTANCE;
        inventoryDAO = InventoryDAO.INSTANCE;
        gson = new Gson();
    }

    public void start(){
        System.out.println("************** Start **************");
        List<EdgeFormEntity> edgeFormEntities = connectionDAO.getLoadForAssignmentForms();
        List<String> macAddresses = new ArrayList<>();
        System.out.println("Load for Assignment forms"+edgeFormEntities);
        for(EdgeFormEntity edgeFormEntity : edgeFormEntities){
            String formdef = edgeFormEntity.getFormDef();
            List<EdgeFormData> edgeFormDataList = gson.fromJson(formdef,new TypeToken<List<EdgeFormData>>(){}.getType());
            macAddresses.addAll(getToMeMacAddress(edgeFormDataList));
        }
        if(macAddresses.size() > 0) {
            JsonArray jsonArray = inventoryDAO.getNotebookFromNoteTitle(macAddresses);
            System.out.println("result = "+jsonArray.toString());
            System.out.println("result size = "+jsonArray.size());
            for(JsonElement jsonElement : jsonArray){
                JsonObject jsonObject = (JsonObject) jsonElement;
                String noteguid = jsonObject.get("id").getAsString();
                System.out.println("delete comedinstallsync item: "+noteguid);
                //connectionDAO.deleteComedInstallSyncEntity(noteguid);
            }
        }
        System.out.println("************** End **************");
    }

    private List<String> getToMeMacAddress(List<EdgeFormData> edgeFormDataList){
        JsonArray jsonArray = new JsonArray();
        List<String> macaddresses = new ArrayList<>();
        for(EdgeFormData edgeFormData : edgeFormDataList){
            if(edgeFormData.getValue().contains("To Me")){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("groupId", edgeFormData.getGroupId());
                jsonObject.addProperty("repeatableCount", edgeFormData.getGroupRepeatableCount());
                jsonArray.add(jsonObject);
            }
        }
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = (JsonObject) jsonElement;
            getMacAddress(edgeFormDataList,jsonObject.get("groupId").getAsInt(),jsonObject.get("repeatableCount").getAsInt(),macaddresses);

        }
        return macaddresses;
    }

    private String getMacAddress(List<EdgeFormData> edgeFormDatas, int groupId, int repeatableCount,List<String> macaddresses){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getLabel().startsWith("MAC") && edgeFormData.getGroupId() == groupId && edgeFormData.getGroupRepeatableCount() == repeatableCount){
                String[] macAddressWithLabelList =  getRepeatableFormValue(edgeFormData.getValue());
                for (String macAddressWithLabel : macAddressWithLabelList) {
                    String macAddressValue = getFormValue(macAddressWithLabel);
                    if(macAddressValue != null){
                        macaddresses.add(macAddressValue);
                    }
                }
            }
        }
        return null;
    }


    public String getFormValue(String value) {
        if (value != null) {
            String[] values = value.split("#");
            if (values.length == 2) {
                return values[1];
            }
        }
        return null;

    }


    public String[] getRepeatableFormValue(String value) {
        if (value != null) {
            String[] values = value.split(PARSE_REPEATEABLE_DELIMITER);
            return values;
        }
        return null;

    }
}
