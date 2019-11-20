package com.slvinterface.exception;

public class SLVConnectionException extends  Exception {

    public SLVConnectionException(String message,Throwable t){
        super(message,t);
    }

    public SLVConnectionException(Throwable t){
        super(t);
    }

}
