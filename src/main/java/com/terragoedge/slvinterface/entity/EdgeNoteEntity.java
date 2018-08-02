package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.terragoedge.slvinterface.model.NotesType;
import com.vividsolutions.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.List;


@DatabaseTable(tableName = "edgenote")
public class EdgeNoteEntity {


	private int noteId;

	@DatabaseField(columnName = "noteguid")
	private String noteGuid;
	@DatabaseField(columnName = "createdby")
	private String createdBy;
	@DatabaseField(columnName = "createddatetime")
	private long createdDateTime;
	@DatabaseField(columnName = "description")
	private String description;
	@DatabaseField(columnName = "title")
	public String title;
	@DatabaseField(columnName = "type")
	private NotesType notesType;

	@DatabaseField(columnName = "revisionfromnoteid")
	private String revisionfromNoteID;
	@DatabaseField(columnName = "iscurrent")
	private boolean isCurrent;


	@DatabaseField(columnName = "geojson")
	private String geoJson;
	@DatabaseField(columnName = "locationdescription")
	public String locationDescription;
	@DatabaseField(columnName = "parentnoteid")
	private String parentNoteId;
	@DatabaseField(columnName = "isdeleted")
	private boolean isDeleted;

	@DatabaseField(columnName = "horizontalaccuracy")
	private String horizontalAccuracy;

	@DatabaseField(columnName = "altitude")
	private String altitude;
	@DatabaseField(columnName = "altitudeaccuracy")
	private String altitudeAccuracy;

	@DatabaseField(columnName = "satellitescount")
	private Integer satellitesCount;
	@DatabaseField(columnName = "bearing")
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
	@DatabaseField(columnName = "pdop")
	private String PDOP;
	@DatabaseField(columnName = "vdop")
	private String VDOP;
	@DatabaseField(columnName = "hdop")
	private String HDOP;




	//private List<EdgeFormEntity> forms;
	
	

	public EdgeNoteEntity(){
	}
	




	public int getNoteId() {
		return noteId;
	}

	public void setNoteId(int noteId) {
		this.noteId = noteId;
	}

	public String getNoteGuid() {
		return noteGuid;
	}

	public void setNoteGuid(String noteGuid) {
		this.noteGuid = noteGuid;
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

	public NotesType getNotesType() {
		return notesType;
	}

	public void setNotesType(NotesType notesType) {
		this.notesType = notesType;
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

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	


	public String getGeometry() {
		if(geoJson == null){
			return GeometryHelper.edgeJSONFromGeometry(geometry);
		}
		return geoJson;
	}

	
	
	public String getGeoJson() {
		return geoJson;
	}

	public void setGeoJson(String geoJson) {
		this.geoJson = geoJson;
	}

	public Geometry getGeometryObj(){
		return geometry;
	}

	public void setGeometry(String geomString) {
		System.out.println(geomString);
		// parse Feature or FeatureCollection
		Feature feature = (Feature) GeoJSONFactory.create(geomString);

		// parse Geometry from Feature
		GeoJSONReader reader = new GeoJSONReader();
		Geometry geom = reader.read(feature.getGeometry());
		System.out.println(geom.toString());
		this.geometry = geom;
	}

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

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
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

	public void setPDOP(String pDOP) {
		PDOP = pDOP;
	}

	public String getVDOP() {
		return VDOP;
	}

	public void setVDOP(String vDOP) {
		VDOP = vDOP;
	}

	public String getHDOP() {
		return HDOP;
	}

	public void setHDOP(String hDOP) {
		HDOP = hDOP;
	}


    @Override
    public String toString() {
        return "EdgeNoteEntity{" +
                "noteId=" + noteId +
                ", noteGuid='" + noteGuid + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", notesType=" + notesType +
                ", revisionfromNoteID='" + revisionfromNoteID + '\'' +
                ", isCurrent=" + isCurrent +
                ", geoJson='" + geoJson + '\'' +
                ", locationDescription='" + locationDescription + '\'' +
                ", parentNoteId='" + parentNoteId + '\'' +
                ", isDeleted=" + isDeleted +
                ", horizontalAccuracy='" + horizontalAccuracy + '\'' +
                ", altitude='" + altitude + '\'' +
                ", altitudeAccuracy='" + altitudeAccuracy + '\'' +
                ", satellitesCount=" + satellitesCount +
                ", bearing='" + bearing + '\'' +
                ", gpsTime='" + gpsTime + '\'' +
                ", speed='" + speed + '\'' +
                ", corrected='" + corrected + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", bearingTruenorth='" + bearingTruenorth + '\'' +
                ", bearingAccuracy='" + bearingAccuracy + '\'' +
                ", lockType='" + lockType + '\'' +
                ", locationProvider='" + locationProvider + '\'' +
                ", PDOP='" + PDOP + '\'' +
                ", VDOP='" + VDOP + '\'' +
                ", HDOP='" + HDOP + '\'' +
                '}';
    }
}
