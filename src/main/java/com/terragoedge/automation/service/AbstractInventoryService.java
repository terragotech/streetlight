package com.terragoedge.automation.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.terragoedge.automation.Dao.InstallDAO;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.model.CustodyResultModel;
import com.terragoedge.automation.model.UserEntity;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.model.EdgeFormData;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class AbstractInventoryService {
    InventoryDAO inventoryDAO;
    InstallDAO installDAO = null;
    Gson gson = null;
    public static final String PARSE_REPEATEABLE_DELIMITER = "\\*\\|\\#\\*";

    public AbstractInventoryService() {
        inventoryDAO = InventoryDAO.INSTANCE;
        installDAO = new InstallDAO();
        gson = new Gson();

    }

    public String getPalletNumber(String formTemplateGuid, EdgeNoteView edgeNoteView) {
        String palletNumber = null;
        try {
            EdgeFormEntity edgeFormEntity = inventoryDAO.getFormDef(formTemplateGuid, edgeNoteView.getNoteGuid());
            if (edgeFormEntity != null) {
                String formDef = edgeFormEntity.getFormDef();
                List<EdgeFormData> edgeFormDataList = getFormDef(formDef);
                palletNumber = valueById(edgeFormDataList, 45);
            }
            return palletNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return palletNumber;
    }

    public List<EdgeFormData> getFormDef(String formDef) {
        Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
        }.getType();
        Gson gson = new Gson();
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
        return edgeFormDatas;
    }

    private String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setId(id);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value.contains("#")) {
                String[] values = value.split("#");
                if (values.length == 2) {
                    return values[1];
                }
            }

            if (value == null || value.trim().isEmpty()) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }
    public List<CustodyResultModel> getCsvToEntity(String path) {
        try {
            List<CustodyResultModel> custodyResultModels=new ArrayList<>();
            CSVReader reader = new CSVReader(new FileReader(path), ',');
            List<String[]> records = reader.readAll();
            Iterator<String[]> iterator = records.iterator();
            while (iterator.hasNext()) {
                CustodyResultModel custodyResultModel = new CustodyResultModel();
                custodyResultModels.add(custodyResultModel);
                String[] record = iterator.next();
                custodyResultModel.setMacaddress(record[0]);
                custodyResultModel.setCurrentinventorylocation(record[1]);
                custodyResultModel.setUserselectedcurrentlocation(record[2]);
                custodyResultModel.setDestinationlocation(record[3]);
                custodyResultModel.setNotedatetime(record[4]);
                custodyResultModel.setUsername(record[5]);
                custodyResultModel.setWorkflow(record[6]);
            }
            return custodyResultModels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public static String[] getRepeatableFormValue(List<EdgeFormData> edgeFormDatas, int id, int groupId, int groupRepCount) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        tempEdgeFormData.setGroupId(groupId);
        tempEdgeFormData.setGroupRepeatableCount(groupRepCount);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value != null) {
                String[] values = value.split(PARSE_REPEATEABLE_DELIMITER);
                return values;
            }
        }
        return null;
    }

    public String getFormValue(String value) {
        if (value != null) {
            String[] values = value.split("#");
            if (values.length == 2) {
                return values[1];
            }else if(!value.contains("#")){
                return value;
            }
        }
        return null;
    }
    public Map<String,String> getAllUsers() {
        List<UserEntity> userList = installDAO.getUsersList();
        Map<String,String> usersMap = new HashMap<>();
        for(UserEntity user : userList){
            usersMap.put(user.getUserName(),"User-"+(user.getFirstName() == null ? "" : user.getFirstName()) +" "+(user.getLastName() == null ? "" : user.getLastName()));
        }
        return usersMap;
    }


}
