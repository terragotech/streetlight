package com.terragoedge.streetlight.utils;

import com.terragoedge.edgeserver.EdgeFormData;

import java.util.List;

public class Utils {
    public static void updateFormValue(List<EdgeFormData> edgeFormDatas, int id, String value){
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if(pos != -1){
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            edgeFormData.setValue(edgeFormData.getLabel()+"#"+value);
        }
    }
    public static void updateFormValueAndRepeatableCount(List<EdgeFormData> edgeFormDatas, int id, String value,int repeatableCount){
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if(pos != -1){
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            edgeFormData.setValue(edgeFormData.getLabel()+"#"+value);
            edgeFormData.setGroupRepeatableCount(repeatableCount);
        }
    }

    public static String getFormValue(List<EdgeFormData> edgeFormDatas, int id){
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if(pos != -1){
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            return edgeFormData.getValue();
        }
        return "";
    }
    public static String getRepeatableFormValue(List<EdgeFormData> edgeFormDatas,int groupid, int id,int groupRepCount) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        tempEdgeFormData.setGroupRepeatableCount(groupRepCount);
        if(groupid!=-1){
            tempEdgeFormData.setGroupId(groupid);
        }
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            return edgeFormData.getValue();
        }
        return null;
    }

}
