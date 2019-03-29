package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.model.SlvData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AbstractReportService {
    public long getInstallDateAsLong(SlvData slvData) {
        long inspectionDateMilli = 0L;
        try {
            SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
            Date parseDate = format.parse(slvData.getInstallDate());
            inspectionDateMilli = parseDate.getTime();
        } catch (Exception e) {
        }
        return inspectionDateMilli;
    }
}
