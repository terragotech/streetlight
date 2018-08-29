package com.terragoedge.slvinterface.model;

import java.util.ArrayList;
import java.util.List;

public class NoteDetails {
	private String title;
	private String noteGuid;
	private String noteid;
	private String geojson;
	private String lat;
	private String lng;
	private long createdDateTime;
	private List<FormData> formData = new ArrayList<>();



	public long getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(long createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNoteGuid() {
		return noteGuid;
	}

	public void setNoteGuid(String noteGuid) {
		this.noteGuid = noteGuid;
	}

	public String getNoteid() {
		return noteid;
	}

	public void setNoteid(String noteid) {
		this.noteid = noteid;
	}

	public String getGeojson() {
		return geojson;
	}

	public void setGeojson(String geojson) {
		this.geojson = geojson;
	}

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

	public List<FormData> getFormData() {
		return formData;
	}

	public void setFormData(List<FormData> formData) {
		this.formData = formData;
	}

	@Override
	public String toString() {
		return "NoteDetails [title=" + title + ", noteGuid=" + noteGuid + ", noteid=" + noteid + ", geojson=" + geojson
				+ ", lat=" + lat + ", lng=" + lng + ", formData=" + formData + "]";
	}

}
