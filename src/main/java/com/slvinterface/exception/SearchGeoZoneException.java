package com.slvinterface.exception;

public class SearchGeoZoneException extends Exception {
    String message = null;

    public SearchGeoZoneException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
