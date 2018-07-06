package com.terragoedge.edgeserver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.annotation.Resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



public class EdgeNote {

	private String PDOP = null;
	private String gpsTime = null;
	private String altitude = null;
	private String title = null;
	private List<FormData> formData = new ArrayList<>();
	private Integer satellitesCount = null;
	private String description = null;
	private String HDOP = null;
	private String notesType = null;
	private Boolean isTaskNote = null;
	private String locationDescription = null;
	private String speed = null;
	private String horizontalAccuracy = null;
	private String locationProvider = null;
	private String noteGuid = null;
	private String geometry = null;
	private String VDOP = null;
	private String createdBy = null;
	private String lockType = null;
	private Long createdDateTime = null;
	private String bearing = null;
	private String bearingAccuracy = null;
	private String bearingTruenorth = null;
	private String corrected = null;
	private String resourceRef = null;
	private String sourceType = null;

	private String altitudeAccuracy = null;



private EdgeNotebook edgeNotebook = null;
	public EdgeNote() {

	}

	public String getPDOP() {
		return PDOP;
	}

	public void setPDOP(String pDOP) {
		PDOP = pDOP;
	}

	public String getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(String gpsTime) {
		this.gpsTime = gpsTime;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setFormData(List<FormData> formData) {
		this.formData = formData;
	}

	public List<FormData> getFormData() {
		System.out.println(formData);
		System.out.println(formData.toString());
	/*	Type listType = new TypeToken<ArrayList<FormData>>() {
		}.getType();
		Gson gson = new Gson();
		List<FormData> edgeNoteList = gson.fromJson(formData.toString(), listType);*/
		
		return formData;
	}

	

	public Integer getSatellitesCount() {
		return satellitesCount;
	}

	public void setSatellitesCount(Integer satellitesCount) {
		this.satellitesCount = satellitesCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHDOP() {
		return HDOP;
	}

	public void setHDOP(String hDOP) {
		HDOP = hDOP;
	}

	

	public String getNotesType() {
		return notesType;
	}

	public void setNotesType(String notesType) {
		this.notesType = notesType;
	}

	public Boolean getIsTaskNote() {
		return isTaskNote;
	}

	public void setIsTaskNote(Boolean isTaskNote) {
		this.isTaskNote = isTaskNote;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public void setLocationDescription(String locationDescription) {
		this.locationDescription = locationDescription;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getHorizontalAccuracy() {
		return horizontalAccuracy;
	}

	public void setHorizontalAccuracy(String horizontalAccuracy) {
		this.horizontalAccuracy = horizontalAccuracy;
	}

	
	public String getLocationProvider() {
		return locationProvider;
	}

	public void setLocationProvider(String locationProvider) {
		this.locationProvider = locationProvider;
	}

	public String getNoteGuid() {
		return noteGuid;
	}

	public void setNoteGuid(String noteGuid) {
		this.noteGuid = noteGuid;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

	public String getVDOP() {
		return VDOP;
	}

	public void setVDOP(String vDOP) {
		VDOP = vDOP;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLockType() {
		return lockType;
	}

	public void setLockType(String lockType) {
		this.lockType = lockType;
	}

	public Long getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Long createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getBearing() {
		return bearing;
	}

	public void setBearing(String bearing) {
		this.bearing = bearing;
	}

	public String getBearingAccuracy() {
		return bearingAccuracy;
	}

	public void setBearingAccuracy(String bearingAccuracy) {
		this.bearingAccuracy = bearingAccuracy;
	}

	public String getBearingTruenorth() {
		return bearingTruenorth;
	}

	public void setBearingTruenorth(String bearingTruenorth) {
		this.bearingTruenorth = bearingTruenorth;
	}

	public String getCorrected() {
		return corrected;
	}

	public void setCorrected(String corrected) {
		this.corrected = corrected;
	}

	public String getResourceRef() {
		return resourceRef;
	}

	public void setResourceRef(String resourceRef) {
		this.resourceRef = resourceRef;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	
	public String getAltitudeAccuracy() {
		return altitudeAccuracy;
	}

	public void setAltitudeAccuracy(String altitudeAccuracy) {
		this.altitudeAccuracy = altitudeAccuracy;
	}


    public EdgeNotebook getEdgeNotebook() {
        return edgeNotebook;
    }

    public void setEdgeNotebook(EdgeNotebook edgeNotebook) {
        this.edgeNotebook = edgeNotebook;
    }
}
