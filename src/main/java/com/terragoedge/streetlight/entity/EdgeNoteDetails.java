package com.terragoedge.streetlight.entity;

public class EdgeNoteDetails {

	private String noteId;
	private String noteGuid;
	private String parentNoteId;
	private String title;
	private EdgeFormValues edgeFormValues;
	private String geoJson;
	private String lat;
	private String lng;
	private String createdDateTime;
	

	
	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getNoteId() {
		return noteId;
	}

	public void setNoteId(String noteId) {
		this.noteId = noteId;
	}

	public String getNoteGuid() {
		return noteGuid;
	}

	public void setNoteGuid(String noteGuid) {
		this.noteGuid = noteGuid;
	}

	public String getParentNoteId() {
		return parentNoteId;
	}

	public void setParentNoteId(String parentNoteId) {
		this.parentNoteId = parentNoteId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public EdgeFormValues getEdgeFormValues() {
		return edgeFormValues;
	}

	public void setEdgeFormValues(EdgeFormValues edgeFormValues) {
		this.edgeFormValues = edgeFormValues;
	}

	@Override
	public String toString() {
		return "EdgeNoteDetails [noteId=" + noteId + ", noteGuid=" + noteGuid + ", parentNoteId=" + parentNoteId
				+ ", title=" + title + ", edgeFormValues=" + edgeFormValues + "]";
	}

	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}

	public String getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

}
