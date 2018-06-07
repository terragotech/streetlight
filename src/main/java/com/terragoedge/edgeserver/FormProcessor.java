package com.terragoedge.edgeserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FormProcessor {

    public static void main(String[] rg){
        String formDef = "";
        Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
        }.getType();
        Gson gson = new Gson();
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
        System.out.println(edgeFormDatas);
    }
}
