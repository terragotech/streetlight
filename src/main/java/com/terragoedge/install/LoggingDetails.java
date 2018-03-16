package com.terragoedge.install;

public class LoggingDetails {

	private String noteId;
	private String noteGuid;
	private String title;
	private String status;
	private String actionType;
	private String totalForms;
	private String description;
	private long createDateTime;
	
	

	public long getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(long createDateTime) {
		this.createDateTime = createDateTime;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTotalForms() {
		return totalForms;
	}

	public void setTotalForms(String totalForms) {
		this.totalForms = totalForms;
	}

	@Override
	public String toString() {
		return "LoggingDetails [noteId=" + noteId + ", noteGuid=" + noteGuid + ", title=" + title + ", status=" + status
				+ ", actionType=" + actionType + ", totalForms=" + totalForms + ", description=" + description
				+ ", createDateTime=" + createDateTime + "]";
	}

	

}
