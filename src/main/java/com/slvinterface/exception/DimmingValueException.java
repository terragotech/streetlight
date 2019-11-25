package com.slvinterface.exception;

public class DimmingValueException extends Exception{
    String message = null;

    public DimmingValueException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
