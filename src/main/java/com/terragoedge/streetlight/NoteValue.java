package com.terragoedge.streetlight;

public class NoteValue {

	String latitude = null;
	String longitude = null;
	String createdDate = null;
	String title = null;

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "NoteValue [latitude=" + latitude + ", longitude=" + longitude + ", createdDate=" + createdDate
				+ ", title=" + title + "]";
	}

}
