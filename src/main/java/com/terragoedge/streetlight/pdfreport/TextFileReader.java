package com.terragoedge.streetlight.pdfreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TextFileReader {
	File file = null;
	FileReader fileReader = null;
	BufferedReader bufferedReader = null;
	
	public void openFile(String fileName) throws FileNotFoundException
	{
		file = new File(fileName);
		fileReader = new FileReader(file);
		bufferedReader = new BufferedReader(fileReader);
		
	}
	public String readLine()
	{
		String line = null;
		try {
			line = bufferedReader.readLine();
		} catch (IOException e) {
			line = null;
		}
		return line;
	}
	public void closeFile()
	{
		if(bufferedReader != null)
		{
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
