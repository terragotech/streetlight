package com.slvinterface.exception;

public class ValueClearException extends  Exception{
    String message = null;

    public ValueClearException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
