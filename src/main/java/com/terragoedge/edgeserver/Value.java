package com.terragoedge.edgeserver;

public class Value {

	private String address;
	private String categoryStrId;
	private String controllerStrId;
	private String functionId;
	private Integer geoZoneId;
	private String geoZoneNamesPath;
	private Integer id;
	private String idOnController;
	private Float lat;
	private Float lng;
	private String modelName;
	private String name;
	private String nodeTypeStrId;
	private String technologyStrId;
	private String type;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCategoryStrId() {
		return categoryStrId;
	}

	public void setCategoryStrId(String categoryStrId) {
		this.categoryStrId = categoryStrId;
	}

	public String getControllerStrId() {
		return controllerStrId;
	}

	public void setControllerStrId(String controllerStrId) {
		this.controllerStrId = controllerStrId;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public Integer getGeoZoneId() {
		return geoZoneId;
	}

	public void setGeoZoneId(Integer geoZoneId) {
		this.geoZoneId = geoZoneId;
	}

	public String getGeoZoneNamesPath() {
		return geoZoneNamesPath;
	}

	public void setGeoZoneNamesPath(String geoZoneNamesPath) {
		this.geoZoneNamesPath = geoZoneNamesPath;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdOnController() {
		return idOnController;
	}

	public void setIdOnController(String idOnController) {
		this.idOnController = idOnController;
	}

	public Float getLat() {
		return lat;
	}

	public void setLat(Float lat) {
		this.lat = lat;
	}

	public Float getLng() {
		return lng;
	}

	public void setLng(Float lng) {
		this.lng = lng;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNodeTypeStrId() {
		return nodeTypeStrId;
	}

	public void setNodeTypeStrId(String nodeTypeStrId) {
		this.nodeTypeStrId = nodeTypeStrId;
	}

	public String getTechnologyStrId() {
		return technologyStrId;
	}

	public void setTechnologyStrId(String technologyStrId) {
		this.technologyStrId = technologyStrId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Value [address=" + address + ", categoryStrId=" + categoryStrId + ", controllerStrId=" + controllerStrId
				+ ", functionId=" + functionId + ", geoZoneId=" + geoZoneId + ", geoZoneNamesPath=" + geoZoneNamesPath
				+ ", id=" + id + ", idOnController=" + idOnController + ", lat=" + lat + ", lng=" + lng + ", modelName="
				+ modelName + ", name=" + name + ", nodeTypeStrId=" + nodeTypeStrId + ", technologyStrId="
				+ technologyStrId + ", type=" + type + "]";
	}

}
