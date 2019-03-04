package com.terragoedge.streetlight.installmaintain;

import com.terragoedge.streetlight.dao.UtilDao;
import com.terragoedge.streetlight.installmaintain.json.Config;
import com.terragoedge.streetlight.installmaintain.json.Ids;
import com.terragoedge.streetlight.installmaintain.json.Prop;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class InstallMaintenanceDao extends UtilDao {

    public void doProcess(){
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try{
            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid from edgenote where title in (select distinct title from edgenote where createddatetime >= ) where iscurrent = true and isdeleted = false;");
            while (queryResponse.next()) {
               String noteGuid = queryResponse.getString("noteguid");
               String parentNoteId =  queryResponse.getString("parentnoteid");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }


    public void getCurrentNoteDetails(String noteGuid){
        PreparedStatement queryStatement = null;
        ResultSet queryResponse = null;
        try{
            queryStatement = connection.prepareStatement("select formdef,formtemplateguid from edgeform where edgenoteentity_noteid in (select noteid from edgenote where noteguid = ?)");
            queryStatement.setString(1,noteGuid);
            queryResponse = queryStatement.executeQuery();
            while (queryResponse.next()) {
                String formDef = queryResponse.getString("formdef");
                String formTemplateGuid = queryResponse.getString("formtemplateguid");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }

    private InstallMaintenanceModel getInstallMaintenanceModel(List<Config> configList, String formDef, String formTemplateGuid){
        for(Config config : configList){
            if(config.getFormTemplateGuid().equals(formTemplateGuid)){
                List<Prop> props = config.getProps();
                for(Prop prop : props){
                    List<Ids> idsList = prop.getIds();
                    prop.getType();
                }
            }
        }
    }
}
