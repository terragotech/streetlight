package com.terragoedge.streetlight.exception;

public class AlreadyUsedException extends  Exception{
    private String message;

    public AlreadyUsedException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
