package com.slvinterface.exception;

public class DeviceUpdationFailedException extends Exception {
    String message = null;

    public DeviceUpdationFailedException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }

}
