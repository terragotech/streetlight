package com.terragoedge.edgeserver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FormData {

	private String formTemplateGuid = null;
	private String formDef = null;
	private String category = null;
	private String formGuid = null;
	//private String formTemplateDef = null;
	private String name = null;

	public String getFormTemplateGuid() {
		return formTemplateGuid;
	}

	public void setFormTemplateGuid(String formTemplateGuid) {
		this.formTemplateGuid = formTemplateGuid;
	}

	public List<EdgeFormData> getFormDef() {
		Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
		}.getType();
		Gson gson = new Gson();
		List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
		return edgeFormDatas;
	}


	public void setFormDef(String formDef) {
		this.formDef = formDef;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFormGuid() {
		return formGuid;
	}

	public void setFormGuid(String formGuid) {
		this.formGuid = formGuid;
	}

	/*public String getFormTemplateDef() {
		return formTemplateDef;
	}

	public void setFormTemplateDef(String formTemplateDef) {
		this.formTemplateDef = formTemplateDef;
	}*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

}
