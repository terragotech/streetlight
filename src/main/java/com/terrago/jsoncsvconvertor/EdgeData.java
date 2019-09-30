package com.terrago.jsoncsvconvertor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EdgeData {
    private String title;
    private long slvCslpNodeInstallDate;
    private long slvCslpLumInstallDate;
    private long slvInstallDate;
    private long slvLumInstallDate;

    private long edgeCslpNodeInstallDate;
    private long edgeCslpLumInstallDate;
    private long edgeInstallDate;
    private long edgeLumInstallDate;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSlvCslpNodeInstallDate() {
        return slvCslpNodeInstallDate;
    }

    public void setSlvCslpNodeInstallDate(long slvCslpNodeInstallDate) {
        this.slvCslpNodeInstallDate = slvCslpNodeInstallDate;
    }

    public long getSlvCslpLumInstallDate() {
        return slvCslpLumInstallDate;
    }

    public void setSlvCslpLumInstallDate(long slvCslpLumInstallDate) {
        this.slvCslpLumInstallDate = slvCslpLumInstallDate;
    }

    public long getSlvInstallDate() {
        return slvInstallDate;
    }

    public void setSlvInstallDate(long slvInstallDate) {
        this.slvInstallDate = slvInstallDate;
    }

    public long getSlvLumInstallDate() {
        return slvLumInstallDate;
    }

    public void setSlvLumInstallDate(long slvLumInstallDate) {
        this.slvLumInstallDate = slvLumInstallDate;
    }

    public long getEdgeCslpNodeInstallDate() {
        return edgeCslpNodeInstallDate;
    }

    public void setEdgeCslpNodeInstallDate(long edgeCslpNodeInstallDate) {
        this.edgeCslpNodeInstallDate = edgeCslpNodeInstallDate;
    }

    public long getEdgeCslpLumInstallDate() {
        return edgeCslpLumInstallDate;
    }

    public void setEdgeCslpLumInstallDate(long edgeCslpLumInstallDate) {
        this.edgeCslpLumInstallDate = edgeCslpLumInstallDate;
    }

    public long getEdgeInstallDate() {
        return edgeInstallDate;
    }

    public void setEdgeInstallDate(long edgeInstallDate) {
        this.edgeInstallDate = edgeInstallDate;
    }

    public long getEdgeLumInstallDate() {
        return edgeLumInstallDate;
    }

    public void setEdgeLumInstallDate(long edgeLumInstallDate) {
        this.edgeLumInstallDate = edgeLumInstallDate;
    }

    @Override
    public String toString() {
        return "EdgeData{" +
                "title='" + title + '\'' +
                ", slvCslpNodeInstallDate=" + slvCslpNodeInstallDate +
                ", slvCslpLumInstallDate=" + slvCslpLumInstallDate +
                ", slvInstallDate=" + slvInstallDate +
                ", slvLumInstallDate=" + slvLumInstallDate +
                ", edgeCslpNodeInstallDate=" + edgeCslpNodeInstallDate +
                ", edgeCslpLumInstallDate=" + edgeCslpLumInstallDate +
                ", edgeInstallDate=" + edgeInstallDate +
                ", edgeLumInstallDate=" + edgeLumInstallDate +
                '}';
    }



    public boolean isEqual(){

        boolean res = true;
        if(slvCslpLumInstallDate != edgeCslpLumInstallDate){
            res = false;
        }else{
            slvCslpLumInstallDate = 0;
            edgeCslpLumInstallDate = 0;
        }

        if(slvCslpNodeInstallDate != edgeCslpNodeInstallDate){
            res = false;
        }else{
            slvCslpNodeInstallDate = 0;
            edgeCslpNodeInstallDate = 0;
        }

        if(slvInstallDate != edgeInstallDate){
            res = false;
        }else{
            slvInstallDate = 0;
            edgeInstallDate = 0;
        }

        if(slvLumInstallDate != edgeLumInstallDate){
            res = false;
        }else{
            slvLumInstallDate = 0;
            edgeLumInstallDate = 0;
        }

        return res;
    }


    public boolean compareStartOfDay(){
        boolean res = true;
        if(!isSameDay(slvCslpNodeInstallDate,edgeCslpNodeInstallDate)){
            res = false;
        }

        if(!isSameDay(slvCslpLumInstallDate,edgeCslpLumInstallDate)){
            res = false;
        }

        if(!isSameDay(slvLumInstallDate,edgeLumInstallDate)){
            res = false;
        }

        if(!isSameDay(slvInstallDate,edgeInstallDate)){
            res = false;
        }

        return  res;

    }


    public boolean isSameDay(long slvDate,long edgeDate){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        System.out.println("D1"+fmt.format(new Date(edgeDate)));
        System.out.println("D2"+fmt.format(new Date(slvDate)));
        return fmt.format(new Date(edgeDate)).equals(fmt.format(new Date(slvDate)));
    }



}

