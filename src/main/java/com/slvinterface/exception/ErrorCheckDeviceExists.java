package com.slvinterface.exception;

public class ErrorCheckDeviceExists extends Exception{
    String message = null;

    public ErrorCheckDeviceExists(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
