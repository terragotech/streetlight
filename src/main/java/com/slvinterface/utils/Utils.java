package com.slvinterface.utils;

import com.slvinterface.json.FormValues;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils {
    public static String dateFormat(Long dateTime) {
        String timeZone = PropertiesReader.getProperties().getProperty("serverTimeZone");
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        String dff = dateFormat.format(date);
        return dff;
    }
    public static void updateFormValue(List<FormValues> edgeFormDatas, int id, String value) {
        FormValues tempEdgeFormData = new FormValues();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            FormValues edgeFormData = edgeFormDatas.get(pos);
            if (value != null && !value.trim().isEmpty()) {
                edgeFormData.setValue(edgeFormData.getLabel() + "#" + value);
            } else {
                edgeFormData.setValue(edgeFormData.getLabel() + "#");
            }

        }
    }
}