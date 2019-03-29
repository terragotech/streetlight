package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edgenoteview")
public class EdgeNoteView {
	
	public static final String GROUP_NAME = "groupname";
	public static final String GROUP_ID = "groupGuid";
	public static final String COLOR_NAME = "colorName";
	
	public static final String NOTE_ID = "noteid";
	public static final String NOTE_GUID = "noteguid";
	public static final String CREATED_BY = "createdBy";
	public static final String CREATED_DATE_TIME = "createddatetime";
	public static final String DESCRIPTION = "description";
	public static final String TITLE = "title";
	public static final String NOTES_TYPE = "notesType";
	public static final String RESOURCE_REF = "resourceRef";
	public static final String REVISION_FROM_NOTE_ID = "revisionfromNoteID";
	public static final String IS_CURRENT = "iscurrent";
	public static final String IS_TASKNOTE = "isTaskNote";
	public static final String IS_DELETED = "isdeleted";
	public static final String LOCATION_DESCRIPTION = "locationDescription";
	public static final String PARENT_NOTE_ID = "parentNoteId";
	public static final String EDGE_NOTEBOOK_ENTITY = "notebookid";
	public static final String GEOMETRY = "geometry";
	public static final String OAUTH = "oAuth";
	
	public static final String HORIZONTAL_ACCURACY = "horizontalAccuracy";
	public static final String ALTITUDE = "altitude";
	public static final String ALTITUDE_ACCURCY = "altitudeAccuracy";
	public static final String SATELLITES_COUNT = "satellitesCount";
	public static final String BEARING = "bearing";
	public static final String GPS_TIME = "gpsTime";
	public static final String SPEED = "speed";
	public static final String CORRECTED = "corrected";
	public static final String SOURCE_TYPE = "sourceType";
	public static final String BEARING_TRUE_NORTH = "bearingTruenorth";
	public static final String BEARING_ACCURACY = "bearingAccuracy";
	
	public static final String LOCK_TYPE = "lockType";
	public static final String LOCATION_PROVIDER = "locationProvider";
	public static final String PDO_P = "PDOP";
	public static final String VDO_P = "VDOP";
	public static final String HDO_P = "HDOP";
	public static final String SYNC_TIME = "synctime";
	
	public static final String TAGS = "tags";

	public static final String GEO_JSON = "geoJson";
	@DatabaseField(columnName = "noteid")
	private int noteId;
	@DatabaseField(columnName = "noteguid")
	private String noteGuid;
	@DatabaseField(columnName = "createdby")
	private String createdBy;
	@DatabaseField(columnName = "createddatetime")
	private long createdDateTime;
	@DatabaseField(columnName = "description")
	private String description;
	@DatabaseField(columnName= "title")
	private String title;

	@DatabaseField(columnName= "resourceref")
	private String resourceRef;
	@DatabaseField(columnName = "revisionfromnoteid")
	private String revisionfromNoteID;
	@DatabaseField(columnName= "iscurrent")
	private boolean isCurrent;
	@DatabaseField(columnName = "istasknote")
	private boolean isTaskNote;
	/*@DatabaseField(columnName = "Geometry",columnDefinition = "geography")
	private Geometry geometry;*/
	@DatabaseField(columnName= "locationdescription")
	private String locationDescription;
	@DatabaseField(columnName = "parentnoteid")
	private String parentNoteId;
	@DatabaseField(columnName= "isdeleted")
	private boolean isDeleted;
	@DatabaseField(columnName = "oauth")
	private String oAuth;

	@DatabaseField(columnName = "horizontalaccuracy")
	private String horizontalAccuracy;

	@DatabaseField(columnName= "altitude")
	private String altitude;
	@DatabaseField(columnName= "altitudeaccuracy")
	private String altitudeAccuracy;

	@DatabaseField(columnName = "satellitescount")
	private Integer satellitesCount;
	@DatabaseField(columnName= "bearing")
	private String bearing;

	@DatabaseField(columnName = "gpstime")
	private String gpsTime;
	@DatabaseField(columnName = "speed")
	private String speed;
	@DatabaseField(columnName = "corrected")
	private String corrected;
	@DatabaseField(columnName = "sourcetype")
	private String sourceType;
	@DatabaseField(columnName = "bearingtruenorth")
	private String bearingTruenorth;
	@DatabaseField(columnName = "bearingaccuracy")
	private String bearingAccuracy;
	@DatabaseField(columnName = "locktype")
	private String lockType;
	@DatabaseField(columnName = "locationprovider")
	private String locationProvider;
	@DatabaseField(columnName= "pdop")
	private String PDOP;
	@DatabaseField(columnName= "vdop")
	private String VDOP;
	@DatabaseField(columnName= "hdop")
	private String HDOP;

	@DatabaseField(columnName= "synctime")
	private Long syncTime;

	@DatabaseField(columnName= "groupname")
	private String groupName;
	@DatabaseField(columnName = "colorname")
	private String colorName;
	@DatabaseField(columnName= "groupid")
	private String groupGuid;

	private String tags;

	@DatabaseField(columnName= "notebookid")
	private String notebookid;
	@DatabaseField(columnName= "geojson")
	private String geoJson;
/*

	@DatabaseField(columnName = "noteid", foreignAutoRefresh = true, foreign = true)
	private List<EdgeFormEntity> forms;
*/

	public String getNoteGuid() {
		return noteGuid;
	}

	public void setNoteGuid(String noteGuid) {
		this.noteGuid = noteGuid;
	}

	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getColorName() {
		return colorName;
	}

	public void setColorName(String colorName) {
		this.colorName = colorName;
	}

	public String getGroupGuid() {
		return groupGuid;
	}

	public void setGroupGuid(String groupGuid) {
		this.groupGuid = groupGuid;
	}

	public int getNoteId() {
		return noteId;
	}

	public void setNoteId(int noteId) {
		this.noteId = noteId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(long createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getResourceRef() {
		return resourceRef;
	}

	public void setResourceRef(String resourceRef) {
		this.resourceRef = resourceRef;
	}

	public String getRevisionfromNoteID() {
		return revisionfromNoteID;
	}

	public void setRevisionfromNoteID(String revisionfromNoteID) {
		this.revisionfromNoteID = revisionfromNoteID;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean current) {
		isCurrent = current;
	}

	public boolean isTaskNote() {
		return isTaskNote;
	}

	public void setTaskNote(boolean taskNote) {
		isTaskNote = taskNote;
	}
/*

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
*/

	public String getLocationDescription() {
		return locationDescription;
	}

	public void setLocationDescription(String locationDescription) {
		this.locationDescription = locationDescription;
	}

	public String getParentNoteId() {
		return parentNoteId;
	}

	public void setParentNoteId(String parentNoteId) {
		this.parentNoteId = parentNoteId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	public String getoAuth() {
		return oAuth;
	}

	public void setoAuth(String oAuth) {
		this.oAuth = oAuth;
	}

	public String getHorizontalAccuracy() {
		return horizontalAccuracy;
	}

	public void setHorizontalAccuracy(String horizontalAccuracy) {
		this.horizontalAccuracy = horizontalAccuracy;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getAltitudeAccuracy() {
		return altitudeAccuracy;
	}

	public void setAltitudeAccuracy(String altitudeAccuracy) {
		this.altitudeAccuracy = altitudeAccuracy;
	}

	public Integer getSatellitesCount() {
		return satellitesCount;
	}

	public void setSatellitesCount(Integer satellitesCount) {
		this.satellitesCount = satellitesCount;
	}

	public String getBearing() {
		return bearing;
	}

	public void setBearing(String bearing) {
		this.bearing = bearing;
	}

	public String getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(String gpsTime) {
		this.gpsTime = gpsTime;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getCorrected() {
		return corrected;
	}

	public void setCorrected(String corrected) {
		this.corrected = corrected;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getBearingTruenorth() {
		return bearingTruenorth;
	}

	public void setBearingTruenorth(String bearingTruenorth) {
		this.bearingTruenorth = bearingTruenorth;
	}

	public String getBearingAccuracy() {
		return bearingAccuracy;
	}

	public void setBearingAccuracy(String bearingAccuracy) {
		this.bearingAccuracy = bearingAccuracy;
	}

	public String getLockType() {
		return lockType;
	}

	public void setLockType(String lockType) {
		this.lockType = lockType;
	}

	public String getLocationProvider() {
		return locationProvider;
	}

	public void setLocationProvider(String locationProvider) {
		this.locationProvider = locationProvider;
	}

	public String getPDOP() {
		return PDOP;
	}

	public void setPDOP(String PDOP) {
		this.PDOP = PDOP;
	}

	public String getVDOP() {
		return VDOP;
	}

	public void setVDOP(String VDOP) {
		this.VDOP = VDOP;
	}

	public String getHDOP() {
		return HDOP;
	}

	public void setHDOP(String HDOP) {
		this.HDOP = HDOP;
	}

	public Long getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Long syncTime) {
		this.syncTime = syncTime;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getNotebookid() {
		return notebookid;
	}

	public void setNotebookid(String notebookid) {
		this.notebookid = notebookid;
	}

	@Override
	public String toString() {
		return "EdgeNoteView [groupName=" + groupName + ", groupGuid="
				+ groupGuid + ",colorName=" + colorName + "]";
	}

}
