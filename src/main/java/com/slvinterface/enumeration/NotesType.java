package com.slvinterface.enumeration;

public enum NotesType {

    none(1),image(2),audio(3),video(4),group(5),forms(6),Form(6),attachment(7),file(8),location(9),multiupload(10),Image(11),Audio(12),Video(13),Forms(14),File(15) ;

    private int notesType;

    private NotesType(int notesType){
        this.notesType = notesType;
    }

    public int getValue(){
        return notesType;
    }
}
