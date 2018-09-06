package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.model.EdgeFormData;

import java.util.List;

public class AbstractService {
    public Gson gson = null;

    public AbstractService() {
        gson = new Gson();
    }

    public List<EdgeFormData> getEdgeFormData(String data) {
        List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
        }.getType());
        return edgeFormDatas;
    }

    public static String getValueByLabel(List<EdgeFormData> edgeFormDatas, String label) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setLabel(label);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            return edgeFormData.getValue();
        }
        return "";
    }

    public static String getValueById(List<EdgeFormData> edgeFormDatas, int id) {
        for(EdgeFormData edgeFormData : edgeFormDatas){
                if(edgeFormData.getId() == id){
                    return edgeFormData.getValue();
                }
        }
        return "";
    }

    public static String getValueByTitle(List<EdgeFormData> edgeFormDatas, String label) {
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getLabel().equals(label)){
                return edgeFormData.getValue();
            }
        }
        return "";
    }
}
