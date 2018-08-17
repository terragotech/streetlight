package com.terragoedge.slvinterface.exception;

public class NotesNotFoundException extends Exception {
    String message;

    public NotesNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
