package com.terragoedge.xml.parser;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

import com.terragoedge.xml.devices.SLVDeviceArray;

public class XMLMarshaller {
	
	
	public Object xmlToObject(String xml){
		try{
			if(xml != null){
				InputSource inputSource = new InputSource();
				inputSource.setCharacterStream(new StringReader(xml));
				JAXBContext context = JAXBContext.newInstance(SLVDeviceArray.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				Object ob =  unmarshaller.unmarshal(inputSource);
				return ob;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public  String objectToXml(Object obj) {
		try{
			if(obj != null){
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
				JAXBContext context = JAXBContext.newInstance(SLVDeviceArray.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(obj,arrayOutputStream);
				return new String(arrayOutputStream.toByteArray(),"UTF-8");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
