package com.terragoedge.slvinterface.model;

import java.util.Objects;

public class EdgeFormData {

	private int id;
	private String label;
	private String value;
	private NotesType type;
	private int count;
	private int groupId = -1;
	private int groupRepeatableCount = -1;
	private boolean isGroup;

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		if (value.contains("#")) {
			String[] values = value.split("#");
			if (values.length == 2) {
				return values[1];
			}
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public NotesType getType() {
		return type;
	}

	public void setType(NotesType type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupRepeatableCount() {
		return groupRepeatableCount;
	}

	public void setGroupRepeatableCount(int groupRepeatableCount) {
		this.groupRepeatableCount = groupRepeatableCount;
	}

	@Override
	public String toString() {
		return "EdgeFormData [id=" + id + ", label=" + label + ", value=" + value + ", type=" + type + ", count="
				+ count + ", groupId=" + groupId + ", groupRepeatableCount=" + groupRepeatableCount + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EdgeFormData that = (EdgeFormData) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {

		return Objects.hash(id);
	}
}
