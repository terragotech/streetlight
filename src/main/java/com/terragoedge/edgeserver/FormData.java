package com.terragoedge.edgeserver;

import java.util.ArrayList;
import java.util.List;

public class FormData {

	private String formTemplateGuid = null;
	private List<EdgeFormData> formDef = new ArrayList<>();
	private String category = null;
	private String formGuid = null;
	private String formTemplateDef = null;
	private String name = null;

	public String getFormTemplateGuid() {
		return formTemplateGuid;
	}

	public void setFormTemplateGuid(String formTemplateGuid) {
		this.formTemplateGuid = formTemplateGuid;
	}

	public List<EdgeFormData> getFormDef() {
		return formDef;
	}

	public void setFormDef(List<EdgeFormData> formDef) {
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

	public String getFormTemplateDef() {
		return formTemplateDef;
	}

	public void setFormTemplateDef(String formTemplateDef) {
		this.formTemplateDef = formTemplateDef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FormData [formTemplateGuid=" + formTemplateGuid + ", formDef=" + formDef + ", category=" + category
				+ ", formGuid=" + formGuid + ", formTemplateDef=" + formTemplateDef + ", name=" + name + "]";
	}

}
