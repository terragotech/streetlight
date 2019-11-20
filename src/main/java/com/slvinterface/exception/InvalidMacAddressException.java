package com.slvinterface.exception;

public class InvalidMacAddressException extends Exception{
    String message = null;

    public InvalidMacAddressException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
