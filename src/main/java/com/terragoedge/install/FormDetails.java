package com.terragoedge.install;

public class FormDetails {

	private String formDef;
	private String formTemplateDef;
	private String formGuid;
	private String formTemplateGuid;

	public String getFormDef() {
		return formDef;
	}

	public void setFormDef(String formDef) {
		this.formDef = formDef;
	}

	public String getFormTemplateDef() {
		return formTemplateDef;
	}

	public void setFormTemplateDef(String formTemplateDef) {
		this.formTemplateDef = formTemplateDef;
	}

	public String getFormGuid() {
		return formGuid;
	}

	public void setFormGuid(String formGuid) {
		this.formGuid = formGuid;
	}

	public String getFormTemplateGuid() {
		return formTemplateGuid;
	}

	public void setFormTemplateGuid(String formTemplateGuid) {
		this.formTemplateGuid = formTemplateGuid;
	}

	@Override
	public String toString() {
		return "FormDetails [formDef=" + formDef + ", formTemplateDef=" + formTemplateDef + ", formGuid=" + formGuid
				+ ", formTemplateGuid=" + formTemplateGuid + "]";
	}

}
