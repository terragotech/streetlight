package com.terragoedge.streetlight.entity;

public class EdgeFormValues {

	private String formTempalteDef;
	private String formDef;
	private String formGuid;
	private String formTemplateGuid;
	private String formName;

	public String getFormTempalteDef() {
		return formTempalteDef;
	}

	public void setFormTempalteDef(String formTempalteDef) {
		this.formTempalteDef = formTempalteDef;
	}

	public String getFormDef() {
		return formDef;
	}

	public void setFormDef(String formDef) {
		this.formDef = formDef;
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

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	@Override
	public String toString() {
		return "EdgeFormValues [formTempalteDef=" + formTempalteDef + ", formDef=" + formDef + ", formGuid=" + formGuid
				+ ", formTemplateGuid=" + formTemplateGuid + ", formName=" + formName + "]";
	}

}
