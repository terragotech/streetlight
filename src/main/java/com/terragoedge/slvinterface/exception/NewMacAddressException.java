package com.terragoedge.slvinterface.exception;

public class NewMacAddressException extends Exception {
    String message=null;
    public NewMacAddressException(String message){
        this.message=message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
