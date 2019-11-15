package com.terragoedge.streetlight.exception;

public class SerialNumberSyncedException extends  Exception{

    String message;

    public SerialNumberSyncedException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
