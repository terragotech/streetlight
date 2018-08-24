package com.terragoedge.slvinterface.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.log4j.Logger;

@DatabaseTable(tableName = "edgeform")
public class EdgeFormEntity {

    private static  final Logger logger = Logger.getLogger(EdgeFormEntity.class);


	public static final String NOTEID = "edgenoteentity_noteid";
	public static final String FORM_ID = "formId";
	public static final String FORM_GUID = "formGuid";
	public static final String CREATED_DATE = "createdDate";
	public static final String FORM_TEMPLATE_GUID = "formtemplateguid";
	public static final String FORM_TEMPLATE_DEF = "formtemplatedef";
	public static final String FROM_DEF = "formdef";
	public static final String CREATED_BY = "createdBy";
	public static final String EDGE_NOTE_ENTITY = "edgeNoteEntity";
	public static final String NAME = "name";
	public static final String CATEGORY = "category";

	@DatabaseField(columnName = "edgenoteentity_noteid")
	private int edgenoteentity_noteid;

	@DatabaseField(columnName = "formId")
	private int formId;

	@DatabaseField(columnName = "formGuid")
	private String formGuid;

	@DatabaseField(columnName = "CreatedDate")
	private long createdDate;

	@DatabaseField(columnName = "formtemplateguid")
	private String formTemplateGuid;

	@DatabaseField(columnName="formTemplateDef")
	private String formTemplateDef;

	@DatabaseField(columnName = "formDef")
	private String formDef;

	@DatabaseField(columnName = "createdBy")
	private String createdBy;

	@DatabaseField(columnName= "name")
	private String name;

	@DatabaseField(columnName= "category")
	private String category;
	

	public EdgeFormEntity(){
		
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public String getFormGuid() {
		return formGuid;
	}

	public void setFormGuid(String formGuid) {
		this.formGuid = formGuid;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public String getFormTemplateGuid() {
		return formTemplateGuid;
	}

	public void setFormTemplateGuid(String formTemplateGuid) {
		this.formTemplateGuid = formTemplateGuid;
	}

	public String getFormTemplateDef() {
		return formTemplateDef;
	}

	public void setFormTemplateDef(String formTemplateDef) {
		this.formTemplateDef = formTemplateDef;
	}

	public String getFormDef() {
		return formDef;
	}

	public void setFormDef(String formDef) {
		this.formDef = formDef;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getEdgenoteentity_noteid() {
		return edgenoteentity_noteid;
	}

	public void setEdgenoteentity_noteid(int edgenoteentity_noteid) {
		this.edgenoteentity_noteid = edgenoteentity_noteid;
	}
}
