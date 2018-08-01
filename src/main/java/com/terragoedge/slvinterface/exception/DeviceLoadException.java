package com.terragoedge.slvinterface.exception;

public class DeviceLoadException extends  Exception {
    private String message;

    public DeviceLoadException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
