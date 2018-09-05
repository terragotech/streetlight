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
                connectionDAO.deleteComedInstallSyncEntity(noteguid);
            }
        }
        System.out.println("************** End **************");
    }

    private List<String> getToMeMacAddress(List<EdgeFormData> edgeFormDataList){
        JsonArray jsonArray = new JsonArray();
        List<String> macaddresses = new ArrayList<>();
        for(EdgeFormData edgeFormData : edgeFormDataList){
            if(edgeFormData.getLabel().equals("To Installer") && edgeFormData.getValue().equals("To Me")){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("groupId", edgeFormData.getGroupId());
                jsonObject.addProperty("repeatableCount", edgeFormData.getGroupRepeatableCount());
                jsonArray.add(jsonObject);
            }
        }
        for(JsonElement jsonElement : jsonArray){
            JsonObject jsonObject = (JsonObject) jsonElement;
            String macAddress= getMacAddress(edgeFormDataList,jsonObject.get("groupId").getAsInt(),jsonObject.get("repeatableCount").getAsInt());
            if(macAddress != null){
                macaddresses.add(macAddress);
            }
        }
        return macaddresses;
    }

    private String getMacAddress(List<EdgeFormData> edgeFormDatas, int groupId, int repeatableCount){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getLabel().equals("MAC address") && edgeFormData.getGroupId() == groupId && edgeFormData.getGroupRepeatableCount() == repeatableCount){
                return edgeFormData.getValue();
            }
        }
        return null;
    }
}
