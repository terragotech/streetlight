package com.slvinterface.exception;

public class ValueCheckException extends Exception {
    String message = null;

    public ValueCheckException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
