package com.terragoedge.slvinterface.utils;

import com.terragoedge.slvinterface.model.EdgeFormData;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils {
    public static String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dff = dateFormat.format(date);
        return dff;
    }
    public static void updateFormValue(List<EdgeFormData> edgeFormDatas, int id, String value) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            if (value != null && !value.trim().isEmpty()) {
                edgeFormData.setValue(edgeFormData.getLabel() + "#" + value);
            } else {
                edgeFormData.setValue(edgeFormData.getLabel() + "#");
            }

        }
    }

    public static String installDateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//2019-05-24
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
        String dff = dateFormat.format(date);
        return dff;
    }

    public static RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
}
