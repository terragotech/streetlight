package com.terragoedge.automation.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.exception.NoValueException;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractInventoryService {
    InventoryDAO inventoryDAO;
    Gson gson = null;

    public AbstractInventoryService() {
        inventoryDAO = InventoryDAO.INSTANCE;
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
    public List<String> getCsvToEntity(String path) {
        try {
            List<String> macList=new ArrayList<>();
            CSVReader reader = new CSVReader(new FileReader(path), ',');
            List<String[]> records = reader.readAll();
            Iterator<String[]> iterator = records.iterator();
            while (iterator.hasNext()) {
                String[] record = iterator.next();
                macList.add(record[0]);
            }
            return macList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

}
