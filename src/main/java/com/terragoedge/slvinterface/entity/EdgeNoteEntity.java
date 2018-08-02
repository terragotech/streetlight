package com.terragoedge.slvinterface.entity;

import com.vividsolutions.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.List;

public class EdgeNoteEntity {

    public static final String NOTE_ID = "noteId";
    public static final String NOTE_GUID = "noteGuid";
    public static final String CREATED_BY = "createdBy";
    public static final String CREATED_DATE_TIME = "createdDateTime";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String NOTES_TYPE = "notesType";
    public static final String RESOURCE_REF = "resourceRef";
    public static final String REVISION_FROM_NOTE_ID = "revisionfromNoteID";
    public static final String IS_CURRENT = "isCurrent";
    public static final String IS_TASKNOTE = "isTaskNote";
    public static final String IS_DELETED = "isDeleted";
    public static final String LOCATION_DESCRIPTION = "locationDescription";
    public static final String PARENT_NOTE_ID = "parentNoteId";
    public static final String EDGE_NOTEBOOK_ENTITY = "edgeNotebookEntity";
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
    public static final String SYNC_TIME = "syncTime";


    public static final String GEO_JSON = "geoJson";


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NoteID")
    private int noteId;
    @Column(name = "NoteGUID")
    private String noteGuid;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NotebookId")
    private EdgeNotebookEntity edgeNotebookEntity;
    @Column(name = "CreatedBy")
    private String createdBy;
    @Column(name = "CreatedDateTime")
    private long createdDateTime;
    @Column(name = "Description")
    private String description;
    @Column(name = "Title")
    public String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private NotesType notesType;
    @Column(name = "resourceRef")
    private String resourceRef;
    @Column(name = "revisionfromNoteID")
    private String revisionfromNoteID;
    @Column(name = "isCurrent")
    private boolean isCurrent;
    @Column(name = "isTaskNote")
    private boolean isTaskNote;
    @Column(name = "Geometry", columnDefinition = "geography")
    private Geometry geometry;
    @Column(name = "geojson")
    private String geoJson;
    @Column(name = "LocationDescription")
    public String locationDescription;
    @Column(name = "parentNoteId")
    private String parentNoteId;
    @Column(name = "isDeleted")
    private boolean isDeleted;
    @Column(name = "oauth")
    private String oAuth;

    @Column(name = "horizontalaccuracy")
    private String horizontalAccuracy;

    @Column(name = "altitude")
    private String altitude;
    @Column(name = "altitudeaccuracy")
    private String altitudeAccuracy;

    @Column(name = "satellitescount")
    private Integer satellitesCount;
    @Column(name = "bearing")
    private String bearing;

    @Column(name = "gpstime")
    private String gpsTime;
    @Column(name = "speed")
    private String speed;
    @Column(name = "corrected")
    private String corrected;
    @Column(name = "sourcetype")
    private String sourceType;
    @Column(name = "bearingtruenorth")
    private String bearingTruenorth;
    @Column(name = "bearingaccuracy")
    private String bearingAccuracy;
    @Column(name = "locktype")
    private String lockType;
    @Column(name = "locationprovider")
    private String locationProvider;
    @Column(name = "pdop")
    private String PDOP;
    @Column(name = "vdop")
    private String VDOP;
    @Column(name = "hdop")
    private String HDOP;

    @Column(name = "SyncTime")
    private Long syncTime;

    @OneToMany(mappedBy = "formId")
    private List<EdgeFormEntity> forms;


    public EdgeNoteEntity() {
    }

    public EdgeNoteEntity(RevisionSummary revisionSummary) {
        this.createdBy = revisionSummary.getCreatedBy();
        this.createdDateTime = revisionSummary.getCreatedDateTime();
        this.isDeleted = revisionSummary.getIsDeleted();
        this.isTaskNote = revisionSummary.getIsTaskNote();
        this.noteGuid = revisionSummary.getNoteGuid();

    }


    public void populateEdgeNote(Note note) {
        this.altitude = note.getAltitude();
        this.altitudeAccuracy = note.getAltitudeAccuracy();
        this.bearing = note.getBearing();
        this.bearingAccuracy = note.getBearingAccuracy();
        this.bearingTruenorth = note.getBearingTruenorth();
        this.corrected = note.getCorrected();
        this.notesType = NotesType.none;
        this.createdBy = note.getCreatedBy();
        //	this.createdDateTime = System.currentTimeMillis();
        this.createdDateTime = Long.valueOf(note.getCreatedDate());
        this.description = note.getDescription();
        // this.edgeNotebookEntity = null;
        // this.geoJson = null;
        // this.geometry = null;
        this.gpsTime = note.getGpsTime();
        this.HDOP = note.getHDOP();
        this.horizontalAccuracy = note.getHorizontalAccuracy();
        this.isCurrent = Boolean.valueOf(true);
        this.isDeleted = Boolean.valueOf(note.getIsDelete());
        this.isTaskNote = note.isIsTask() != null ? Boolean.valueOf(note.isIsTask()) : false;

        this.locationDescription = note.getLocationDescription();
        this.locationProvider = note.getLocationProvider();
        this.lockType = note.getLockType();
        this.noteGuid = note.getNoteId();
        //this.notesType = null;
        this.oAuth = null;
        this.parentNoteId = null;
        this.PDOP = note.getPDOP();
        this.resourceRef = note.getResourceRef();
        this.revisionfromNoteID = note.getRevisionOfNoteId();
        this.satellitesCount = note.getSatellitesCount();
        this.sourceType = note.getSourceType();
        this.speed = note.getSpeed();
        // this.syncTime =
        this.title = note.getNoteTitle();
        this.VDOP = note.getVDOP();
    }

    public EdgeNoteEntity(Note note) {
        populateEdgeNote(note);
    }

    public EdgeNoteEntity(EdgeNote edgeNote) {
        this.altitude = edgeNote.getAltitude();
        this.altitudeAccuracy = edgeNote.getAltitudeAccuracy();
        this.bearing = edgeNote.getBearing();
        this.bearingAccuracy = edgeNote.getBearingAccuracy();
        this.bearingTruenorth = edgeNote.getBearingTruenorth();
        this.corrected = edgeNote.getCorrected();
        this.createdBy = edgeNote.getCreatedBy();
        this.createdDateTime = edgeNote.getCreatedDateTime();
        this.description = edgeNote.getDescription();
        //this.edgeNotebookEntity =
        //this.geometry = edgeNote.getGeometry();
        this.gpsTime = edgeNote.getGpsTime();
        this.HDOP = edgeNote.getHDOP();
        this.horizontalAccuracy = edgeNote.getHorizontalAccuracy();
        this.isCurrent = true;
        this.isDeleted = false;
        this.isTaskNote = edgeNote.getIsTaskNote() != null ? edgeNote.getIsTaskNote() : false;
        this.locationDescription = edgeNote.getLocationDescription();
        this.locationProvider = edgeNote.getLocationProvider();
        this.lockType = edgeNote.getLockType();
        this.noteGuid = edgeNote.getNoteGuid();
        //this.notesType = NotesType.valueOf(edgeNote.getNotesType()); -- Moved out here to validate notes type
        //this.oAuth =
        //this.parentNoteId =
        this.PDOP = edgeNote.getPDOP();
        //this.resourceRef =  edgeNote.getResourceRef();
        //this.revisionfromNoteID =
        this.satellitesCount = edgeNote.getSatellitesCount();
        this.sourceType = edgeNote.getSourceType();
        this.speed = edgeNote.getSpeed();
        //this.syncTime =
        this.title = edgeNote.getTitle();
        this.VDOP = edgeNote.getVDOP();


    }

    public EdgeNoteEntity clone() {
        EdgeNoteEntity edgeNoteEntity = new EdgeNoteEntity();
        edgeNoteEntity.noteId = this.noteId;
        edgeNoteEntity.noteGuid = this.noteGuid;
        edgeNoteEntity.edgeNotebookEntity = this.edgeNotebookEntity;
        edgeNoteEntity.createdBy = this.createdBy;
        edgeNoteEntity.createdDateTime = this.createdDateTime;
        edgeNoteEntity.description = this.description;
        edgeNoteEntity.title = this.title;
        edgeNoteEntity.notesType = this.notesType;
        //edgeNoteEntity.resourceRef = this.resourceRef;
        edgeNoteEntity.revisionfromNoteID = this.revisionfromNoteID;
        edgeNoteEntity.isCurrent = this.isCurrent;
        edgeNoteEntity.isTaskNote = this.isTaskNote;
        edgeNoteEntity.geometry = this.geometry;
        edgeNoteEntity.locationDescription = this.locationDescription;
        edgeNoteEntity.parentNoteId = this.parentNoteId;
        edgeNoteEntity.isDeleted = this.isDeleted;
        edgeNoteEntity.oAuth = this.oAuth;
        edgeNoteEntity.horizontalAccuracy = this.horizontalAccuracy;
        edgeNoteEntity.altitude = this.altitude;
        edgeNoteEntity.altitudeAccuracy = this.altitudeAccuracy;
        edgeNoteEntity.satellitesCount = this.satellitesCount;
        edgeNoteEntity.bearing = this.bearing;
        edgeNoteEntity.gpsTime = this.gpsTime;
        edgeNoteEntity.speed = this.speed;
        edgeNoteEntity.corrected = this.corrected;
        edgeNoteEntity.sourceType = this.sourceType;
        edgeNoteEntity.bearingTruenorth = this.bearingTruenorth;
        edgeNoteEntity.bearingAccuracy = this.bearingAccuracy;
        edgeNoteEntity.lockType = this.lockType;
        edgeNoteEntity.locationProvider = this.locationProvider;
        edgeNoteEntity.PDOP = this.PDOP;
        edgeNoteEntity.VDOP = this.VDOP;
        edgeNoteEntity.HDOP = this.HDOP;
        edgeNoteEntity.syncTime = this.syncTime;
        return edgeNoteEntity;
    }

    public Long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Long syncTime) {
        this.syncTime = syncTime;
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

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public boolean isTaskNote() {
        return isTaskNote;
    }

    public void setTaskNote(boolean isTaskNote) {
        this.isTaskNote = isTaskNote;
    }

    public EdgeNotebookEntity getEdgeNotebookEntity() {
        return edgeNotebookEntity;
    }

    public void setEdgeNotebookEntity(EdgeNotebookEntity edgeNotebookEntity) {
        this.edgeNotebookEntity = edgeNotebookEntity;
    }

    public String getGeometry() {
        if (geoJson == null) {
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

    public Geometry getGeometryObj() {
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

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
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
        return "EdgeNoteEntity [noteId=" + noteId + ", noteGuid=" + noteGuid
                + ", edgeNotebookEntity=" + edgeNotebookEntity + ", createdBy="
                + createdBy + ", createdDateTime=" + createdDateTime
                + ", description=" + description + ", title=" + title
                + ", notesType=" + notesType
                + ", revisionfromNoteID=" + revisionfromNoteID + ", isCurrent="
                + isCurrent + ", isTaskNote=" + isTaskNote + ", geometry="
                + geometry + ", locationDescription=" + locationDescription
                + ", parentNoteId=" + parentNoteId + ", isDeleted=" + isDeleted
                + ", oAuth=" + oAuth + ", horizontalAccuracy="
                + horizontalAccuracy + ", altitude=" + altitude
                + ", altitudeAccuracy=" + altitudeAccuracy
                + ", satellitesCount=" + satellitesCount + ", bearing="
                + bearing + ", gpsTime=" + gpsTime + ", speed=" + speed
                + ", corrected=" + corrected + ", sourceType=" + sourceType
                + ", bearingTruenorth=" + bearingTruenorth
                + ", bearingAccuracy=" + bearingAccuracy + ", lockType="
                + lockType + ", locationProvider=" + locationProvider
                + ", PDOP=" + PDOP + ", VDOP=" + VDOP + ", HDOP=" + HDOP + "]";
    }


}
