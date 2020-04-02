package com.terragoedge.slvinterface.exception;

public class NoDataChangeException  extends  Exception {

    private String message;

    public NoDataChangeException(String message) {
        super(message);
        this.message = message;
    }
}
