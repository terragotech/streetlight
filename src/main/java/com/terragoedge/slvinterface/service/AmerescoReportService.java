package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.model.CsvReportModel;
import com.terragoedge.slvinterface.model.EdgeNote;

import java.util.ArrayList;
import java.util.List;

public class AmerescoReportService {

    public AmerescoReportService() {

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
}
