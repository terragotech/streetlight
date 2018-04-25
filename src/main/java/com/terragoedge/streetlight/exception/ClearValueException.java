package com.terragoedge.streetlight.exception;

public class ClearValueException extends Exception {

    public String message;

    public ClearValueException(String message){
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
