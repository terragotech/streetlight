package com.slvinterface.exception;

public class DatabaseException extends  Exception {

    public DatabaseException(String message,Throwable t){
        super(message,t);
    }

    public DatabaseException(Throwable t){
        super(t);
    }
}
