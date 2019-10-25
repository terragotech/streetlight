package com.slvinterface.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.query.In;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.slvinterface.json.ConditionsJson;
import com.slvinterface.json.InBoundConfig;
import com.slvinterface.model.HistoryModel;
import com.slvinterface.service.*;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Test {
public static void main(String []args) throws Exception {
    InBoundInterface inBoundInterface = new PrismInboundInterface();
    //inBoundInterface.startProcessing();
    //inBoundInterface.startProcessing();
    inBoundInterface.updateNotes(
            "D:\\wc\\terrago\\uc_slv_interface\\streetlight\\src\\main\\resources\\prism\\inbound_data\\2019_10_25\\updates_05_39_51.csv");

}

}
