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
		if(value != null){
			value = value.replace(label+"#", "");
			if(value.equals("(null)")){
				value = null;
			}
		}
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormValues other = (FormValues) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
	
}
