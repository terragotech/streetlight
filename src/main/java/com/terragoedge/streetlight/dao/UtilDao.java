package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.terragoedge.streetlight.StreetlightDaoConnection;

public abstract class UtilDao {
	
	Connection connection = null;
	public static String Installation_Maintenance = "select title, replace(replace(substring(a.formdef from 'Node MAC address#(.+?)count'),'\"',''),',','') nodemacaddress,  replace(replace(substring(a.formdef from 'New Node MAC Address#(.+?)count'),'\"',''),',','') newnodemacaddress,  replace(replace(substring(substring(a.formdef from(position('New Node MAC Address#' in a.formdef)+20)) from 'New Node MAC Address#(.+?)count'),'\"',''),',','') newnodemacaddressreplace  from edgenote, edgeform a where    a.formtemplateguid = 'c8acc150-6228-4a27-bc7e-0fabea0e2b93' and  a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';";
    public static String Installation_Maintenance_Updated = "select title,replace(replace(substring(a.formdef from 'Node MAC address#(.+?)count'),'\"',''),',','') nodemacaddress,  replace(replace(substring(a.formdef from 'New Node MAC Address#(.+?)count'),'\"',''),',','') newnodemacaddress,  replace(replace(substring(substring(a.formdef from(position('New Node MAC Address#' in a.formdef)+20)) from 'New Node MAC Address#(.+?)count'),'\"',''),',','') newnodemacaddressreplace  from edgenote, edgeform a where    a.formtemplateguid = 'fa47c708-fb82-4877-938c-992e870ae2a4' and  a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';";
    public static String Replace_Node = "select title, replace(replace(substring(a.formdef from 'New Node MAC Address#(.+?)count'),'\"',''),',','') newnodemacaddressreplace from edgenote, edgenotebook, edgeform a where    a.formtemplateguid = '606fb4ca-40a4-466b-ac00-7c0434f82bfa' and  a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';";
    public static String New_Installation_QR_Scan = "select  title, replace(replace(substring(a.formdef from 'Node MAC address#(.+?)count'),'\"',''),',','') nodemacaddress   from edgenote, edgenotebook, edgeform a where   a.formtemplateguid = '0ea4f5d4-0a17-4a17-ba8f-600de1e2515f' and  a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';";
    public static String New_Installation_Missing_fixtures = "select  title, replace(replace(substring(a.formdef from 'Node MAC address#(.+?)count'),'\"',''),',','') nodemacaddress   from edgenote, edgenotebook, edgeform a where   a.formtemplateguid = '7d8f04dd-f404-43df-bc36-cac015bffff7' and  a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';";

	
	public UtilDao(){
		connection = StreetlightDaoConnection.getInstance().getConnection();
	}

	
	public void closeResultSet(ResultSet resultSet){
		if(resultSet != null){
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void closeStatement(Statement queryStatement){
		if(queryStatement != null){
			try {
				queryStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}
