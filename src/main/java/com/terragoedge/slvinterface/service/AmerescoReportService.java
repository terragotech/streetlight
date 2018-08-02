package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.model.CsvReportModel;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AmerescoReportService extends AbstractService {
    public Gson gson = null;

    public AmerescoReportService() {
        gson = new Gson();
    }

    public void start() {
        List<EdgeNote> edgeNoteList = new ArrayList<>();
        List<CsvReportModel> csvReportModelList = new ArrayList<>();
        for (EdgeNote edgeNote : edgeNoteList) {
            CsvReportModel csvReportModel = new CsvReportModel();
            csvReportModelList.add(csvReportModel);
            processEdgeNote(edgeNote, csvReportModel);
        }
    }

    public void processEdgeNote(EdgeNote edgeNote, CsvReportModel csvReportModel) {

    }

    public void processInspectionForm(String formDef, CsvReportModel csvReportModel) {
        List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDef);
        String issueType = null;
        String comments = null;
        try {
            issueType = getValueByLabel(edgeFormDataList, Constants.ISSUE_TYPE);
            comments = getValueByLabel(edgeFormDataList, Constants.COMMENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        csvReportModel.setIssue1(issueType);
        csvReportModel.setAddComment1(comments);
    }
    public void setCsvModel(EdgeNote edgeNote,CsvReportModel csvReportModel){

    }
}
