package com.slvinterface.exception;

public class CreateGeoZoneException extends  Exception {
    public CreateGeoZoneException(Exception e) {
        super(e);
    }

    public CreateGeoZoneException(String e) {
        super(e);
    }
}
