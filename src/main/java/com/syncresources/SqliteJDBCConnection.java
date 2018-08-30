package com.syncresources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum SqliteJDBCConnection {

    INSTANCE;

    Logger logger = Logger.getLogger(SqliteJDBCConnection.class);

    private String serverResourcePath;
    private String mobileResourcePath;
    private List<String> noResources = new ArrayList<>();
    private List<String> totalMobileResources = new ArrayList<>();
    private List<String> copiedResources = new ArrayList<>();
    private List<String> presentResources = new ArrayList<>();
    ConnectionSource connectionSource = null;
    Dao<EdgeFormMobile, String> edgeformDao;
    Properties properties;
    private Gson gson = new Gson();
    SqliteJDBCConnection() {

        try {
            properties = PropertiesReader.getProperties();
            String DATABASE_URL = properties.getProperty("sqlite.db.path");
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            edgeformDao = DaoManager.createDao(connectionSource, EdgeFormMobile.class);
            serverResourcePath = properties.getProperty("server.resource.path");
            mobileResourcePath=properties.getProperty("mobile.resource.path");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Catch block1: "+e.getMessage());
        }

    }
    public List<String> getAllFormResources(){
        List<String> resources = new ArrayList<>();
        try {
            List<EdgeFormMobile> edgeFormEntities = edgeformDao.queryBuilder().query();
            for(EdgeFormMobile edgeFormMobile : edgeFormEntities){
                List<FormDef> formDefs = gson.fromJson(edgeFormMobile.getFormDef(),new TypeToken<List<FormDef>>(){}.getType());
                for(FormDef formDef : formDefs){
                    if(formDef.getLabel().equals("Photo Upload")){
                        String value = formDef.getValue();
                        if(value != null && !value.equals("Photo Upload#(null)")) {
                            if(value.contains(",")){
                                String[] images = value.split(",");
                                for(String image : images){
                                    resources.add(image);
                                }
                            }else{
                                resources.add(value);
                            }
                        }
                    }
                }
            }
            System.out.println(resources.size());
        }catch (Exception e){
            logger.error("catch block2: "+e.getMessage());
            e.printStackTrace();
        }
        return resources;
    }

    public void processResources(List<String> resources){
        List<String> serverResourceNames = new ArrayList<>();
        List<String> missingResources = new ArrayList<>();
        File serverResourceFolder = new File(serverResourcePath);
        if(serverResourceFolder.exists()){
            File[] files = serverResourceFolder.listFiles();
            if(files != null){
                for (File file : files){
                    serverResourceNames.add(file.getName());
                }
            }
        }

        for(String resource : resources){
            if(!serverResourceNames.contains(resource)){
                missingResources.add(resource);
            }
        }
        File mobileFolder = new File(mobileResourcePath);
        copyResources(missingResources,mobileFolder);
        logger.info("Missing resources: "+missingResources);
        System.out.println(missingResources.size());
    }

private void copyResources(List<String> missingResources,File folder){
        File[] files = folder.listFiles();
        for(File file : files){
            if(file.isDirectory()){
                copyResources(missingResources,file);
            }else{
                if(missingResources.contains(file.getName())){
                    try {
                        FileCopyUtils.copy(file, new File(serverResourcePath+"/"+file.getName()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    logger.info("file copied: "+file.getName());
                }else {
                    noResources.add(file.getName());
                    logger.info("Resource not found: "+file.getName());
                    System.out.println("Resource not found"+ file.getName());
                }
            }
        }
}

public void copyAllResources(){
        List<String> serverResourceNames = new ArrayList<>();
    File serverResourceFolder = new File(serverResourcePath);
    if(serverResourceFolder.exists()){
        File[] files = serverResourceFolder.listFiles();
        if(files != null){
            for (File file : files){
                if(!file.isDirectory()) {
                    serverResourceNames.add(file.getName());
                }
            }
        }
    }

    File mobileResourceFolder = new File(mobileResourcePath);
    processMobileResources(serverResourceNames,mobileResourceFolder);

    System.out.println("Total mobile Resources : "+ totalMobileResources);
    System.out.println("Total mobile Resources count : "+ totalMobileResources.size());

    System.out.println("Copied Resources : "+ copiedResources);
    System.out.println("Copied Resources count : "+ copiedResources.size());

    System.out.println("Already present Resources : "+ presentResources);
    System.out.println("Already present Resources count : "+ presentResources.size());
}

private void processMobileResources(List<String> serverResources, File directory){
    if(directory.exists()) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if(file.isDirectory()){
                    processMobileResources(serverResources,file);
                }else{
                    totalMobileResources.add(file.getName());
                    if(!serverResources.contains(file.getName())) {
                        try {
                            FileCopyUtils.copy(file, new File(serverResourcePath + "/" + file.getName()));
                            copiedResources.add(file.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        presentResources.add(file.getName());
                    }
                }
            }
        }
    }
}
}
