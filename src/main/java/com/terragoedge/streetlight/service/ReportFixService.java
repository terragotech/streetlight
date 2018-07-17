package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.json.model.FailureFormDBmodel;

public class ReportFixService {
    private Gson gson = null;
    private StreetlightDao streetlightDao = null;

    public ReportFixService() {
        gson = new Gson();
        streetlightDao = new StreetlightDao();
    }

    public void run() {
        streetlightDao.getFailureModelList();
    }
}
