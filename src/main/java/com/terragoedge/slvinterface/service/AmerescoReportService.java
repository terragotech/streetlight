package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.entity.EdgeNoteEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
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
        List<EdgeNoteView> edgeNoteViewList = new ArrayList<>();
        List<CsvReportModel> csvReportModelList = new ArrayList<>();
        for (EdgeNoteView edgeNoteView : edgeNoteViewList) {
            CsvReportModel csvReportModel = new CsvReportModel(edgeNoteView);
            csvReportModelList.add(csvReportModel);
            processEdgeNote(edgeNoteView, csvReportModel);
        }
    }

    public void processEdgeNote(EdgeNoteView edgeNoteView, CsvReportModel csvReportModel) {

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
}
