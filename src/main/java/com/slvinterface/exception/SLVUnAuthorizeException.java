package com.slvinterface.exception;

public class SLVUnAuthorizeException extends  Exception {
    String message = null;

    public SLVUnAuthorizeException(String message){
        super(message);
        this.message = message;
    }


    public String getMessage(){
        return  message;
    }
}
