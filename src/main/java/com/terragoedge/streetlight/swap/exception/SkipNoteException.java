package com.terragoedge.streetlight.swap.exception;

public class SkipNoteException extends Exception {
    private String message;

    public SkipNoteException(String message) {
        super(message);
        this.message = message;
    }


    public SkipNoteException(Throwable cause) {
        super(cause);
    }
}
