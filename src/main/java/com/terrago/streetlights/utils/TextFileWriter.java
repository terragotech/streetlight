package com.terrago.streetlights.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileWriter {
    File file = null;
    FileWriter fileWriter = null;
    BufferedWriter writer = null;
    public void openFile(String strFileName){
        try {
            file = new File(strFileName);
            fileWriter = new FileWriter(file);
            // create file if not exists
            if (!file.exists()) {
                file.createNewFile();
            }
            // initialize BufferedWriter
            writer = new BufferedWriter(fileWriter);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void writeData(String fileContent){
        try {
            writer.write(fileContent);
            writer.write(("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void closeFile(){

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // close FileWriter
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    }
}
