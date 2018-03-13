package com.terragoedge.install;

public class FormValues {

	private Integer id;
	private String label;
	private String value;
	private Integer count;
	private Integer groupId;
	private Integer groupRepeatableCount;
	private Boolean isGroup;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Integer getGroupRepeatableCount() {
		return groupRepeatableCount;
	}
	public void setGroupRepeatableCount(Integer groupRepeatableCount) {
		this.groupRepeatableCount = groupRepeatableCount;
	}
	public Boolean getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}
	@Override
	public String toString() {
		return "FormValues [id=" + id + ", label=" + label + ", value=" + value + ", count=" + count + ", groupId="
				+ groupId + ", groupRepeatableCount=" + groupRepeatableCount + ", isGroup=" + isGroup + "]";
	}
	
	
}
