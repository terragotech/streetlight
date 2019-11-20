package com.slvinterface.exception;

public class DeviceCreationFailedException extends  Exception {
    String message = null;

    public DeviceCreationFailedException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }

}
