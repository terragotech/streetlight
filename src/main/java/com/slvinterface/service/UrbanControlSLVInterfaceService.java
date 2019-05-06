package com.slvinterface.service;

import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.json.*;

import java.util.List;

public class UrbanControlSLVInterfaceService extends  SLVInterfaceService{

    public UrbanControlSLVInterfaceService()throws Exception{
        super();
    }


    public void processFormData(List<FormData> formDataList){
        for(FormData formData : formDataList){
            List<FormValues> formValuesList = formData.getFormDef();
            List<Priority> priorities = conditionsJson.getPriority();
            List<Config> configList = conditionsJson.getConfigList();
            for(Priority priority : priorities){
                Config temp = new Config();
                temp.setType(priority.getType());

                int pos = configList.indexOf(priorities);

                if(pos != -1){
                    Config config =  configList.get(pos);
                    List<Id> idList = config.getIds();
                }
            }
        }
    }
}
