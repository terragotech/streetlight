package com.slvinterface.exception;

public class GeoZoneCreationFailedException extends Exception {
    String message = null;

    public GeoZoneCreationFailedException(String message) {
        super(message);
        this.message = message;
    }


    public String getMessage() {
        return message;
    }
}