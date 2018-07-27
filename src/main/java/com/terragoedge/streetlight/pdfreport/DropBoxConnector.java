package com.terragoedge.streetlight.pdfreport;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import com.dropbox.core.v2.users.FullAccount;


public class DropBoxConnector {
	private String mdropBoxAccessToken;
	
	private DbxClientV2 client;
	
	
	public String getdropBoxAccessToken() {
		return mdropBoxAccessToken;
	}
	public void setdropBoxAccessToken(String mdropBoxAccessToken) {
		this.mdropBoxAccessToken = mdropBoxAccessToken;
	}
	
	
	public void establishConnection() throws DbxException
	{
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
	     client = new DbxClientV2(config, mdropBoxAccessToken);
	}
	public void uploadFile(String dropBoxLocation, String srcFileLocation, String srcFileName) throws UploadErrorException, DbxException, IOException
	{
		InputStream in = new FileInputStream(srcFileLocation+File.separator+srcFileName); 
		FileMetadata metadata = client.files().uploadBuilder(dropBoxLocation+"/"+srcFileName).uploadAndFinish(in);
		
	}
	public String getSharedLinks(String dropBoxLocation,String srcFileName) throws CreateSharedLinkWithSettingsErrorException, DbxException
	{
		String sharedLink = null;
		SharedLinkMetadata slm = client.sharing().createSharedLinkWithSettings(dropBoxLocation+"/"+srcFileName, SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build());
		String url = slm.getUrl();
		sharedLink = url;
		return sharedLink;
	}
	/*public static void main(String[] args) throws DbxException, IOException  {
		String dropBoxAccessToken = PropertiesReader.getProperties().getProperty("dailyreport.dropboxAccessToken");
		String dropBoxLocation = PropertiesReader.getProperties().getProperty("dailyreport.dropboxLocation");
		DropBoxConnector dropBoxConnector = new DropBoxConnector();
		dropBoxConnector.setdropBoxAccessToken(dropBoxAccessToken);
		dropBoxConnector.establishConnection();
		dropBoxConnector.uploadFile(dropBoxLocation,"D:","1.txt");
		String dropBoxLink = dropBoxConnector.getSharedLinks(dropBoxLocation, "1.txt");
		System.out.println(dropBoxLink);
	}*/

}